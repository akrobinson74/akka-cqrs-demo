package com.olx.iris.model

sealed trait CustomerType

object CustomerType {
  case object BUSINESS extends CustomerType
  case object RESIDENTIAL extends CustomerType
}