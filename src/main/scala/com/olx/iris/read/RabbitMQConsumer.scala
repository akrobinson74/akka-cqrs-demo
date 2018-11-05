package com.olx.iris.read

import java.util.concurrent.TimeUnit

import akka.actor.{ ActorSystem, Props }
//import akka.remote.WireFormats
import akka.stream.{ ActorMaterializer, ActorMaterializerSettings, Supervision }
import akka.stream.Supervision.Decider
//import com.olx.iris.ActorSettings
import com.spingo.op_rabbit._
import com.spingo.op_rabbit.PlayJsonSupport._
import com.spingo.op_rabbit.stream.RabbitSource

import scala.concurrent.duration.FiniteDuration
//import scala.concurrent.ExecutionContext.Implicits.global

class RabbitMQConsumer {
  //  implicit val recoveryStrategy = RecoveryStrategy.none
  //  private val rabbitControl = context.watch(context.actorOf(Props[RabbitControl]))
  //
  //  val subscriptionRef = Subscription.run(rabbitControl) {
  //    import Directives._
  //    // A qos of 3 will cause up to 3 concurrent messages to be processed at any given time.
  //    channel(qos = 3) {
  //      consume(topic(queue("such-message-queue"), List("amq.topic"))) {
  //        (body(as[Order])) { domainObject =>
  //          ack
  //        }
  //      }
  //    }
  //  }
  //
  //  override def receive: Receive = ???
  import Directives._
  implicit val system = ActorSystem()
  private val rabbitControl = system.actorOf(Props[RabbitControl], name = "op-rabbit")
  // We define an ActorMaterializer with a resumingDecider supervision strategy,
  // which prevents the graph from stopping when an exception is thrown.
  implicit val materializer = ActorMaterializer(
    ActorMaterializerSettings(system)
      .withSupervisionStrategy(Supervision.resumingDecider: Decider))
  // As a recovery strategy, let's suppose we want all nacked messages to go to
  // an existing queue called "failed-events"
  implicit private val recoveryStrategy = RecoveryStrategy.abandonedQueue(
    new FiniteDuration(length = 1L, unit = TimeUnit.DAYS),
    abandonQueueName = (_: String) => "failed-events")
  //  implicit val
  //  implicit val addressFormat = Json.format[Address]
  //  implicit val customerFormat = Json.format[Customer]
  //  implicit val paymentFormat = Json.format[PaymentReference]
  //  implicit val productFormat = Json.format[Product]
  //  implicit val orderFormat = Json.format[Order]

  //  val rabbitControl = system.actorOf(Props[RabbitControl])

  val src = RabbitSource(
    rabbitControl,
    channel(qos = 3),
    consume(queue("iris-write-events", durable = true, exclusive = false, autoDelete = false)),
    body(as[String]))

}
