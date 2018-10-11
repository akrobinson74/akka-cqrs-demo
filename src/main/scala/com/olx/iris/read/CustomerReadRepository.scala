package com.olx.iris.read

import com.olx.iris.util.DatabaseService

import scala.concurrent.Future

class CustomerReadRepository(val databaseService: DatabaseService) extends CustomerEntityTable {

  import databaseService._
  import databaseService.driver.api._

  def getCustomers(): Future[Seq[CustomerEntity]] = db.run(customers.result)

  def getCustomerById(id: Long): Future[Option[CustomerEntity]] = db.run(customers.filter(_.id === id).result.headOption)

  def getCustomerByUserId(userId: String): Future[Option[CustomerEntity]] =
    db.run(customers.filter(_.userId === userId).result.headOption)

  def createCustomer(customer: CustomerEntity): Future[Long] =
    db.run((customers returning customers.map(_.id)) += customer)

  def deleteCustomer(id: Long): Future[Int] = db.run(customers.filter(_.id === id).delete)

  def createTable(): Future[Unit] = { db.run(DBIO.seq(customers.schema.create)) }
}
