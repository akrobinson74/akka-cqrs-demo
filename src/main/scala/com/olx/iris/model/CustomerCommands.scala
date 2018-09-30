package com.olx.iris.model

final case class AddCustomerCommand(customer: Customer)

final case class CustomerAddedResponse(customer: Customer)
final case class CustomerExistsResponse(customer: Customer)
final case class CustomerUpdatedResponse(customer: Customer)
