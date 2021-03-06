package com.olx.iris.write

import akka.actor.{ Actor, ActorLogging, ActorPath, Props, Status }
import akka.camel.CamelMessage
import com.olx.iris.model.{ Address, DomainMessageType }
import com.olx.iris.read.EventReceiver.DOMAIN_MSG_TYPE
import org.apache.camel.component.rabbitmq.RabbitMQConstants

import scala.collection.immutable

object AddressEventSender {

  final val Name = "address-event-sender"

  def props(): Props = Props(new AddressEventSender())

  final case class Msg(deliveryId: Long, address: Address, messageType: String = DomainMessageType.ADDRESS_STR)

  final case class Confirm(deliveryId: Long)
}

class AddressEventSender extends Actor with ActorLogging {
  import AddressEventSender._
  import io.circe.generic.auto._
  import io.circe.syntax._

  private val amqpSender = context.watch(context.actorOf(Props[AmqpSender]))

  private var unconfirmed = immutable.SortedMap.empty[Long, ActorPath]

  override def receive: Receive = {
    case Msg(deliveryId, address, messageType) =>
      log.info("Sending msg for address: {}", address.addressId)
      unconfirmed = unconfirmed.updated(deliveryId, sender().path)
      val headersMap = Map(
        DOMAIN_MSG_TYPE -> messageType,
        RabbitMQConstants.MESSAGE_ID -> deliveryId.toString,
        RabbitMQConstants.CORRELATIONID -> deliveryId.toString)
      amqpSender ! CamelMessage(address.asJson.noSpaces, headersMap)

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
