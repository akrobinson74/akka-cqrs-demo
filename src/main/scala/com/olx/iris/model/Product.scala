package com.olx.iris.model

import java.time.ZonedDateTime

case class Product(
  activationTime: ZonedDateTime,
  category: ProductCategory,
//  currency: String = "CRD",
  description: String,
//  discountAmount: Option[BigDecimal],
  expirationTime: ZonedDateTime,
  grossPrice: BigDecimal = BigDecimal(0.00),
  name: String,
//  netPrice: Option[BigDecimal],
  orderItemId: String,
  packageId: String,
  revenueClass: RevenueClass,
  skuId: String,
  `type`: ProductType,
  unitPrice: BigDecimal = BigDecimal(1.00),
  units: BigInt
)
