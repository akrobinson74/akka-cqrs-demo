package com.olx.iris.model

final case class Order(
  customer: Customer,
  orderId: String,
  paymentReference: PaymentReference,
  products: List[Product],
  source: String,
  status: Status)
  extends DomainObject

final case class DBOrder(
  userId: String,
  orderId: String,
  paymentReferenceId: String,
  products: String,
  source: String,
  status: String)
  extends DomainObject

object DBOrder {
  def apply(
    userId: String,
    orderId: String,
    paymentReferenceId: String,
    products: String,
    source: String,
    status: String): DBOrder = new DBOrder(userId, orderId, paymentReferenceId, products, source, status)

  def apply(order: Order): DBOrder = new DBOrder(
    order.customer.userId,
    order.orderId,
    order.paymentReference.paymentIdentifier,
    order.products.foldLeft("") { (init, current) => init + "," + current.orderItemId },
    order.source,
    order.status.toString)
}