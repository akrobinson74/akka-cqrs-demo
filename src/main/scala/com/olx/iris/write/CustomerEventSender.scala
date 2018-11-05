package com.olx.iris.write

import akka.actor.{ Actor, ActorLogging, ActorPath, Props, Status }
import akka.camel.CamelMessage
import com.olx.iris.model.{ Customer, DomainMessageType }
import com.olx.iris.read.EventReceiver.DOMAIN_MSG_TYPE
import com.olx.iris.write.CustomerEventSender.{ Confirm, Msg }
import org.apache.camel.component.rabbitmq.RabbitMQConstants

import scala.collection.immutable

object CustomerEventSender {
  final val Name = "customer-event-sender"

  def props(): Props = Props(new CustomerEventSender())

  final case class Msg(deliveryId: Long, customer: Customer, messageType: String = DomainMessageType.CUSTOMER_STR)
  final case class Confirm(deliveryId: Long)
}

class CustomerEventSender extends Actor with ActorLogging {

  import io.circe.generic.auto._
  import io.circe.syntax._

  private val amqpSender = context.watch(context.actorOf(Props[AmqpSender]))
  private var unconfirmed = immutable.SortedMap.empty[Long, ActorPath]

  override def receive: Receive = {
    case Msg(deliveryId, customer, messageType) =>
      log.info("Sending msg for customer: {}", customer.userId)
      unconfirmed = unconfirmed.updated(deliveryId, sender().path)
      val headersMap = Map(
        RabbitMQConstants.MESSAGE_ID -> deliveryId.toString,
        RabbitMQConstants.CORRELATIONID -> deliveryId.toString,
        DOMAIN_MSG_TYPE -> messageType)
      amqpSender ! CamelMessage(customer.asJson.noSpaces, headersMap)

    case CamelMessage(_, headers) =>
      val deliveryId: Long = headers.getOrElse(RabbitMQConstants.MESSAGE_ID, -1L).asInstanceOf[Long]
      log.info("Event successfully delivered for id {}, sending confirmation", deliveryId)
      unconfirmed
        .get(deliveryId)
        .foreach(senderActor => {
          unconfirmed -= deliveryId
          context.actorSelection(senderActor) ! Confirm(deliveryId)
        })

    case Status.Failure(ex) =>
      log.error("Event delivery failed. Reason: {}", ex.toString)
  }
}
