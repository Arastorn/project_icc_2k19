package routes

// packages
import akka.util.Timeout
import scala.concurrent.duration._
import akka.actor.{Props, ActorLogging, Actor, ActorRef, ActorSystem}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpEntity, ContentTypes}
import akka.http.scaladsl.server.Directives._
import scala.concurrent.{ExecutionContextExecutor, Future}

// Services
import services.products.actors.ProductRequestHandler
import services.products.messages.ProductMessages._
import services.products.models._

// routes
import routes.home.HomeRouter
import routes.products.ProductRouter
import routes.hiking.HikingRouter
import routes.extract.ExtractRouter

trait Router extends HomeRouter with ProductRouter with HikingRouter with ExtractRouter{

  override implicit val timeout: Timeout = 5.seconds
  implicit val system: ActorSystem

  def route: Route  =
    product ~
    hiking ~
    welcomeOnApiPathExtract ~
    welcomeOnApiPath

}
