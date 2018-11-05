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
  vatNumber: Option[String])
  extends DomainObject

final case class DBCustomer(
  addressId: String,
  businessName: Option[String],
  emailAddress: String,
  firstName: String,
  language: String,
  lastName: String,
  `type`: String,
  userId: String,
  vatNumber: Option[String])
  extends DomainObject

object DBCustomer {
  def apply(customer: Customer): DBCustomer =
    new DBCustomer(
      customer.address.addressId,
      customer.businessName,
      customer.emailAddress,
      customer.firstName,
      customer.language,
      customer.lastName,
      customer.`type`.toString,
      customer.userId,
      customer.vatNumber)
}