package messages

import models._
import akka.actor._
import spray.json._

object ProductMessages {

  case class ProductsResponse(products: Seq[Product])
  case class ProductResponse(product : Option[Product])
  case class AddProductRequest(product: JsValue)
  case class ChangeProductLabelRequest(label: JsValue, id : Int)
  case class ChangeProductPriceRequest(price: JsValue, id : Int)
  case class DeleteProductById(id: Int)
  case class GetProductById(id:  Int)
  object GetProductsRequest

}
