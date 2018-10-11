package com.olx.iris.write
import java.util.concurrent.TimeUnit

import akka.actor.{ ActorLogging, ActorRef, Props, SupervisorStrategy }
import akka.persistence.{ AtLeastOnceDelivery, PersistentActor }
import com.olx.iris.model.{ AddAddressCommand, Address, AddressAddedResponse, AddressExistsResponse }
import com.olx.iris.write.AddressAggregate.{ Event, GetAddressesForwardResponse, MsgAddAddress, MsgConfirmed }
import com.olx.iris.write.AddressEventSender.{ Confirm, Msg }
import com.olx.iris.write.AddressWriteRepository.{ AddAddress, ConfirmAddAddress, GetAddresses }

class AddressAggregate extends PersistentActor with AtLeastOnceDelivery with ActorLogging {

  import akka.pattern.{ ask, pipe }
  import akka.util.Timeout
  import context.dispatcher

  override def persistenceId: String = "address-aggregate"
  override val supervisorStrategy = SupervisorStrategy.stoppingStrategy
  implicit val timeout = Timeout(100, TimeUnit.MILLISECONDS)

  private val addressWriteRepository = context.watch(createAddressWriteRepository())
  private val addressEventSender = context.watch(createAddressEventSender())

  def createAddressWriteRepository(): ActorRef =
    context.actorOf(AddressWriteRepository.props(), AddressWriteRepository.Name)

  def createAddressEventSender(): ActorRef = context.actorOf(AddressEventSender.props(), AddressEventSender.Name)

  override def receiveCommand: Receive = {
    case AddAddressCommand(newAddress) =>
      val origSender = sender()
      val addressesFuture = addressWriteRepository ? GetAddresses
      val _ = pipe(addressesFuture.mapTo[Set[Address]].map(GetAddressesForwardResponse(origSender, _, newAddress))) to self

    case GetAddressesForwardResponse(origSender, addresses, newAddress) =>
      if (addresses.exists(_.userId == newAddress.userId))
        origSender ! AddressExistsResponse(newAddress)
      else {
        persist(MsgAddAddress(newAddress)) { persistedAddress =>
          updateState(persistedAddress)
          origSender ! AddressAddedResponse(newAddress)
        }
      }

    case ConfirmAddAddress(deliveryId) =>
      persist(MsgConfirmed(deliveryId))(updateState)
    case Confirm(deliveryId) =>
      persist(MsgConfirmed(deliveryId))(updateState)
  }
  override def receiveRecover: Receive = {
    case event: Event => updateState(event)
  }

  def updateState(event: Event): Unit = event match {
    case MsgConfirmed(deliveryId) =>
      confirmDelivery(deliveryId); return
    case MsgAddAddress(a) =>
      deliver(addressEventSender.path)(deliveryId => Msg(deliveryId, a))
      deliver(addressWriteRepository.path)(deliveryId => AddAddress(deliveryId, a))
  }
}

object AddressAggregate {
  final val Name = "address-aggregate"

  def props(): Props = Props(new AddressAggregate())

  sealed trait Event
  final case class MsgAddAddress(a: Address) extends Event
  final case class MsgConfirmed(deliverId: Long) extends Event
  final case class GetAddressesForwardResponse(
    senderActor: ActorRef,
    existingAddresses: Set[Address],
    newAddress: Address)
}
