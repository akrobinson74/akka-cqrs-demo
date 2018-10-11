package com.olx.iris.model

sealed trait RevenueClass

object RevenueClass {
  final case object DAILY extends RevenueClass
  final case object MONTHLY extends RevenueClass
  final case object USAGE extends RevenueClass

  def apply(kind: String): RevenueClass = kind match {
    case "DAILY" => DAILY
    case "MONTHLY" => MONTHLY
    case "USAGE" => USAGE
  }
}
