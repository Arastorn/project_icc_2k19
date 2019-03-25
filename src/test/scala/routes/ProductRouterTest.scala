package routes

import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.testkit.TestActorRef
import akka.actor.ActorSystem
import spray.json._
import DefaultJsonProtocol._

import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.duration._

import actors.ProductRequestHandler
import messages.ProductMessages._
import models._


class ProductRouterTest extends WordSpec with Matchers with ScalatestRouteTest with ProductRouter {

  implicit def default(implicit system: ActorSystem) = RouteTestTimeout(5 seconds)
  //override def testConfigSource = "akka.loglevel = DEBUG"
  //override def config = testConfig

  val productRequestHandler: TestActorRef[ProductRequestHandler] = TestActorRef[ProductRequestHandler](new ProductRequestHandler())

  "A Product Router" should {

    "list products with no product" in {
      Get("/product") ~> route ~> check {
        status shouldBe StatusCodes.OK
        responseAs[Seq[Product]] shouldBe Nil
      }
    }

    "create a product Product(stylo,5)" in {
      Post("/product", ("""{"label": "stylo","price":5}""").parseJson) ~> route ~> check {
        var products: Seq[Product] = Nil :+ Product(1,"stylo",5)
        status shouldBe StatusCodes.OK
        responseAs[Seq[Product]] shouldBe  products
      }
    }

    "create a product Product(Voiture,5000)" in {
      Post("/product", ("""{"label": "Voiture","price":5000}""").parseJson) ~> route ~> check {
        var products: Seq[Product] = Nil :+ Product(1,"stylo",5) :+ Product(2,"Voiture",5000)
        status shouldBe StatusCodes.OK
        responseAs[Seq[Product]] shouldBe  products
      }
    }

    "list products with products" in {
      Get("/product") ~> route ~> check {
        var products: Seq[Product] = Nil :+ Product(1,"stylo",5) :+ Product(2,"Voiture",5000)
        status shouldBe StatusCodes.OK
        responseAs[Seq[Product]] shouldBe products
      }
    }

    "Get product with id = 1 in the list" in {
      Get("/product/1") ~> route ~> check {
        status shouldBe StatusCodes.OK
        var product: Product = Product(1,"stylo",5)
        responseAs[Product] shouldBe product
      }
    }


    "Change the label of product with id = 1 in the list" in {
      Put("/product/1/changeLabel", ("""{"label": "crayon","id":1}""").parseJson) ~> route ~> check {
        var product: Product = Product(1,"crayon",5)
        status shouldBe StatusCodes.OK
        responseAs[Product] shouldBe product
      }
    }

    "Change the Price of product with id = 1 in the list" in {
      Put("/product/1/changePrice", ("""{"price": 2,"id":1}""").parseJson) ~> route ~> check {
        var product: Product = Product(1,"crayon",2)
        status shouldBe StatusCodes.OK
        responseAs[Product] shouldBe product
      }
    }

    "Delete product with id = 1 in the list" in {
      Delete("/product/1") ~> route ~> check {
        var products: Seq[Product] = Nil :+ Product(2,"Voiture",5000)
        status shouldBe StatusCodes.OK
        responseAs[Seq[Product]] shouldBe products
      }
    }

  }
}
