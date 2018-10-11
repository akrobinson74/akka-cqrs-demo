package com.olx.iris.write
import java.util.concurrent.TimeUnit

import akka.actor.{ ActorLogging, ActorRef, Props, SupervisorStrategy }
import akka.persistence.{ AtLeastOnceDelivery, PersistentActor }
import com.olx.iris.model.{ AddCustomerCommand, Customer, CustomerAddedResponse, CustomerExistsResponse }
import com.olx.iris.write.CustomerAggregate.{ Event, GetCustomersForwardResponse, MsgAddCustomer, MsgConfirmed }
import com.olx.iris.write.CustomerEventSender.Msg
import com.olx.iris.write.CustomerWriteRepository.{ AddCustomer, GetCustomers }

class CustomerAggregate extends PersistentActor with AtLeastOnceDelivery with ActorLogging {

  import akka.pattern.{ ask, pipe }
  import akka.util.Timeout
  import context.dispatcher

  override def persistenceId: String = "customer-aggregate"
  override val supervisorStrategy = SupervisorStrategy.stoppingStrategy
  implicit val timeout = Timeout(100, TimeUnit.MILLISECONDS)

  private val customerEventSender = context.watch(createCustomerEventSender())
  private val customerWriteRepository = context.watch(createCustomerWriteRepository())

  def createCustomerEventSender(): ActorRef =
    context.actorOf(CustomerEventSender.props(), CustomerEventSender.Name)
  def createCustomerWriteRepository(): ActorRef =
    context.actorOf(CustomerWriteRepository.props(), CustomerWriteRepository.Name)

  override def receiveCommand: Receive = {
    case AddCustomerCommand(newCustomer) =>
      val originalSender = sender()
      val customerFuture = customerWriteRepository ? GetCustomers
      val _ = pipe(
        customerFuture
          .mapTo[Set[Customer]]
          .map(GetCustomersForwardResponse(originalSender, _, newCustomer))) to self

    case GetCustomersForwardResponse(origSender, customers, newCustomer) =>
      if (customers.exists(_.userId == newCustomer.userId))
        origSender ! CustomerExistsResponse(newCustomer)
      else {
        persist(MsgAddCustomer(newCustomer)) { persistedCustomer =>
          updateState(persistedCustomer)
          origSender ! CustomerAddedResponse(newCustomer)
        }
      }
  }
  override def receiveRecover: Receive = {
    case event: Event => updateState(event)
  }

  def updateState(event: Event): Unit = event match {
    case MsgConfirmed(deliveryId) =>
      confirmDelivery(deliveryId); return
    case MsgAddCustomer(c) =>
      deliver(customerEventSender.path)(deliveryId => Msg(deliveryId, c))
      deliver(customerWriteRepository.path)(deliveryId => AddCustomer(deliveryId, c))
  }
}

object CustomerAggregate {
  final val Name = "customer-aggregate"

  def props(): Props = Props(new CustomerAggregate())

  sealed trait Event
  final case class MsgAddCustomer(c: Customer) extends Event
  final case class MsgConfirmed(deliveryId: Long) extends Event
  final case class GetCustomersForwardResponse(
    senderActor: ActorRef,
    existingCustomers: Set[Customer],
    newCustomer: Customer)
}
