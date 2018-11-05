package com.olx.iris.read
import java.sql.Timestamp

import com.olx.iris.model.DBAddress
import com.olx.iris.util.DatabaseService
import slick.sql.SqlProfile.ColumnOption.SqlType

final case class AddressEntity(
  id: Option[Long] = None,
  createdAt: Option[Timestamp] = None,
  updatedAt: Option[Timestamp] = None,
  messageSeqNr: Long,
  addressInfo: DBAddress)

trait AddressEntityTable {

  protected val databaseService: DatabaseService
  import databaseService.driver.api._

  class Addresses(tag: Tag) extends Table[AddressEntity](tag, "addresses") {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def createdAt =
      column[Timestamp]("CREATED_AT", SqlType("timestamp not null default CURRENT_TIMESTAMP"))
    def updatedAt =
      column[Timestamp](
        "UPDATED_AT",
        SqlType("timestamp not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP"))
    def messageSeqNr = column[Long]("MSG_SEQ_NR")
    def addressId = column[String]("ADDRESS_ID")
    def addressLines = column[String]("ADDRESS_LINES")
    def city = column[String]("CITY")
    def country = column[String]("COUNTRY")
    def houseNumber = column[String]("HOUSE_NUMBER")
    def region = column[Option[String]]("REGION")
    def state = column[Option[String]]("STATE")
    def stateCode = column[Option[String]]("STATE_CODE")
    def street = column[String]("STREET")
    def zipCode = column[String]("ZIP_CODE")

    override def * =
      (
        id.?,
        createdAt.?,
        updatedAt.?,
        messageSeqNr,
        (addressId, addressLines, city, country, houseNumber, region, state, stateCode, street, zipCode)).shaped <> ({
          case (id, createdAt, updatedAt, messageSeqNr, addressInfo) =>
            AddressEntity(id, createdAt, updatedAt, messageSeqNr, (infoApply _).tupled(addressInfo))
        }, { ae: AddressEntity =>
          def f1(a: DBAddress) = DBAddress.unapply(a).get
          Some((ae.id, ae.createdAt, ae.updatedAt, ae.messageSeqNr, f1(ae.addressInfo)))
        })

    def infoApply(
      addressId: String,
      addressLines: String = "",
      city: String,
      country: String,
      houseNumber: String,
      region: Option[String] = None,
      state: Option[String] = None,
      stateCode: Option[String] = None,
      street: String,
      zipCode: String) = DBAddress(
      addressId,
      addressLines,
      city,
      country,
      houseNumber,
      region,
      state,
      stateCode,
      street,
      zipCode)
  }

  protected val addresses = TableQuery[Addresses]
}
