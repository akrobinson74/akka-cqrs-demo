package com.olx.iris.model

case class Address(
  addressLines: List[String] = List(),
  city: String,
  country: String,
  houseNumber: String,
  region: Option[String] = Option.empty,
  state: Option[String] = Option.empty,
  stateCode: Option[String] = Option.empty,
  street: String,
  addressId: String,
  zipCode: String)
  extends DomainObject

final case class DBAddress(
  addressId: String,
  addressLines: String = "",
  city: String,
  country: String,
  houseNumber: String,
  region: Option[String] = Option.empty,
  state: Option[String] = Option.empty,
  stateCode: Option[String] = Option.empty,
  street: String,
  zipCode: String)
  extends DomainObject

object DBAddress {
  def apply(
    addressId: String,
    addressLines: String = "",
    city: String,
    country: String,
    houseNumber: String,
    region: Option[String] = Option.empty,
    state: Option[String] = Option.empty,
    stateCode: Option[String] = Option.empty,
    street: String,
    zipCode: String): DBAddress =
    new DBAddress(addressId, addressLines, city, country, houseNumber, region, state, stateCode, street, zipCode)

  def apply(address: Address): DBAddress =
    new DBAddress(
      address.addressId,
      address.addressLines.foldLeft("") { (init, n) => init + n },
      address.city,
      address.country,
      address.houseNumber,
      address.region,
      address.state,
      address.stateCode,
      address.street,
      address.zipCode)
}
