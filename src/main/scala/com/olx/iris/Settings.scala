package com.olx.iris

import akka.actor.{ Actor, ExtendedActorSystem, Extension, ExtensionKey }

import scala.concurrent.duration.{ FiniteDuration, MILLISECONDS }

object Settings extends ExtensionKey[Settings]

class Settings(system: ExtendedActorSystem) extends Extension {

  object httpService {
    val address: String = irisApp.getString("http-service.address")
    val port: Int = irisApp.getInt("http-service.port")
    val selfTimeout: FiniteDuration = getDuration("http-service.self-timeout")
  }

  object rabbitMQ {
    val uri: String = irisApp.getString("rabbitmq.uri")
  }

  object mariaDB {
    val uri: String = irisApp.getString("mariadb.uri")
    val user: String = irisApp.getString("mariadb.user")
    val password: String = irisApp.getString("mariadb.password")
  }

  private val irisApp = system.settings.config.getConfig("iris-app")

  private def getDuration(key: String) = FiniteDuration(irisApp.getDuration(key, MILLISECONDS), MILLISECONDS)
}

trait ActorSettings {
  this: Actor =>
  val settings: Settings = Settings(context.system)
}
