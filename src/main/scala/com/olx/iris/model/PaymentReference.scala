package com.olx.iris.model

import java.time.ZonedDateTime

case class PaymentReference(
  amount: MonetaryAmount,
  executionTime: ZonedDateTime
)

