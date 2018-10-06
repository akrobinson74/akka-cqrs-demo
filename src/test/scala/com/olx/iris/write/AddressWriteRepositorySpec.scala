package com.olx.iris.write

import akka.actor.ActorSystem
import akka.testkit._
import com.olx.iris.model.Address
import com.olx.iris.write.AddressWriteRepository.{ AddAddress, ConfirmAddAddress }
import org.scalatest.{ BeforeAndAfterAll, Matchers, WordSpecLike }

class AddressWriteRepositorySpec
    extends TestKit(ActorSystem("AddressWriteRepositorySpec"))
    with ImplicitSender
    with WordSpecLike
    with Matchers
    with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "An AddressWriteRepository" must {
    "add a new address to its set of addresses" in {
      val probe = TestProbe()
      val writeRepo = system.actorOf(AddressWriteRepository.props())
      val newAddress = new Address(
        addressLines = List("line1", "line2"),
        city = "New Orleans",
        country = "USA",
        houseNumber = "7001",
        state = Option[String]("LA"),
        street = "Neptune Ct",
        userId = "player1",
        zipCode = "70126"
      )
      probe.send(writeRepo, AddAddress(0L, newAddress))
//      val msg = probe.receiveOne(FiniteDuration(10L, TimeUnit.SECONDS))
      probe.expectMsg(ConfirmAddAddress(0L))
    }
  }

}
