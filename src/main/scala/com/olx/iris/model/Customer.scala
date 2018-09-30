package com.olx.iris.model

final case class Customer(
  address: Address,
  businessName: Option[String],
  emailAddress: String,
  firstName: String,
  language: String,
  lastName: String,
  `type`: CustomerType,
  userId: String,
  vatNumber: Option[String]
)

final case class DBCustomer(
  addressId: Long,
  businessName: Option[String],
  emailAddress: String,
  firstName: String,
  language: String,
  lastName: String,
  `type`: String,
  userId: String,
  vatNumber: Option[String]
)