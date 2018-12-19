package com.olx.iris.write

import akka.actor.{Actor, ActorLogging, ActorPath, Props}
import com.olx.iris.model.{Address, Customer, DomainMessageType, DomainObject, Order, PaymentReference, Product}
import com.olx.iris.read.EventReceiver
import com.olx.iris.write.WriteSideEventSender.{
  Confirm, UpdateAddressMsg, UpdateCustomerMsg, WriteEventMsg, WriteSideEvent
}

import scala.collection.immutable

class WriteSideEventSender extends Actor with ActorLogging {

  private val eventReceiver = context.watch(context.actorOf(Props[EventReceiver]))
  private var unconfirmed = immutable.SortedMap.empty[Long, ActorPath]

  def extractWriteEventInfo(deliveryId: Long, payload: DomainObject) = payload match {
    case a: Address          => ("addressId", a.addressId, UpdateAddressMsg(deliveryId, a))
    case c: Customer         => ("userId", c.userId, payload, UpdateCustomerMsg(deliveryId, c))
    case o: Order            => ("orderId", o.orderId, payload, )
    case p: PaymentReference => ("paymentIdentifier", p.paymentIdentifier)
    case pt: Product         => ("orderItemId", pt.orderItemId)
  }

  override def receive: Receive = {
    case WriteEventMsg(deliveryId, messageType, payload) =>
      val (uniqueFieldName, value, writeEvent) = extractWriteEventInfo(deliveryId, payload)
      log.info("Sending {} msg to readside w/{}: {}", messageType.name, value)
      unconfirmed = unconfirmed.updated(deliveryId, sender().path)
      eventReceiver ! writeEvent

    case e: WriteSideEvent =>
      val deliveryId = e.id
      log.info("Event successfully delivered for id {}, sending confirmation", deliveryId)
      unconfirmed
        .get(deliveryId)
        .foreach(senderActor => {
          unconfirmed -= deliveryId
          context.actorSelection(senderActor) ! Confirm(deliveryId)
        })
  }
}

object WriteSideEventSender {
  final val Name = "writeside-event-sender"

  def props(): Props = Props(new WriteSideEventSender())

  final case class WriteEventMsg(deliveryId: Long, messageType: DomainMessageType, payload: DomainObject)
//
//  class WriteEvent(deliveryId: Long, domainObject: DomainObject)
//  object WriteEvent {
//    def unapply(value: WriteEvent): Option[(Long, DomainObject)] = value match {
//      case a: UpdateAddressMsg => Option(a.deliveryId, a.address)
//      case c: UpdateCustomerMsg => Option(c.deliveryId, c.customer)
//      case o: UpdateOrderMsg => Option(o.deliveryId, o.order)
//      case p: UpdatePaymentMsg => Option(p.deliveryId, p.paymentReference)
//      case pt: UpdateProductMsg => Option(pt.deliveryId, pt.product)
//    }
//  }

  sealed trait WriteSideEvent {
    val id: Long
  }
  final case class UpdateAddressMsg(deliveryId: Long, address: Address) extends WriteSideEvent {
    override val id: Long = deliveryId
  }
  final case class UpdateCustomerMsg(deliveryId: Long, customer: Customer) extends WriteSideEvent {
    override val id: Long = deliveryId
  }
  final case class UpdateOrderMsg(deliveryId: Long, order: Order) extends WriteSideEvent {
    override val id: Long = deliveryId
  }
  final case class UpdatePaymentMsg(deliveryId: Long, paymentReference: PaymentReference) extends WriteSideEvent {
    override val id: Long = deliveryId
  }
  final case class UpdateProductMsg(deliveryId: Long, product: Product) extends WriteSideEvent {
    override val id: Long = deliveryId
  }

  final case class Confirm(deliveryId: Long)
}
