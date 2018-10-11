package com.olx.iris.write
import akka.actor.{ ActorLogging, Props }
import akka.persistence.PersistentActor
import com.olx.iris.model.Customer
import com.olx.iris.write.CustomerWriteRepository.{ AddCustomer, ConfirmAddCustomer, ConfirmUpdateCustomer, GetCustomers, UpdateCustomer }

object CustomerWriteRepository {
  final val Name = "customer-write-repository"

  def props(): Props = Props(new CustomerWriteRepository())

  case object GetCustomers
  final case class AddCustomer(deliveryId: Long, customer: Customer)
  final case class UpdateCustomer(deliveryId: Long, customer: Customer)

  final case class ConfirmAddCustomer(deliveryId: Long)
  final case class ConfirmUpdateCustomer(deliveryId: Long)
}

class CustomerWriteRepository extends PersistentActor with ActorLogging {
  private var customers = Map.empty[String, Customer]

  override def persistenceId: String = "customer-write-repository"

  override def receiveCommand: Receive = {
    case GetCustomers => sender() ! customers.values.toSet
    case AddCustomer(id, customer) => storeCustomer(id, customer)
    case UpdateCustomer(id, customer) => storeCustomer(id, customer, true)
  }

  override def receiveRecover: Receive = {
    case customer: Customer => customers += (customer.userId -> customer)
  }

  def storeCustomer(id: Long, customer: Customer, isUpdate: Boolean = false): Unit = {
    persist(customer) { persistedCustomer =>
      receiveRecover(persistedCustomer)
      val responseMsg = if (isUpdate) ConfirmUpdateCustomer(id) else ConfirmAddCustomer(id)
      sender() ! responseMsg
    }
  }
}
