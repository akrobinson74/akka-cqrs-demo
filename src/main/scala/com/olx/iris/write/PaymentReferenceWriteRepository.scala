package com.olx.iris.write
import akka.actor.{ ActorLogging, Props }
import akka.persistence.PersistentActor
import com.olx.iris.model.PaymentReference
import com.olx.iris.write.PaymentReferenceWriteRepository.{ AddPaymentReference, ConfirmAddPaymentReference, ConfirmUpdatePaymentReference, GetPaymentReferences, UpdatePaymentReference }

object PaymentReferenceWriteRepository {
  final val Name = "payment-reference-write-repository"

  def props(): Props = Props(new PaymentReferenceWriteRepository())

  case object GetPaymentReferences
  final case class AddPaymentReference(deliveryId: Long, paymentReference: PaymentReference)
  final case class UpdatePaymentReference(deliveryId: Long, paymentReference: PaymentReference)

  final case class ConfirmAddPaymentReference(deliveryId: Long)
  final case class ConfirmUpdatePaymentReference(deliveryId: Long)
}

class PaymentReferenceWriteRepository extends PersistentActor with ActorLogging {
  private var paymentReferences = Set.empty[PaymentReference]

  override def persistenceId: String = "payment-reference-write-repository"

  override def receiveCommand: Receive = {
    case GetPaymentReferences => sender() ! paymentReferences
    case AddPaymentReference(id, paymentReference) => storePaymentReference(id, paymentReference)
    case UpdatePaymentReference(id, paymentReference) => storePaymentReference(id, paymentReference, true)
  }

  override def receiveRecover: Receive = {
    case paymentReference: PaymentReference => paymentReferences += paymentReference
  }

  def storePaymentReference(id: Long, paymentReference: PaymentReference, isUpdate: Boolean = false): Unit = {
    persist(paymentReference) { persistedPaymentReference =>
      receiveRecover(persistedPaymentReference)
      val responseMsg = if (isUpdate) ConfirmUpdatePaymentReference(id) else ConfirmAddPaymentReference(id)
      sender() ! responseMsg
    }
  }
}
