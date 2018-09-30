/*
 * Copyright 2016 Miel Donkers
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.olx.iris.read

import nl.codecentric.coffee.util.DatabaseService

import scala.concurrent.{ExecutionContext, Future}

/**
 * @author Miel Donkers (miel.donkers@codecentric.nl)
 */
class AddressRepository(val databaseService: DatabaseService)(implicit executionContext: ExecutionContext)
    extends AddressEntityTable {

  import databaseService._
  import databaseService.driver.api._

  def getAddresses(): Future[Seq[AddressEntity]] = db.run(addresses.result)

  def getAddressById(id: Long): Future[Option[AddressEntity]] = db.run(addresses.filter(_.id === id).result.headOption)

  def getAddressByUserId(userId: String): Future[Option[AddressEntity]] =
    db.run(addresses.filter(_.userId === userId).result.headOption)

  def createAddress(address: AddressEntity): Future[Long] = db.run((addresses returning addresses.map(_.id)) += address)

  def deleteAddress(id: Long): Future[Int] = db.run(addresses.filter(_.id === id).delete)

  def createTable(): Future[Unit] = {
    db.run(DBIO.seq(addresses.schema.create))
  }
}
