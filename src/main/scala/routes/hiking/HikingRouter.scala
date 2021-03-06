package routes.hiking

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
import akka.http.scaladsl.model.HttpMethods._
import spray.json._

// Services
import services.hiking.actors.HikingRequestHandler
import services.hiking.messages.HikingMessages._
import services.hiking.models._

trait HikingRouter {

  implicit val timeout: Timeout = 5.seconds

  def hikingRequestHandler : ActorRef

  def getHikingById(id: Int) : Route = {
    get {
        onSuccess(hikingRequestHandler ? GetHikingById(id))
        {
          case response: HikingResponse =>
             complete(StatusCodes.OK, response.hiking)
          case response: HikingThrowServerError =>
            complete(StatusCodes.NotFound)
          case _ =>
            complete(StatusCodes.InternalServerError)
        }
      }
    }


  def hikingId(id : Int) : Route = {
    pathEndOrSingleSlash{
      getHikingById(id)
    }
  }


  def hiking: Route =
    pathPrefix("hiking") {
      pathPrefix(IntNumber) {id => hikingId(id)}
    }

}
