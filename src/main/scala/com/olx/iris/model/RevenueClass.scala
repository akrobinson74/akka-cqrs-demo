package com.olx.iris.model

sealed trait RevenueClass

object RevenueClass {
  final case object DAILY extends RevenueClass
  final case object MONTHLY extends RevenueClass
  final case object USAGE extends RevenueClass
}
