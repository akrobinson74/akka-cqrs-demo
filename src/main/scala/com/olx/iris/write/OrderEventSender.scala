package com.olx.iris.write
import akka.actor.{ Actor, ActorLogging, ActorPath, Props, Status }
import akka.camel.CamelMessage
import com.olx.iris.model.Order
import com.olx.iris.write.OrderEventSender.{ Confirm, Msg }
import org.apache.camel.component.rabbitmq.RabbitMQConstants

import scala.collection.immutable

object OrderEventSender {

  final val Name = "address-event-sender"

  def props(): Props = Props(new OrderEventSender())

  final case class Msg(deliveryId: Long, order: Order)
  final case class Confirm(deliveryId: Long)
}

class OrderEventSender extends Actor with ActorLogging {

  import io.circe.generic.auto._
  import io.circe.syntax._

  private val amqpSender = context.watch(context.actorOf(Props[AmqpSender]))

  private var unconfirmed = immutable.SortedMap.empty[Long, ActorPath]

  override def receive: Receive = {
    case Msg(deliveryId, order) =>
      log.info("Sending msg for order: {}", order.orderId)
      unconfirmed = unconfirmed.updated(deliveryId, sender().path)
      val headersMap = Map(RabbitMQConstants.MESSAGE_ID -> deliveryId, RabbitMQConstants.CORRELATIONID -> deliveryId)
      amqpSender ! CamelMessage(order.asJson.noSpaces, headersMap)

    case CamelMessage(_, headers) =>
      val deliveryId: Long = headers.getOrElse(RabbitMQConstants.MESSAGE_ID, -1L).asInstanceOf[Long]
      log.info("Event successfully delivered for id {}, sending confirmation", deliveryId)
      unconfirmed
        .get(deliveryId)
        .foreach(
          senderActor => {
            unconfirmed -= deliveryId
            context.actorSelection(senderActor) ! Confirm(deliveryId)
          })

    case Status.Failure(ex) =>
      log.error("Event delivery failed. Reason: {}", ex.toString)
  }
}
