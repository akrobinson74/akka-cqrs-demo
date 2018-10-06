package com.olx.iris.read
import java.sql.Timestamp

import com.olx.iris.model.DBCustomer
import com.olx.iris.util.DatabaseService
import slick.sql.SqlProfile.ColumnOption.SqlType

final case class CustomerEntity(
  id: Option[Long] = None,
  createdAt: Option[Timestamp] = None,
  updatedAt: Option[Timestamp] = None,
  messageSeqNr: Long,
  customerInfo: DBCustomer
)

trait CustomerEntityTable {

  protected val databaseService: DatabaseService
  import databaseService.driver.api._

  class Customers(tag: Tag) extends Table[CustomerEntity](tag, "customers") {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def createdAt =
      column[Timestamp]("CREATED_AT", SqlType("timestamp not null default CURRENT_TIMESTAMP"))
    def updatedAt =
      column[Timestamp](
        "UPDATED_AT",
        SqlType("timestamp not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP")
      )
    def messageSeqNr = column[Long]("MSG_SEQ_NR")
    def addressId = column[Long]("ADDRESS_ID")
    def businessName = column[Option[String]]("BUSINESS_NAME")
    def emailAddress = column[String]("EMAIL_ADDRESS")
    def firstName = column[String]("FIRST_NAME")
    def languange = column[String]("LANGUAGE")
    def lastName = column[String]("LAST_NAME")
    def `type` = column[String]("TYPE")
    def userId = column[String]("USER_ID")
    def vatNumber = column[Option[String]]("VAT_NUMBER")

    override def * =
      (
        id.?,
        createdAt.?,
        updatedAt.?,
        messageSeqNr,
        (addressId, businessName, emailAddress, firstName, languange, lastName, `type`, userId, vatNumber)).shaped <> ({
        case (id, createdAt, updatedAt, messageSeqNr, customerInfo) =>
          CustomerEntity(id, createdAt, updatedAt, messageSeqNr, DBCustomer.tupled.apply(customerInfo))
      }, { ce: CustomerEntity =>
        def f1(c: DBCustomer) = DBCustomer.unapply(c).get
        Some((ce.id, ce.createdAt, ce.updatedAt, ce.messageSeqNr, f1(ce.customerInfo)))
      })

    def idx_customer = index("idx_customer", userId, unique = true)
  }

  protected val customers = TableQuery[Customers]
}
