package com.olx.iris
import akka.actor.ActorRef
import akka.http.scaladsl.model.{ HttpResponse, StatusCodes }
import akka.http.scaladsl.server.Directives
import akka.pattern.ask
import akka.util.Timeout
import com.olx.iris.model.{ AddAddressCommand, Address, AddressAddedResponse, AddressExistsResponse }
import com.olx.iris.read.AddressRepository
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.swagger.annotations.{ Api, ApiImplicitParam, ApiImplicitParams, ApiOperation, ApiResponse, ApiResponses }
import javax.ws.rs.Path

import scala.util.{ Failure, Success }

@Path("/addresses")
@Api(value = "/addresses", produces = "application/json")
class AddressService(
  addressAggregate: ActorRef,
  addressRepository: AddressRepository,
  internalTimeout: Timeout
) extends Directives {

  import FailFastCirceSupport._
  import io.circe.generic.auto._

  implicit val timeout = internalTimeout

  val route = pathPrefix("addresses") { addressesGetAll ~ addressPost }

  @ApiOperation(
    value = "Get list of all addresses",
    nickname = "getAllAddresses",
    httpMethod = "GET",
    response = classOf[Address],
    responseContainer = "Set")
  @ApiResponses(
    Array(
      new ApiResponse(code = 500, message = "Internal error")
    ))
  def addressesGetAll = get {
    onComplete(addressRepository.getAddresses()) {
      case Success(addresses) => complete(addresses.map(ae => ae.addressInfo))
      case Failure(throwable) =>
        complete(HttpResponse(StatusCodes.InternalServerError, entity = s"Internal error: $throwable"))
    }
  }

  @ApiOperation(
    value = "Create new address",
    nickname = "addressPost",
    httpMethod = "POST",
    produces = "application/json")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "address", dataType = "com.olx.iris.model.Address", paramType = "body", required = true)
  ))
  @ApiResponses(
    Array(
      new ApiResponse(code = 201, message = "Address created"),
      new ApiResponse(code = 409, message = "Address exists")
    ))
  def addressPost = post {
    entity(as[Address]) { address =>
      onSuccess(addressAggregate ? AddAddressCommand(address)) {
        case AddressAddedResponse(_) =>
          complete(HttpResponse(StatusCodes.Created, entity = s"Address w/ userId (${address.userId}) added"))
        case AddressExistsResponse(_) =>
          complete(HttpResponse(StatusCodes.Conflict, entity = s"Address w/ userId (${address.userId}) already exists"))
      }
    }
  }
}
