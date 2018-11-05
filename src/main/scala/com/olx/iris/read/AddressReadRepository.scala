package com.olx.iris.read

import com.olx.iris.util.DatabaseService

import scala.concurrent.Future

class AddressReadRepository(val databaseService: DatabaseService) extends AddressEntityTable {

  import databaseService._
  import databaseService.driver.api._

  def getAddresses(): Future[Seq[AddressEntity]] = db.run(addresses.result)

  def getAddressById(id: Long): Future[Option[AddressEntity]] = db.run(addresses.filter(_.id === id).result.headOption)

  def getAddressByUserId(addressId: String): Future[Option[AddressEntity]] =
    db.run(addresses.filter(_.addressId === addressId).result.headOption)

  def createAddress(address: AddressEntity): Future[Long] = db.run((addresses returning addresses.map(_.id)) += address)

  def deleteAddress(id: Long): Future[Int] = db.run(addresses.filter(_.id === id).delete)

  def createTable(): Future[Unit] = {
    db.run(DBIO.seq(addresses.schema.create))
  }
}
