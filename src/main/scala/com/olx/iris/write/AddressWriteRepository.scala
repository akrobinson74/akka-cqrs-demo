package com.olx.iris.write
import akka.actor.{ActorLogging, Props}
import akka.persistence.PersistentActor
import com.olx.iris.model.Address
import com.olx.iris.write.AddressWriteRepository.{AddAddress, ConfirmAddAddress, GetAddresses}

class AddressWriteRepository extends PersistentActor with ActorLogging {
  private var addresses = Set.empty[Address]

  override def persistenceId: String = "address-write-repository"

  override def receiveCommand: Receive = {
    case GetAddresses =>
      sender() ! addresses
    case AddAddress(id, address) =>
    persist(address) { persistedAddress =>
      receiveRecover(persistedAddress)
      sender() ! ConfirmAddAddress(id)
    }
  }

  override def receiveRecover: Receive = {
    case address: Address => addresses += address
  }
}

object AddressWriteRepository {
  final val Name = "address-write-repository"

  def props(): Props = Props(new AddressWriteRepository())

  case object GetAddresses
  final case class AddAddress(deliveryId: Long, address: Address)
  final case class ConfirmAddAddress(deliveryId: Long)
}
