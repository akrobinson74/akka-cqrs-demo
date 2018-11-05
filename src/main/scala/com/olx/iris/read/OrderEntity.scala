package com.olx.iris.read

import java.sql.Timestamp

import com.olx.iris.model.DBOrder
import com.olx.iris.util.DatabaseService
import slick.sql.SqlProfile.ColumnOption.SqlType

final case class OrderEntity(
  id: Option[Long] = None,
  createdAt: Option[Timestamp] = None,
  updatedAt: Option[Timestamp] = None,
  messageSeqNr: Long,
  orderInfo: DBOrder)

trait OrderEntityTable {

  protected val databaseService: DatabaseService
  import databaseService.driver.api._

  class Orders(tag: Tag) extends Table[OrderEntity](tag, "orders") {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def createdAt =
      column[Timestamp]("CREATED_AT", SqlType("timestamp not null default CURRENT_TIMESTAMP"))
    def updatedAt =
      column[Timestamp](
        "UPDATED_AT",
        SqlType("timestamp not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP"))
    def messageSeqNr = column[Long]("MSG_SEQ_NR")
    def userId = column[String]("USER_ID")
    def orderId = column[String]("ORDER_ID")
    def paymentIdentifier = column[String]("PAYMENT_IDENTIFIER")
    def orderItemIds = column[String]("ORDER_ITEM_IDS")
    def source = column[String]("SOURCE")
    def status = column[String]("STATUS")

    override def * =
      (id.?, createdAt.?, updatedAt.?, messageSeqNr, (userId, orderId, paymentIdentifier, orderItemIds, source, status)).shaped <> ({
        case (id, createdAt, updatedAt, messageSeqNr, orderInfo) =>
          OrderEntity(id, createdAt, updatedAt, messageSeqNr, (infoApply _).tupled.apply(orderInfo))
      }, { oe: OrderEntity =>
        def f1(o: DBOrder) = DBOrder.unapply(o).get
        Some((oe.id, oe.createdAt, oe.updatedAt, oe.messageSeqNr, f1(oe.orderInfo)))
      })

    def idx_order = index("idx_order", orderId, unique = true)

    def infoApply(
      userId: String,
      orderId: String,
      paymentReferenceId: String,
      products: String,
      source: String,
      status: String): DBOrder = new DBOrder(userId, orderId, paymentReferenceId, products, source, status)
  }

  protected val orders = TableQuery[Orders]
}
