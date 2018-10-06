package com.olx.iris.swagger

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.github.swagger.akka._
import com.github.swagger.akka.model.Info
import com.olx.iris.AddressService

class SwaggerDocService(address: String, port: Int, system: ActorSystem) extends SwaggerHttpService {
  implicit val actorSystem: ActorSystem = system
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  override val apiClasses = Set(classOf[AddressService])
  override val host = address + ":" + port
  override val info = Info(version = "1.0")
}
