package com.olx.iris.read

import java.sql.Timestamp
import java.time.{ ZoneId, ZonedDateTime }

import com.olx.iris.model.{ Product, ProductCategory, ProductType, RevenueClass }
import com.olx.iris.util.DatabaseService
import slick.sql.SqlProfile.ColumnOption.SqlType

final case class ProductEntity(
  id: Option[Long] = None,
  createdAt: Option[Timestamp] = None,
  updatedAt: Option[Timestamp] = None,
  messageSeqNr: Long,
  productInfo: Product)

trait ProductEntityTable {

  protected val databaseService: DatabaseService
  import databaseService.driver.api._

  class Products(tag: Tag) extends Table[ProductEntity](tag, "products") {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def createdAt =
      column[Timestamp]("CREATED_AT", SqlType("timestamp not null default CURRENT_TIMESTAMP"))
    def updatedAt =
      column[Timestamp](
        "UPDATED_AT",
        SqlType("timestamp not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP"))
    def messageSeqNr = column[Long]("MSG_SEQ_NR")
    def activationTime = column[Timestamp]("ACTIVATION_TIME")
    def categoryL1 = column[String]("CATEGORY_L1")
    def categoryL2 = column[Option[String]]("CATEGORY_L2")
    def categoryL3 = column[Option[String]]("CATEGORY_L3")
    def currency = column[String]("CURRENCY")
    def description = column[String]("DESCRIPTION")
    def discountAmount = column[Option[BigDecimal]]("DISCOUNT_AMOUNT")
    def expirationTime = column[Timestamp]("EXPIRATION_TIME")
    def grossPrice = column[BigDecimal]("GROSS_PRICE")
    def name = column[String]("NAME")
    def netPrice = column[Option[BigDecimal]]("NET_PRICE")
    def orderItemId = column[String]("ORDER_ITEM_ID")
    def packageId = column[String]("PACKAGE_ID")
    def revenueClass = column[String]("REVENUE_CLASS")
    def skuId = column[String]("SKU_ID")
    def `type` = column[String]("TYPE")
    def unitPrice = column[BigDecimal]("unitPrice")
    def units = column[Long]("UNITS")

    override def * =
      (
        id.?,
        createdAt.?,
        updatedAt.?,
        messageSeqNr,
        (
          activationTime,
          categoryL1,
          categoryL2,
          categoryL3,
          currency,
          description,
          discountAmount,
          expirationTime,
          grossPrice,
          name,
          netPrice,
          orderItemId,
          packageId,
          revenueClass,
          skuId,
          `type`,
          unitPrice,
          units)).shaped <> ({
            case (id, createdAt, updatedAt, messageSeqNr, productInfo) =>
              ProductEntity(
                id,
                createdAt,
                updatedAt,
                messageSeqNr,
                (infoApply _).tupled(productInfo))
          }, { pe: ProductEntity => Some((pe.id, pe.createdAt, pe.updatedAt, pe.messageSeqNr, infoUnapply(pe.productInfo)))
          })

    def infoApply(
      activationTime: Timestamp,
      category1: String,
      category2: Option[String],
      category3: Option[String],
      currency: String,
      description: String,
      discountAmount: Option[BigDecimal],
      expirationTime: Timestamp,
      grossPrice: BigDecimal,
      name: String,
      netPrice: Option[BigDecimal],
      orderItemId: String,
      packageId: String,
      revenueClass: String,
      skuId: String,
      `type`: String,
      unitPrice: BigDecimal,
      units: Long): Product = Product(
      ZonedDateTime.ofInstant(activationTime.toInstant, ZoneId.of("UTC")),
      ProductCategory(category1, category2, category3),
      currency,
      description,
      discountAmount,
      ZonedDateTime.ofInstant(expirationTime.toInstant, ZoneId.of("UTC")),
      grossPrice,
      name,
      netPrice,
      orderItemId,
      packageId,
      RevenueClass(revenueClass),
      skuId,
      ProductType(`type`),
      unitPrice,
      units)

    def infoUnapply(pi: Product) = {
      val (
        activationTime,
        category,
        currency,
        description,
        discountAmount,
        expirationTime,
        grossPrice,
        name,
        netPrice,
        orderItemId,
        packageId,
        revenueClass,
        skuId,
        productType,
        unitPrice,
        units) = Product.unapply(pi).get

      (
        new Timestamp(activationTime.toInstant().getEpochSecond() * 1000L),
        category.level1,
        category.level2,
        category.level3,
        currency,
        description,
        discountAmount,
        new Timestamp(expirationTime.toInstant().getEpochSecond() * 1000L),
        grossPrice,
        name,
        netPrice,
        orderItemId,
        packageId,
        revenueClass.getClass.toString,
        skuId,
        productType.getClass.toString,
        unitPrice,
        units.toLong)
    }
  }
  protected val products = TableQuery[Products]
}
