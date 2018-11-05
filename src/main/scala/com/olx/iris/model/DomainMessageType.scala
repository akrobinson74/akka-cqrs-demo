package com.olx.iris.model

sealed trait DomainMessageType {
  val name: String
}

object DomainMessageType {
  final val ADDRESS_STR: String = "ADDRESS"
  final val CUSTOMER_STR: String = "CUSTOMER"
  final val ORDER_STR: String = "ORDER"
  final val PAYMENT_REFERENCE_STR: String = "PAYMENT_REFERENCE"
  final val PRODUCT_STR: String = "PRODUCT"

  final case object ADDRESS extends DomainMessageType { override val name: String = ADDRESS_STR }
  final case object CUSTOMER extends DomainMessageType { override val name: String = CUSTOMER_STR }
  final case object ORDER extends DomainMessageType { override val name: String = ORDER_STR }
  final case object PAYMENT_REFERENCE extends DomainMessageType { override val name: String = PAYMENT_REFERENCE_STR }
  final case object PRODUCT extends DomainMessageType { override val name: String = PRODUCT_STR }

  def apply(name: String): DomainMessageType = name match {
    case ADDRESS_STR           => ADDRESS
    case CUSTOMER_STR          => CUSTOMER
    case ORDER_STR             => ORDER
    case PAYMENT_REFERENCE_STR => PAYMENT_REFERENCE
    case PRODUCT_STR           => PRODUCT
    case _                     => throw new IllegalArgumentException(s"Invalid domain message type string: $name")
  }
}
