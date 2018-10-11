package com.olx.iris.model

final case class AddPaymentReferenceCommand(paymentReference: PaymentReference)

final case class PaymentReferenceAddedResponse(paymentReference: PaymentReference)
final case class PaymentReferenceExistsResponse(paymentReference: PaymentReference)
final case class PaymentReferenceUpdatedResponse(paymentReference: PaymentReference)
