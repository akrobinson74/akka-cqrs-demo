package com.olx.iris.model

case class Invoice(
  invoiceNumber: String,
  invoiceLocation: String,
  invoiceType: InvoiceType,
  notified: Boolean,
  transactionId: String
)