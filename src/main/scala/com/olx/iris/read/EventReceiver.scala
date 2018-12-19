package com.olx.iris.read

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.camel.Ack
import com.olx.iris.ActorSettings
import com.olx.iris.model.{Address, Customer, DBAddress, DBCustomer, DBOrder, Order, PaymentReference, Product}
import com.olx.iris.write.WriteSideEventSender.{UpdateAddressMsg, UpdateCustomerMsg, UpdateOrderMsg, UpdatePaymentMsg, UpdateProductMsg}

import scala.concurrent.ExecutionContext.Implicits.global

object EventReceiver {
  final val DOMAIN_MSG_TYPE: String = "DOMAIN_MSG_TYPE"
  final val Name = "event-receiver"

  def props(
    addressRepository: AddressReadRepository,
    customerRepository: CustomerReadRepository,
    orderRepository: OrderReadRepository,
    paymentReferenceRepository: PaymentReferenceReadRepository,
    productRepository: ProductReadRepository): Props =
    Props(
      new EventReceiver(
        addressRepository,
        customerRepository,
        orderRepository,
        paymentReferenceRepository,
        productRepository))
}

class EventReceiver(
  addressRepository: AddressReadRepository,
  customerRepository: CustomerReadRepository,
  orderRepository: OrderReadRepository,
  paymentReferenceRepository: PaymentReferenceReadRepository,
  productRepository: ProductReadRepository)
    extends Actor
    with ActorSettings
    with ActorLogging {

  override def receive: Receive = {
    case UpdateAddressMsg(deliveryId, address) =>
      log.info("Event Received with id {} and for domain object: {}", deliveryId, "Address")
      handleAddress(address, deliveryId, sender())

    case UpdateCustomerMsg(deliveryId, customer) =>
      log.info("Event Received with id {} and for domain object: {}", deliveryId, "Customer")
      handleCustomer(customer, deliveryId, sender())

    case UpdateOrderMsg(deliveryId, order) =>
      log.info("Event Received with id {} and for domain object: {}", deliveryId, "Order")
      handleOrder(order, deliveryId, sender())

    case UpdatePaymentMsg(deliveryId, paymentReference) =>
      log.info("Event Received with id {} and for domain object: {}", deliveryId, "PaymentReference")
      handlePaymentReference(paymentReference, deliveryId, sender())

    case UpdateProductMsg(deliveryId, product) =>
      log.info("Event Received with id {} and for domain object: {}", deliveryId, "Product")
      handleProduct(product, deliveryId, sender())

    case _ => log.warning("Unexpected event received")
  }

  def handleAddress(address: Address, messageId: Long, origSender: ActorRef) =
    addressRepository.getAddressByUserId(address.addressId).foreach {
      case Some(_) => log.debug("Address with the addressId {} already exists")
      case None =>
        addressRepository
          .createAddress(AddressEntity(messageSeqNr = messageId, addressInfo = DBAddress(address)))
          .onComplete {
            case scala.util.Success(_) => origSender ! Ack
            case scala.util.Failure(t) =>
              log.error(t, "Failed to persist Address with addressId: {}", address.addressId)
          }
    }

  def handleCustomer(customer: Customer, messageId: Long, origSender: ActorRef) =
    customerRepository.getCustomerByUserId(customer.userId).foreach {
      case Some(_) => log.debug("Customer with the userId {} already exists")
      case None =>
        customerRepository
          .createCustomer(CustomerEntity(messageSeqNr = messageId, customerInfo = DBCustomer(customer)))
          .onComplete {
            case scala.util.Success(_) => origSender ! Ack
            case scala.util.Failure(t) => log.error(t, "Failed to persist Customer with userId: {}", customer.userId)
          }
    }

  def handleOrder(order: Order, messageId: Long, origSender: ActorRef) =
    orderRepository.getOrderByOrderId(order.orderId).foreach {
      case Some(_) => log.debug("Order with the order {} already exists")
      case None =>
        orderRepository
          .createOrder(OrderEntity(messageSeqNr = messageId, orderInfo = DBOrder(order)))
          .onComplete {
            case scala.util.Success(_) => origSender ! Ack
            case scala.util.Failure(t) => log.error(t, "Failed to persist Order with orderId: {}", order.orderId)
          }
    }

  def handlePaymentReference(paymentReference: PaymentReference, messageId: Long, origSender: ActorRef) =
    paymentReferenceRepository.getPaymentReferenceByPaymentId(paymentReference.paymentIdentifier).foreach {
      case Some(_) => log.debug("Order with the order {} already exists")
      case None =>
        paymentReferenceRepository
          .createPaymentReference(
            PaymentReferenceEntity(messageSeqNr = messageId, paymentReferenceInfo = paymentReference))
          .onComplete {
            case scala.util.Success(_) => origSender ! Ack
            case scala.util.Failure(t) =>
              log.error(
                t,
                "Failed to persist PaymentReference with paymentIdentifier: {}",
                paymentReference.paymentIdentifier)
          }
    }

  def handleProduct(product: Product, messageId: Long, origSender: ActorRef) =
    productRepository.getProductByOrderItemId(product.orderItemId).foreach {
      case Some(_) => log.debug("Product with the orderItemId {} already exists")
      case None =>
        productRepository
          .createProduct(ProductEntity(messageSeqNr = messageId, productInfo = product))
          .onComplete {
            case scala.util.Success(_) => origSender ! Ack
            case scala.util.Failure(t) =>
              log.error(t, "Failed to persist Product with orderItemId: {}", product.orderItemId)
          }
    }
}
