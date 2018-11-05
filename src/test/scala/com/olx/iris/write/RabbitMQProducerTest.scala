package com.olx.iris.write
import akka.stream.alpakka.amqp.{AmqpLocalConnectionProvider, AmqpSinkSettings, QueueDeclaration}
import akka.stream.alpakka.amqp.scaladsl.AmqpSink

object RabbitMQProducerTest extends App {

  val queueName = "amqp-conn-it-spec-simple-queue-" + System.currentTimeMillis()
  val queueDeclaration = QueueDeclaration(queueName)
  val sink = AmqpSink.simple(
    AmqpSinkSettings(AmqpLocalConnectionProvider.getInstance())
      .withRoutingKey(queueName)
      .withDeclaration(queueDeclaration)
  )
  "I am awesome"
}
