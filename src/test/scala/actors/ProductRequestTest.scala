package actors


import org.scalatest.{ BeforeAndAfterAll, FlatSpecLike, Matchers, WordSpecLike}
import akka.actor.{ Actor, Props, ActorSystem }
import akka.testkit.{ ImplicitSender, TestKit, TestActorRef, TestProbe }
import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json._
import DefaultJsonProtocol._

import scala.concurrent.duration._

import messages.ProductMessages._
import models._

class ProductRequestTest extends TestKit(ActorSystem("MySpec")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  val echo = system.actorOf(ProductRequestHandler.props)

  "A Product Request" must {

    "list products with no product" in {
      echo ! GetProductsRequest
      expectMsg(ProductsResponse(Nil))
    }

    "create a product Product(stylo,5)" in {
      val products: Seq[Product] = Nil :+ Product(1,"stylo",5)
      echo ! AddProductRequest(("""{"label": "stylo","price":5}""").parseJson)
      expectMsg(ProductsResponse(products))
    }

    "create a product Product(Voiture,5000)" in {
      val products: Seq[Product] = Nil :+ Product(1,"stylo",5) :+ Product(2,"Voiture",5000)
      echo ! AddProductRequest(("""{"label": "Voiture","price":5000}""").parseJson)
      expectMsg(ProductsResponse(products))
    }

    "list products with products" in {
      val products: Seq[Product] = Nil :+ Product(1,"stylo",5) :+ Product(2,"Voiture",5000)
      echo ! GetProductsRequest
      expectMsg(ProductsResponse(products))
      }

    "Get product with id = 1 in the list" in {
      var product: Option[Product] = Seq(Product(1,"stylo",5)).headOption
      echo ! GetProductById(1)
      expectMsg(ProductResponse(product))
    }

    "Change the label of product with id = 1 in the list" in {
      var product: Option[Product] = Seq(Product(1,"crayon",5)).headOption
      echo ! ChangeProductLabelRequest(("""{"label": "crayon"}""").parseJson,1)
      expectMsg(ProductResponse(product))
    }

    "Change the Price of product with id = 1 in the list" in {
      var product: Option[Product] = Seq(Product(1,"crayon",2)).headOption
      echo ! ChangeProductPriceRequest(("""{"price": 2}""").parseJson,1)
      expectMsg(ProductResponse(product))
    }

    "Delete product with id = 1 in the list" in {
      var products: Seq[Product] = Nil :+ Product(2,"Voiture",5000)
      echo ! DeleteProductById(1)
      expectMsg(ProductsResponse(products))
    }

  }
}
