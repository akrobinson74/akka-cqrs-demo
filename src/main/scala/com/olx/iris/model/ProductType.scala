package com.olx.iris.model

sealed trait ProductType {
  val fiqasTransactionType: String
}

object ProductType {
  case object AD_UPGRADE extends ProductType {
    override val fiqasTransactionType: String = "CashPurchase"
  }
  case object EFFORT extends ProductType {
    override val fiqasTransactionType: String = "Reward"
  }
  case object LIMIT extends ProductType {
    override val fiqasTransactionType: String = "CashPurchase"
  }
  case object REWARD extends ProductType {
    override val fiqasTransactionType: String = "Reward"
  }
  case object TOPUP extends ProductType {
    override val fiqasTransactionType: String = "WalletTopUp"
  }
}
