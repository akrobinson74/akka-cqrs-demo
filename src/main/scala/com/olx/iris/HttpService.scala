package com.olx.iris

import akka.actor.{ Actor, ActorLogging, ActorRef, Props, Status }
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.pattern.pipe
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.olx.iris.read.AddressReadRepository
import com.olx.iris.swagger.SwaggerDocService

object HttpService {

  private[iris] case object Stop

  // $COVERAGE-OFF$
  final val Name = "http-service"
  // $COVERAGE-ON$

  def props(
    address: String,
    port: Int,
    internalTimeout: Timeout,
    addressAggregate: ActorRef,
    addressRepository: AddressReadRepository): Props =
    Props(new HttpService(address, port, internalTimeout, addressAggregate, addressRepository))

  private[iris] def route(
    httpService: ActorRef,
    addressService: AddressService,
    swaggerDocService: SwaggerDocService) = {
    import Directives._

    // format: OFF
    def assets = pathPrefix("swagger") {
      getFromResourceDirectory("swagger") ~ pathSingleSlash(get(redirect("index.html", StatusCodes.PermanentRedirect))) }

    def stop = pathSingleSlash {
      delete {
        complete {
          httpService ! Stop
          "Stopping ..."
        }
      }
    }

    assets ~ stop ~ addressService.route ~ swaggerDocService.routes
  }
}

class HttpService(address: String, port: Int, internalTimeout: Timeout, addressAggregate: ActorRef, addressRepository: AddressReadRepository)
  extends Actor with ActorLogging {
  import HttpService._
  import context.dispatcher

  private implicit val mat = ActorMaterializer()

  Http(context.system)
    .bindAndHandle(
      route(self, new AddressService(addressAggregate, addressRepository, internalTimeout), new SwaggerDocService(address, port, context.system)),
      address,
      port)
    .pipeTo(self)

  override def receive = binding

  private def binding: Receive = {
    case serverBinding @ Http.ServerBinding(address) =>
      log.info("Listening on {}", address)
      context.become(bound(serverBinding))

    case Status.Failure(cause) =>
      log.error(cause, s"Can't bind to $address:$port")
      context.stop(self)
  }

  private def bound(serverBinding: Http.ServerBinding): Receive = {
    case Stop =>
      serverBinding.unbind()
      context.stop(self)
  }
}