package com.olx.iris.read
import java.sql.Timestamp
import java.time.{ ZoneId, ZonedDateTime }

import com.olx.iris.model.{ MonetaryAmount, PaymentReference }
import com.olx.iris.util.DatabaseService
import slick.sql.SqlProfile.ColumnOption.SqlType

final case class PaymentReferenceEntity(
  id: Option[Long] = None,
  createdAt: Option[Timestamp] = None,
  updatedAt: Option[Timestamp] = None,
  messageSeqNr: Long,
  paymentReferenceInfo: PaymentReference)

trait PaymentReferenceEntityTable {

  protected val databaseService: DatabaseService
  import databaseService.driver.api._

  class PaymentReferences(tag: Tag) extends Table[PaymentReferenceEntity](tag, "paymentReferences") {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def createdAt =
      column[Timestamp]("CREATED_AT", SqlType("timestamp not null default CURRENT_TIMESTAMP"))
    def updatedAt =
      column[Timestamp](
        "UPDATED_AT",
        SqlType("timestamp not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP"))
    def messageSeqNr = column[Long]("MSG_SEQ_NR")
    def amount = column[BigDecimal]("AMOUNT")
    def currency = column[String]("CURRENCY")
    def executionTime = column[Timestamp]("EXECUTION_TIME")
    def paymentIdentifier = column[String]("PAYMENT_IDENTIFIER")

    override def * =
      (id.?, createdAt.?, updatedAt.?, messageSeqNr, (amount, currency, executionTime, paymentIdentifier)).shaped <> ({
        case (id, createdAt, updatedAt, messageSeqNr, paymentReferenceInfo) =>
          PaymentReferenceEntity(id, createdAt, updatedAt, messageSeqNr, (infoApply _).tupled(paymentReferenceInfo))
      }, { pe: PaymentReferenceEntity =>
        Some((pe.id, pe.createdAt, pe.updatedAt, pe.messageSeqNr, infoUnapply(pe.paymentReferenceInfo)))
      })

    def convertZonedDateTimeToTimestamp(dateTime: ZonedDateTime): Timestamp =
      Timestamp.from(dateTime.toInstant)

    def infoUnapply(pr: PaymentReference) = {
      val (monetaryAmount, executionTime, paymentIdentifier) = PaymentReference.unapply(pr).get
      (monetaryAmount.value, monetaryAmount.currency, convertZonedDateTimeToTimestamp(executionTime), paymentIdentifier)
    }

    def infoApply(
      amount: BigDecimal,
      currency: String,
      executionTime: Timestamp,
      paymentIdentifier: String): PaymentReference =
      PaymentReference(
        amount = MonetaryAmount(currency, amount),
        executionTime = ZonedDateTime.ofInstant(executionTime.toInstant, ZoneId.of("UTC")),
        paymentIdentifier = paymentIdentifier)
    //    def idx_payment_reference = index("idx_payment_reference")
  }

  protected val paymentReferences = TableQuery[PaymentReferences]
}
