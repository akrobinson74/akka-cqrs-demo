package com.olx.iris.model

final case class AddOrderCommand(order: Order)

final case class OrderAddedResponse(order: Order)
final case class OrderExistsResponse(order: Order)
final case class OrderUpdatedResponse(order: Order)
