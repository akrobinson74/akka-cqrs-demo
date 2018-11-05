package com.olx.iris.write

import akka.actor.{ Actor, Props }
import akka.camel.CamelMessage
import com.olx.iris.ActorSettings
import com.spingo.op_rabbit.RabbitControl

class AmqpSender extends Actor with ActorSettings {
  private val rabbitControl = context.watch(context.actorOf(Props[RabbitControl]))

  override def receive: Receive = {
    case cm @ CamelMessage =>
      rabbitControl ! cm
  }
}
