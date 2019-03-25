package models

import spray.json._
import DefaultJsonProtocol._

final case class Product(
  id: Int,
  label: String,
  price: Int
)

object Product extends DefaultJsonProtocol {
  implicit val productFormat = jsonFormat3(Product.apply)
}
