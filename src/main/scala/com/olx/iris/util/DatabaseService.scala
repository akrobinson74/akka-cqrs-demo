package com.olx.iris.util

import com.zaxxer.hikari.HikariDataSource

class DatabaseService(jdbcUrl: String, dbUser: String, dbPassword: String) {
  private val ds = new HikariDataSource()
  ds.setMaximumPoolSize(20)
  ds.setDriverClassName("org.mariadb.jdbc.Driver")
  ds.setJdbcUrl(jdbcUrl)
  ds.addDataSourceProperty("user", dbUser)
  ds.addDataSourceProperty("password", dbPassword)

  val driver = slick.jdbc.MySQLProfile

  import driver.api._
  val db = Database.forDataSource(
    ds = ds,
    maxConnections = Option.empty)
  implicit val dbSession = db.createSession()
}
