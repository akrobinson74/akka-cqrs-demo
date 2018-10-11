package com.olx.iris.write

import akka.actor.{ ActorLogging, Props }
import akka.persistence.PersistentActor
import com.olx.iris.model.Product
import com.olx.iris.write.ProductWriteRepository.{
  AddProduct,
  ConfirmAddProduct,
  ConfirmUpdateProduct,
  GetProducts,
  UpdateProduct
}

class ProductWriteRepository extends PersistentActor with ActorLogging {
  private var products = Map.empty[String, Product]

  override def persistenceId: String = "product-write-repository"

  override def receiveCommand: Receive = {
    case GetProducts => sender() ! products.values.toSet
    case AddProduct(id, product) => storeProduct(id, product)
    case UpdateProduct(id, product) => storeProduct(id, product, true)
  }

  override def receiveRecover: Receive = {
    case product: Product => products += (product.orderItemId -> product)
  }

  def storeProduct(id: Long, product: Product, isUpdate: Boolean = false) = {
    persist(product) { persistedProduct =>
      receiveRecover(persistedProduct)
      val responseMsg = if (isUpdate) ConfirmUpdateProduct(id) else ConfirmAddProduct(id)
      sender() ! responseMsg
    }
  }
}

object ProductWriteRepository {
  final val Name = "product-write-repository"

  def props(): Props = Props(new ProductWriteRepository())

  case object GetProducts
  final case class AddProduct(deliveryId: Long, product: Product)
  final case class UpdateProduct(deliveryId: Long, product: Product)

  final case class ConfirmAddProduct(deliverId: Long)
  final case class ConfirmUpdateProduct(deliveryId: Long)
}
