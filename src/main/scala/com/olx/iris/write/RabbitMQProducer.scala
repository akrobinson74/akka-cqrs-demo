package com.olx.iris.write

import akka.actor.{ ActorSystem, Props }
import com.spingo.op_rabbit.{ Queue, RabbitControl, RecoveryStrategy }

class RabbitMQProducer {
  //  import com.spingo.op_rabbit.PlayJsonSupport._
  //  import scala.concurrent.ExecutionContext.Implicits.global

  implicit val actorSystem = ActorSystem("demo")

  //  implicit val addressFormat = Json.format[Address]
  //  implicit val customerFormat = Json.format[Customer]
  //  implicit val paymentFormat = Json.format[PaymentReference]
  //  implicit val productFormat = Json.format[Product]
  //  implicit val orderFormat = Json.format[Order]

  val rabbitControl = actorSystem.actorOf(Props(new RabbitControl))
  implicit val recoveryStrategy = RecoveryStrategy.nack(false)

  val demoQueue = Queue("demo", durable = false, autoDelete = true)

  def propogateToRabbit() = ???
}
