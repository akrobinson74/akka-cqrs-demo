package com.olx.iris.write
import java.util.concurrent.TimeUnit

import akka.actor.{ ActorLogging, ActorRef, Props, SupervisorStrategy }
import akka.persistence.{ AtLeastOnceDelivery, PersistentActor }
import com.olx.iris.model.{
  AddCustomerCommand,
  AddOrderCommand,
  AddPaymentReferenceCommand,
  AddProductCommand,
  Order,
  OrderAddedResponse,
  OrderExistsResponse
}
import com.olx.iris.write.OrderAggregate.{ Event, GetOrdersForwardResponse, MsgAddOrder, MsgConfirmed }
import com.olx.iris.write.OrderEventSender.{ Confirm, Msg }
import com.olx.iris.write.OrderWriteRepository.{ AddOrder, ConfirmAddOrder, GetOrders }

class OrderAggregate extends PersistentActor with AtLeastOnceDelivery with ActorLogging {

  import akka.pattern.{ ask, pipe }
  import akka.util.Timeout
  import context.dispatcher

  override def persistenceId: String = "order-aggregate"
  override val supervisorStrategy = SupervisorStrategy.stoppingStrategy
  implicit val timeout = Timeout(100, TimeUnit.MILLISECONDS)

  private val customerAggregate = context.watch(createCustomerAggregate())
  private val orderEventSender = context.watch(createOrderEventSender())
  private val orderWriteRepository = context.watch(createOrderWriteRepository())
  private val paymentReferenceAggregate = context.watch(createPaymentReferenceAggregate())
  private val productAggregate = context.watch(createProductAggregate())

  def createCustomerAggregate(): ActorRef =
    context.actorOf(CustomerAggregate.props(), CustomerAggregate.Name)
  def createPaymentReferenceAggregate(): ActorRef =
    context.actorOf(PaymentReferenceAggregate.props(), PaymentReferenceAggregate.Name)
  def createProductAggregate(): ActorRef =
    context.actorOf(ProductAggregate.props(), ProductAggregate.Name)

  def createOrderWriteRepository(): ActorRef =
    context.actorOf(OrderWriteRepository.props(), OrderWriteRepository.Name)

  def createOrderEventSender(): ActorRef =
    context.actorOf(OrderEventSender.props(), OrderEventSender.Name)

  override def receiveCommand: Receive = {
    case AddOrderCommand(newOrder) =>
      val origSender = sender()
      val orderFuture = orderWriteRepository ? GetOrders
      val _ = pipe(orderFuture.mapTo[Set[Order]].map(GetOrdersForwardResponse(origSender, _, newOrder))) to self

    case GetOrdersForwardResponse(origSender, orders, newOrder) =>
      if (orders.exists(_.orderId == newOrder.orderId))
        origSender ! OrderExistsResponse(newOrder)
      else {
        persist(MsgAddOrder(newOrder)) { persistedOrder =>
          updateState(persistedOrder)
          origSender ! OrderAddedResponse(newOrder)
        }
      }

    case Confirm(deliveryId) =>
      persist(MsgConfirmed(deliveryId))(updateState)
    case ConfirmAddOrder(deliveryId) =>
      persist(MsgConfirmed(deliveryId))(updateState)
  }

  override def receiveRecover: Receive = {
    case event: Event => updateState(event)
  }

  def updateState(event: Event): Unit = event match {
    case MsgConfirmed(deliveryId) =>
      confirmDelivery(deliveryId); return
    case MsgAddOrder(o) =>
      deliver(orderEventSender.path)(deliveryId => Msg(deliveryId, o))
      deliver(orderWriteRepository.path)(deliveryId => AddOrder(deliveryId, o))
      deliver(customerAggregate.path)(deliveryId => AddCustomerCommand(o.customer))
      deliver(paymentReferenceAggregate.path)(deliveryId => AddPaymentReferenceCommand(o.paymentReference))
      o.products.foreach(product => deliver(productAggregate.path)(deliveryId => AddProductCommand(product)))
  }
}

object OrderAggregate {
  final val Name = "order-aggregate"

  def props(): Props = Props(new AddressAggregate())

  sealed trait Event
  final case class MsgAddOrder(o: Order) extends Event
  final case class MsgConfirmed(deliverId: Long) extends Event
  final case class GetOrdersForwardResponse(senderActor: ActorRef, existingOrders: Set[Order], newOrder: Order)

}
