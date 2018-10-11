package com.olx.iris.write
import akka.actor.{ ActorLogging, Props }
import akka.persistence.PersistentActor
import com.olx.iris.model.Order
import com.olx.iris.write.OrderWriteRepository.{ AddOrder, ConfirmAddOrder, ConfirmUpdateOrder, GetOrders, UpdateOrder }

class OrderWriteRepository extends PersistentActor with ActorLogging {
  private var orders = Map.empty[String, Order]

  override def persistenceId: String = "order-write-repository"
  override def receiveCommand: Receive = {
    case GetOrders => sender() ! orders.values.toSet
    case AddOrder(id, order) => storeOrder(id, order)
    case UpdateOrder(id, order) => storeOrder(id, order, true)
  }

  override def receiveRecover: Receive = {
    case order: Order => orders += (order.orderId -> order)
  }

  def storeOrder(id: Long, order: Order, isUpdate: Boolean = false): Unit = {
    persist(order) { persistedOrder =>
      receiveRecover(persistedOrder)
      val responseMsg = if (isUpdate) ConfirmUpdateOrder(id) else ConfirmAddOrder(id)
      sender() ! responseMsg
    }
  }
}

object OrderWriteRepository {
  final val Name = "order-write-repository"

  def props(): Props = Props(new OrderWriteRepository())

  case object GetOrders
  final case class AddOrder(deliveryId: Long, order: Order)
  final case class UpdateOrder(deliveryId: Long, order: Order)

  final case class ConfirmAddOrder(deliveryId: Long)
  final case class ConfirmUpdateOrder(deliveryId: Long)
}
