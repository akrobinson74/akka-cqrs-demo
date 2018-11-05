package com.olx.iris.write

import akka.actor.ActorSystem
import akka.testkit.{ ImplicitSender, TestKit, TestProbe }
import org.scalatest.{ BeforeAndAfterAll, Matchers, WordSpecLike }

class AddressAggregateSpec
  extends TestKit(ActorSystem("AddressAggregateSpec"))
  with ImplicitSender
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  val probe = TestProbe()
  val addressAggregate = system.actorOf(AddressAggregate.props())

  "An AddressAggregate" must {
  }
}
