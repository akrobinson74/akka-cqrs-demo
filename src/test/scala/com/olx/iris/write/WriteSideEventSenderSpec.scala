package com.olx.iris.write
import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import com.olx.iris.read.EventReceiver
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class WriteSideEventSenderSpec
  extends TestKit(ActorSystem("WriteSideEventSenderSpec"))
    with ImplicitSender
    with WordSpecLike
    with Matchers
    with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  val probe = TestProbe()
  val eventReceiver = system.actorOf(EventReceiver.props())
  val writeSideEventSender = system.actorOf(WriteSideEventSender.props())
}
