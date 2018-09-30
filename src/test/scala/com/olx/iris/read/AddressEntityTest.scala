package com.olx.iris.read
import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, FunSuite, Matchers, WordSpecLike}

class AddressEntitySpec extends TestKit(ActorSystem("AddressEntitySpec")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  "An AddressEntity" must {

  }
}
