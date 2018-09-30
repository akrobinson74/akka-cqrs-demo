package com.olx.iris.model

sealed trait InvoiceType

object InvoiceType {
  case object CREDIT_NOTE extends InvoiceType
  case object INVOICE extends InvoiceType
}
