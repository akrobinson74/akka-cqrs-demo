package com.olx.iris.write

import akka.actor.{ Actor, ActorLogging, ActorPath, Props, Status }
import akka.camel.CamelMessage
import com.olx.iris.model.Product
import com.olx.iris.write.ProductEventSender.{ Confirm, Msg }
import org.apache.camel.component.rabbitmq.RabbitMQConstants

import scala.collection.immutable

object ProductEventSender {

  final val Name = "product-event-sender"

  def props(): Props = Props(new ProductEventSender())

  final case class Msg(deliveryId: Long, product: Product)
  final case class Confirm(deliveryId: Long)
}

class ProductEventSender extends Actor with ActorLogging {

  import io.circe.generic.auto._
  import io.circe.syntax._

  private val amqpSender = context.watch(context.actorOf(Props[AmqpSender]))

  private var unconfirmed = immutable.SortedMap.empty[Long, ActorPath]

  override def receive: Receive = {
    case Msg(deliveryId, product) =>
      log.info("Sending msg for product: {}", product.orderItemId)
      unconfirmed = unconfirmed.updated(deliveryId, sender().path)
      val headersMap = Map(RabbitMQConstants.MESSAGE_ID -> deliveryId, RabbitMQConstants.CORRELATIONID -> deliveryId)
      amqpSender ! CamelMessage(product.asJson.noSpaces, headersMap)

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
