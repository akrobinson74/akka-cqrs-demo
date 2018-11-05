package com.olx.iris.read

import com.olx.iris.util.DatabaseService

import scala.concurrent.Future

class PaymentReferenceReadRepository(val databaseService: DatabaseService) extends PaymentReferenceEntityTable {

  import databaseService._
  import databaseService.driver.api._

  def getPaymentReferences(): Future[Seq[PaymentReferenceEntity]] = db.run(paymentReferences.result)

  def getPaymentReferenceById(id: Long): Future[Option[PaymentReferenceEntity]] =
    db.run(paymentReferences.filter(_.id === id).result.headOption)

  def getPaymentReferenceByPaymentId(paymentIdentifier: String): Future[Option[PaymentReferenceEntity]] =
    db.run(paymentReferences.filter(_.paymentIdentifier === paymentIdentifier).result.headOption)

  def createPaymentReference(paymentReference: PaymentReferenceEntity): Future[Long] =
    db.run((paymentReferences returning paymentReferences.map(_.id)) += paymentReference)

  def deletePaymentReference(objId: Long): Future[Int] = db.run(paymentReferences.filter(_.id === objId).delete)

  def createTable(): Future[Unit] = { db.run(DBIO.seq(paymentReferences.schema.create)) }
}
