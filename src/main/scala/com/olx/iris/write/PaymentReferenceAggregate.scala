package com.olx.iris.write
import java.util.concurrent.TimeUnit

import akka.actor.{ ActorLogging, ActorRef, Props, SupervisorStrategy }
import akka.persistence.{ AtLeastOnceDelivery, PersistentActor }
import com.olx.iris.model.{
  AddPaymentReferenceCommand,
  PaymentReference,
  PaymentReferenceAddedResponse,
  PaymentReferenceExistsResponse
}
import com.olx.iris.write.PaymentReferenceAggregate.{
  Event,
  GetPaymentReferencesForwardResponse,
  MsgAddPaymentReference,
  MsgConfirmed
}
import com.olx.iris.write.PaymentReferenceEventSender.{ Confirm, Msg }
import com.olx.iris.write.PaymentReferenceWriteRepository.{
  AddPaymentReference,
  ConfirmAddPaymentReference,
  GetPaymentReferences
}

class PaymentReferenceAggregate extends PersistentActor with AtLeastOnceDelivery with ActorLogging {

  import akka.pattern.{ ask, pipe }
  import akka.util.Timeout
  import context.dispatcher

  override val supervisorStrategy = SupervisorStrategy.stoppingStrategy
  implicit val timeout = Timeout(100, TimeUnit.MILLISECONDS)
  private val paymentReferenceEventSender = context.watch(createPaymentReferenceEventSender())
  private val paymentReferenceWriteRepository = context.watch(createPaymentReferenceWriteRepository())

  def createPaymentReferenceEventSender(): ActorRef =
    context.actorOf(PaymentReferenceEventSender.props(), PaymentReferenceEventSender.Name)
  def createPaymentReferenceWriteRepository(): ActorRef =
    context.actorOf(PaymentReferenceWriteRepository.props(), PaymentReferenceWriteRepository.Name)

  override def persistenceId: String = "payment-reference-aggregate"

  override def receiveCommand: Receive = {
    case AddPaymentReferenceCommand(newPaymentReference) =>
      val origSender = sender()
      val paymentReferenceFuture = paymentReferenceWriteRepository ? GetPaymentReferences
      val _ = pipe(
        paymentReferenceFuture
          .mapTo[Set[PaymentReference]]
          .map(GetPaymentReferencesForwardResponse(origSender, _, newPaymentReference))) to self

    case GetPaymentReferencesForwardResponse(origSender, paymentReferences, newPaymentReference) =>
      if (paymentReferences.exists(_.paymentIdentifier == newPaymentReference.paymentIdentifier))
        origSender ! PaymentReferenceExistsResponse(newPaymentReference)
      else {
        persist(MsgAddPaymentReference(newPaymentReference)) { persistedPaymentReference =>
          updateState(persistedPaymentReference)
          origSender ! PaymentReferenceAddedResponse(newPaymentReference)
        }
      }

    case ConfirmAddPaymentReference(deliveryId) =>
      persist(MsgConfirmed(deliveryId))(updateState)
    case Confirm(deliveryId) =>
      persist(MsgConfirmed(deliveryId))(updateState)
  }

  override def receiveRecover: Receive = {
    case event: Event => updateState(event)
  }

  def updateState(event: Event): Unit = event match {
    case MsgConfirmed(deliveryId) =>
      confirmDelivery(deliveryId); return
    case MsgAddPaymentReference(pr) =>
      deliver(paymentReferenceEventSender.path)(deliveryId => Msg(deliveryId, pr))
      deliver(paymentReferenceWriteRepository.path)(deliveryId => AddPaymentReference(deliveryId, pr))
  }
}

object PaymentReferenceAggregate {
  final val Name = "payment-reference-aggregate"

  def props(): Props = Props(new PaymentReferenceAggregate())

  sealed trait Event
  final case class MsgAddPaymentReference(pr: PaymentReference) extends Event
  final case class MsgConfirmed(deliveryId: Long) extends Event
  final case class GetPaymentReferencesForwardResponse(
    senderActor: ActorRef,
    existingPaymentReferences: Set[PaymentReference],
    newPaymentReference: PaymentReference)
}
