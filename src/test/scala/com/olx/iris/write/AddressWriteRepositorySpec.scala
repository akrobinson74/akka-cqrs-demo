package com.olx.iris.write

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.testkit._
import com.olx.iris.model.Address
import com.olx.iris.write.AddressWriteRepository.{ AddAddress, ConfirmAddAddress, ConfirmUpdateAddress, GetAddresses, UpdateAddress }
import org.scalatest.{ BeforeAndAfterAll, Matchers, WordSpecLike }

import scala.concurrent.duration.FiniteDuration

class AddressWriteRepositorySpec
  extends TestKit(ActorSystem("AddressWriteRepositorySpec"))
  with ImplicitSender
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

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
    zipCode = "70126")

  "An AddressWriteRepository" must {
    "add a new address to its set of addresses" in {
      probe.send(writeRepo, AddAddress(0L, newAddress))
      probe.expectMsg(ConfirmAddAddress(0L))
    }

    "getAddresses returns on Address after one had been added" in {
      probe.send(writeRepo, GetAddresses)
      val getAddressesResponse = probe.receiveOne(FiniteDuration(10L, TimeUnit.SECONDS))
      assert(getAddressesResponse != null, "GetAddresses message had a null response")
    }

    "UpdateAddress message replaces a pre-existing Address" in {
      val updatedAddress = newAddress.copy(addressLines = List("Actualy my US mailing address", "No, really!"))
      probe.send(writeRepo, UpdateAddress(1L, updatedAddress))
      probe.expectMsg(ConfirmUpdateAddress(1L))
      probe.send(writeRepo, GetAddresses)
      val addresses = probe.receiveOne(FiniteDuration(10L, TimeUnit.SECONDS)).asInstanceOf[Set[Address]]
      assert(addresses.size == 1, "AddressMap is expected size")
      assert(addresses.head != newAddress, "Address changed")
    }
  }

}
