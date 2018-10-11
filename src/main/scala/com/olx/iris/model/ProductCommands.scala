package com.olx.iris.model

final case class AddProductCommand(product: Product)

final case class ProductAddedResponse(product: Product)
final case class ProductExistsResponse(product: Product)
final case class ProductUpdateResponse(product: Product)