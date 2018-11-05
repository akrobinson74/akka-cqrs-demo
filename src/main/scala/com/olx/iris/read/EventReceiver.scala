package com.olx.iris.read

import akka.actor.{ ActorLogging, ActorRef, Props }
import akka.actor.Status.Failure
import akka.camel.{ Ack, CamelMessage, Consumer }
import com.olx.iris.ActorSettings
import com.olx.iris.model.{
  Address,
  Customer,
  DBAddress,
  DBCustomer,
  DBOrder,
  DomainMessageType,
  DomainObject,
  Order,
  PaymentReference,
  Product
}
import com.olx.iris.model.DomainMessageType.{ ADDRESS, CUSTOMER, ORDER, PAYMENT_REFERENCE, PRODUCT }
import com.olx.iris.read.EventReceiver.DOMAIN_MSG_TYPE
import org.apache.camel.component.rabbitmq.RabbitMQConstants

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
    extends Consumer
    with ActorSettings
    with ActorLogging {

  import io.circe._
  import io.circe.generic.auto._
  import io.circe.parser._

  //  implicit val executionContext: ExecutionContext = global

  override def endpointUri: String = settings.rabbitMQ.uri

  override def autoAck = false

  override def receive: Receive = {
    case msg: CamelMessage =>
      val origSender = sender()
      val domainMessageType: DomainMessageType = DomainMessageType(msg.headers.get(DOMAIN_MSG_TYPE).get.toString)
      val body: Either[Error, DomainObject] = decodeBody(domainMessageType, msg)

      body.fold(
        { error =>
          log.error("Could not parse message: {}", msg)
          origSender ! Failure(error)
        }, { domainObject =>
          val messageId: Long = decodeMessageId(msg)

          log.info("Event Received with id {} and for domain object: {}", messageId, domainMessageType)

          handleDomainMessage(domainMessageType, domainObject, messageId, origSender)
        }
      )

    case _ => log.warning("Unexpected event received")
  }

  def decodeBody(domainMessageType: DomainMessageType, msg: CamelMessage): Either[Error, DomainObject] =
    domainMessageType match {
      case ADDRESS           => decode[Address](msg.bodyAs[String])
      case CUSTOMER          => decode[Customer](msg.bodyAs[String])
      case ORDER             => decode[Order](msg.bodyAs[String])
      case PAYMENT_REFERENCE => decode[PaymentReference](msg.bodyAs[String])
      case PRODUCT           => decode[Product](msg.bodyAs[String])
    }

  def handleDomainMessage(
    domainMessageType: DomainMessageType,
    domainObject: DomainObject,
    messageId: Long,
    origSender: ActorRef) = domainMessageType match {
    case ADDRESS           => handleAddress(domainObject.asInstanceOf[Address], messageId, origSender)
    case CUSTOMER          => handleCustomer(domainObject.asInstanceOf[Customer], messageId, origSender)
    case ORDER             => handleAddress(domainObject.asInstanceOf[Address], messageId, origSender)
    case PAYMENT_REFERENCE => handleAddress(domainObject.asInstanceOf[Address], messageId, origSender)
    case PRODUCT           => handleAddress(domainObject.asInstanceOf[Address], messageId, origSender)
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

  def decodeMessageId(msg: CamelMessage): Long = msg.headers.get(RabbitMQConstants.MESSAGE_ID) match {
    case Some(id: Long)   => id
    case Some(id: String) => id.toLong
    case _                => -1
  }
}
