package com.olx.iris.model

sealed trait Status

object Status {
  final case object ACTIVE extends Status
  final case object INACTIVE extends Status
  final case object PENDING extends Status
}
