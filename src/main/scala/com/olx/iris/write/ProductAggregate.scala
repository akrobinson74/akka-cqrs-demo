package com.olx.iris.write

import java.util.concurrent.TimeUnit

import akka.actor.{ ActorLogging, ActorRef, Props, SupervisorStrategy }
import akka.persistence.{ AtLeastOnceDelivery, PersistentActor }
import com.olx.iris.model.{ AddProductCommand, Product, ProductAddedResponse, ProductExistsResponse }
import com.olx.iris.write.ProductAggregate.{ Event, GetProductsForwardResponse, MsgAddProduct, MsgConfirmed }
import com.olx.iris.write.ProductEventSender.{ Confirm, Msg }
import com.olx.iris.write.ProductWriteRepository.{ AddProduct, ConfirmAddProduct, GetProducts }

class ProductAggregate extends PersistentActor with AtLeastOnceDelivery with ActorLogging {

  import akka.pattern.{ ask, pipe }
  import akka.util.Timeout
  import context.dispatcher

  override def persistenceId: String = "product-aggregate"
  override val supervisorStrategy = SupervisorStrategy.stoppingStrategy
  implicit val timeout = Timeout(100, TimeUnit.MILLISECONDS)

  private val productEventSender = context.watch(createProductEventSender())
  private val productWriteRepository = context.watch(createProductWriteRepository())

  def createProductEventSender(): ActorRef =
    context.actorOf(ProductEventSender.props(), ProductEventSender.Name)
  def createProductWriteRepository(): ActorRef =
    context.actorOf(ProductWriteRepository.props(), ProductWriteRepository.Name)

  override def receiveCommand: Receive = {
    case AddProductCommand(newProduct) =>
      val origSender = sender()
      val productFuture = productWriteRepository ? GetProducts
      val _ = pipe(productFuture.mapTo[Set[Product]].map(GetProductsForwardResponse(origSender, _, newProduct))) to self

    case GetProductsForwardResponse(origSender, products, newProduct) =>
      if (products.exists(_.orderItemId == newProduct.orderItemId))
        origSender ! ProductExistsResponse(newProduct)
      else {
        persist(MsgAddProduct(newProduct)) { persistedProduct =>
          updateState(persistedProduct)
          origSender ! ProductAddedResponse(newProduct)
        }
      }

    case Confirm(deliveryId) => persist(MsgConfirmed(deliveryId))(updateState)
    case ConfirmAddProduct(deliveryId) => persist(MsgConfirmed(deliveryId))(updateState)
  }

  override def receiveRecover: Receive = {
    case event: Event => updateState(event)
  }

  def updateState(event: Event): Unit = {
    val _ = event match {
      case MsgConfirmed(deliveryId) => confirmDelivery(deliveryId)
      case MsgAddProduct(p) =>
        deliver(productEventSender.path)(deliveryId => Msg(deliveryId, p))
        deliver(productWriteRepository.path)(deliveryId => AddProduct(deliveryId, p))
    }
  }
}

object ProductAggregate {
  final val Name = "product-aggregate"

  def props(): Props = Props(new ProductAggregate())

  sealed trait Event
  final case class MsgAddProduct(p: Product) extends Event
  final case class MsgConfirmed(deliveryId: Long) extends Event
  final case class GetProductsForwardResponse(sender: ActorRef, existingProducts: Set[Product], newProduct: Product)
}
