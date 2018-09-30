package com.olx.iris.model

final case class AddAddressCommand(address: Address)

final case class AddressAddedResponse(address: Address)
final case class AddressExistsResponse(address: Address)
final case class AddressUpdateResponse(address: Address)
