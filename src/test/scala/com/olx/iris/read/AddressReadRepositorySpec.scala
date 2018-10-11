package com.olx.iris.read

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class AddressReadRepositorySpec
  extends TestKit(ActorSystem("AddressReadRepositorySpec"))
    with ImplicitSender
    with WordSpecLike
    with Matchers
    with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  val probe = TestProbe()
  val addressReadRepository = system.actorOf(AddressReadRepository.props())
}
