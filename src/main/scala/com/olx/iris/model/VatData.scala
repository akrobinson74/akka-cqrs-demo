package com.olx.iris.model

case class VatData(
  applyVat: Boolean = false,
  vatNumber: Option[String]
)
