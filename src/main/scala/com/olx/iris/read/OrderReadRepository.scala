package com.olx.iris.read

import com.olx.iris.util.DatabaseService

import scala.concurrent.Future

class OrderReadRepository(val databaseService: DatabaseService) extends OrderEntityTable {

  import databaseService._
  import databaseService.driver.api._

  def getOrders(): Future[Seq[OrderEntity]] = db.run(orders.result)
  def getOrderById(id: Long): Future[Option[OrderEntity]] = db.run(orders.filter(_.id === id).result.headOption)
  def getOrderByOrderId(orderId: String): Future[Option[OrderEntity]] =
    db.run(orders.filter(_.orderId === orderId).result.headOption)
  def createOrder(order: OrderEntity): Future[Long] = db.run((orders returning orders.map(_.id)) += order)
  def deleteOrder(id: Long): Future[Int] = db.run(orders.filter(_.id === id).delete)
  def createTable(): Future[Unit] = db.run(DBIO.seq(orders.schema.create))
}
