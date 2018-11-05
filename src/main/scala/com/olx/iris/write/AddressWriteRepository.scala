package com.olx.iris.write

import akka.actor.{ ActorLogging, Props }
import akka.persistence.PersistentActor
import com.olx.iris.model.Address
import com.olx.iris.write.AddressWriteRepository.{
  AddAddress,
  ConfirmAddAddress,
  ConfirmUpdateAddress,
  GetAddresses,
  UpdateAddress
}

class AddressWriteRepository extends PersistentActor with ActorLogging {
  private var addresses = Map.empty[String, Address]

  override def persistenceId: String = "address-write-repository"

  override def receiveCommand: Receive = {
    case GetAddresses => sender() ! addresses.values.toSet

    case AddAddress(id, address) => storeAddress(id, address)

    case UpdateAddress(id, address) =>
      storeAddress(id, address, true)
  }

  override def receiveRecover: Receive = {
    case address: Address => addresses += (address.addressId -> address)
  }

  def storeAddress(id: Long, address: Address, isUpdate: Boolean = false): Unit = {
    persist(address) { persistedAddress =>
      receiveRecover(persistedAddress)
      val responseMsg = if (isUpdate) ConfirmUpdateAddress(id) else ConfirmAddAddress(id)
      sender() ! responseMsg
    }
  }
}

object AddressWriteRepository {
  final val Name = "address-write-repository"

  def props(): Props = Props(new AddressWriteRepository())

  case object GetAddresses
  final case class AddAddress(deliveryId: Long, address: Address)
  final case class UpdateAddress(deliveryId: Long, address: Address)

  final case class ConfirmAddAddress(deliveryId: Long)
  final case class ConfirmUpdateAddress(deliveryId: Long)
}
