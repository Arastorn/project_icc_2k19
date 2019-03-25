package routes

// packages
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import akka.actor.{Props, ActorLogging, Actor, ActorRef, ActorSystem}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpEntity, ContentTypes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json._

// Services
import services.products.actors.ProductRequestHandler
import services.products.messages.ProductMessages._
import services.products.models._

// routes

import routes.home.HomeRouter


import scala.concurrent.{ExecutionContextExecutor, Future}

trait Router extends HomeRouter{

  implicit val system: ActorSystem

  implicit val timeout: Timeout = 5.seconds

  def productRequestHandler: ActorRef

  def putInProductIdChangePrice(id : Int) : Route = {
    path("changePrice") { // /product/:id/changePrice
      put {
        entity(as[JsValue]) { productReport =>
          onSuccess(productRequestHandler ? ChangeProductPriceRequest(productReport,id)) {
            case response: ProductResponse =>
              complete(StatusCodes.OK, response.product)
            case _ =>
              complete(StatusCodes.InternalServerError)
          }
        }
      }
    }
  }

  def putInProductIdChangeLabel(id : Int) : Route = {
    path("changeLabel") { // /product/:id/changeName
      put {
        entity(as[JsValue]) { productReport =>
          onSuccess(productRequestHandler ? ChangeProductLabelRequest(productReport,id)) {
            case response: ProductResponse =>
              complete(StatusCodes.OK, response.product)
            case _ =>
              complete(StatusCodes.InternalServerError)
          }
        }
      }
    }
  }

  def putInProductId(id : Int) : Route = {
    putInProductIdChangePrice(id) ~
    putInProductIdChangeLabel(id)
  }

  def getProductById(id: Int) : Route = {
    get {
      onSuccess(productRequestHandler ? GetProductById(id))
      {
        case response: ProductResponse =>
          complete(StatusCodes.OK, response.product)
        case _ =>
          complete(StatusCodes.InternalServerError)
      }
    }
  }

  def deleteProductById(id: Int) : Route = {
    delete {
      onSuccess(productRequestHandler ? DeleteProductById(id))
      {
        case response: ProductsResponse =>
          complete(StatusCodes.OK, response.products)
        case _ =>
          complete(StatusCodes.InternalServerError)
      }
    }
  }

  def productId(id : Int) : Route = {
    pathEndOrSingleSlash{ // /product/:id/
      getProductById(id) ~
      deleteProductById(id)
    } ~
    putInProductId(id)
  }


  def getProducts: Route =
    get {
      onSuccess(productRequestHandler ? GetProductsRequest) {
        case response: ProductsResponse =>
          complete(StatusCodes.OK, response.products)
        case _ =>
          complete(StatusCodes.InternalServerError)
      }
    }

  def postProduct: Route =
    post {
      entity(as[JsValue]) { productReport =>
        onSuccess(productRequestHandler ? AddProductRequest(productReport)) {
          case response: ProductsResponse =>
            complete(StatusCodes.OK, response.products)
          case _ =>
            complete(StatusCodes.InternalServerError)
        }
      }
    }

  def product: Route =
    pathPrefix("product") { // the products
      pathEndOrSingleSlash { // /product or /product/
        getProducts ~
        postProduct
      } ~
      pathPrefix(IntNumber) {id => productId(id)}
    }



  def route: Route  =
    product ~
    welcomeOnApiPath

}
