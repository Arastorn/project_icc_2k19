package actors

import akka.actor.{Actor, ActorLogging, Props}
import spray.json._
import spray.json.DefaultJsonProtocol._

import models._
import messages.ProductMessages._

class ProductRequestHandler extends Actor with ActorLogging{

  var product: Product = Product(0,"Voiture",5000)
  var products: Seq[Product] = Nil
  var _nextId = 0

  private def nextId() : Int = {
    _nextId = _nextId + 1
    _nextId
  }

  def list: Seq[Product] = {
    products
  }

  def lookup(id: Int): Option[Product] = {
    list.find(_.id == id)
  }

  def create(label: String, price: Int): Int = {
    val generatedId: Int = nextId()
    val newProduct = Product(generatedId, label, price)
    products = products :+ newProduct
    generatedId
  }

  def changeLabel(id: Int, newLabel: String) : Option[Product] = {
    val (updatedProducts, optionProduct) =
      products.foldLeft((Seq.empty[Product], Option.empty[Product])) {
        case ((acc, _), product) if product.id == id =>
          val updatedProduct = product.copy(label = newLabel)
          (acc :+ updatedProduct, Some(updatedProduct))

        case ((acc, option), product) =>
          (acc :+ product, option)
      }
      products = updatedProducts
      optionProduct
  }

  def changePrice(id: Int, newPrice: Int): Option[Product] =
     {
      val (updatedProducts, optionProduct) =
        products.foldLeft((Seq.empty[Product], Option.empty[Product])) {
          case ((acc, _), product) if product.id == id =>
            val updatedProduct = product.copy(price = newPrice)
            (acc :+ updatedProduct, Some(updatedProduct))

          case ((acc, option), product) =>
            (acc :+ product, option)
        }
      products = updatedProducts
      optionProduct
    }

  def delete(id: Int): Option[Product] = {
    val (productsWithId, newProducts) = products.partition(_.id == id)
    products = newProducts
    productsWithId.headOption
  }

  private def parsePriceJson(json: JsValue): Int =
    json.asJsObject.getFields("price") match {
      case Seq(JsNumber(price)) =>
        price.toInt
      case _ =>
      throw new DeserializationException("price: Int expected")
    }

  private def parseLabelJson(json: JsValue): String =
    json.asJsObject.getFields("label") match {
      case Seq(JsString(label)) =>
        label
      case _ =>
      throw new DeserializationException("label: String expected")
    }

  private def parseJson(json: JsValue): (String, Int) =
    json.asJsObject.getFields("label","price") match {
      case Seq(JsString(label),JsNumber(price)) =>
        (label, price.toInt)
      case _ =>
      throw new DeserializationException("label: String and price: Int expected")
    }

  override def receive: Receive = {

    case GetProductsRequest =>
      println("Received GetProductsRequest")
      sender() ! ProductsResponse(list)

    case GetProductById(id) =>
      println("Received GetProductById")
      sender() ! ProductResponse(lookup(id))

    case DeleteProductById(id) =>
      println("Received DeleteProductById")
      delete(id)
      sender() ! ProductsResponse(list)

    case request: ChangeProductLabelRequest =>
      println("Received ChangeProductLabelRequest")
      val newLabel = parseLabelJson(request.label)
      sender() ! ProductResponse(changeLabel(request.id,newLabel))

    case request: ChangeProductPriceRequest =>
      println("Received ChangeProductPriceRequest")
      val newPrice = parsePriceJson(request.price)
      sender() ! ProductResponse(changePrice(request.id,newPrice))

    case request: AddProductRequest =>
      println("add a product to products list")
      val newProduct = parseJson(request.product)
      create(newProduct._1,newProduct._2)
      //products = products :+ request.product
      sender() ! ProductsResponse(list)
  }
}

object ProductRequestHandler {
  def props(): Props = {
    Props(classOf[ProductRequestHandler])
  }
}
