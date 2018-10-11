package com.olx.iris.read

import com.olx.iris.util.DatabaseService

import scala.concurrent.Future

class ProductReadRepository(val databaseService: DatabaseService) extends ProductEntityTable {
  import databaseService._
  import databaseService.driver.api._

  def getProducts(): Future[Seq[ProductEntity]] = db.run(products.result)
  def getProductId(id: Long): Future[Option[ProductEntity]] = db.run(products.filter(_.id === id).result.headOption)
  def getProductByOrderItemId(orderItemId: String): Future[Option[ProductEntity]] =
    db.run(products.filter(_.orderItemId === orderItemId).result.headOption)
  def createProduct(product: ProductEntity): Future[Long] = db.run((products returning products.map(_.id)) += product)
  def deleteProduct(id: Long): Future[Int] = db.run(products.filter(_.id === id).delete)
  def createTable(): Future[Unit] = { db.run(DBIO.seq(products.schema.create)) }
}
