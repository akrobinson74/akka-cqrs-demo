package com.olx.iris.write
import akka.actor.{ Actor, ActorLogging, ActorPath, Props }
import com.olx.iris.model.{ Address, Customer, DomainMessageType, DomainObject, Order, PaymentReference, Product }
import com.olx.iris.read.EventReceiver
import com.olx.iris.write.WriteSideEventSender.UpdateEventMsg

import scala.collection.immutable

class WriteSideEventSender extends Actor with ActorLogging {

  private val eventReceiver = context.watch(context.actorOf(Props[EventReceiver]))
  private var unconfirmed = immutable.SortedMap.empty[Long, ActorPath]

  def getPayloadIdentifier(payload: DomainObject) = payload match {
    case a: Address          => ("addressId", a.addressId)
    case c: Customer         => ("userId", c.userId)
    case o: Order            => ("orderId", o.orderId)
    case p: PaymentReference => ("paymentIdentifier", p.paymentIdentifier)
    case pt: Product         => ("orderItemId", pt.orderItemId)
  }

  override def receive: Receive = {
    case UpdateEventMsg(deliveryId, messageType, payload) =>
      log.info("Sending {} msg to readside w/{}: {}", messageType.name, getPayloadIdentifier(payload))
      unconfirmed = unconfirmed.updated(deliveryId, sender().path)
      eventReceiver ! UpdateEventMsg

    case
  }
}

object WriteSideEventSender {
  final val Name = "writeside-event-sender"

  def props(): Props = Props(new WriteSideEventSender())

  final case class WriteEventMsg(deliveryId: Long, messageType: DomainMessageType, payload: DomainObject)

  final case class UpdateAddressMsg(deliveryId: Long, address: Address)
  final case class UpdateCustomerMsg(deliveryId: Long, customer: Customer)
  final case class UpdateOrderMsg(deliveryId: Long, order: Order)
  final case class UpdatePaymentMsg(deliveryId: Long, paymentReference: PaymentReference)
  final case class UpdateProductMsg(deliveryId: Long, product: Product)

  final case class Confirm(deliveryId: Long)
}
