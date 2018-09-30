package com.olx.iris.write
import akka.actor.{ ActorLogging, ActorRef, Props }
import akka.persistence.{ AtLeastOnceDelivery, PersistentActor }
import com.olx.iris.model.Customer

class CustomerAggregate extends PersistentActor with AtLeastOnceDelivery with ActorLogging {
  override def persistenceId: String = "customer-aggregate"
  override def receiveCommand: Receive = ???
  override def receiveRecover: Receive = ???
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
