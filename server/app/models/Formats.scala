package models

import play.api.libs.json._
import play.api.mvc.WebSocket.MessageFlowTransformer
import shared.{Direction, Down, Left, Right, SnakeState, Up, Vector2, WorldState}

/**
  * Created by lenstr on 11/18/2016.
  */
object Formats {
  implicit val directionFormat = new Format[Direction] {
    override def writes(o: Direction): JsValue = JsString(o.value)
    override def reads(json: JsValue): JsResult[Direction] = json match {
      case JsString(Up.value) => JsSuccess(Up)
      case JsString(Down.value) => JsSuccess(Down)
      case JsString(Left.value) => JsSuccess(Left)
      case JsString(Right.value) => JsSuccess(Right)
      case _ => JsError(s"invalid direction ${json}")
    }
  }

  implicit val vector2Format = Json.writes[Vector2]
  implicit val snakeStateFormat = Json.writes[SnakeState]
  implicit val stateFormat = Json.writes[WorldState]
  implicit val transformer = MessageFlowTransformer.jsonMessageFlowTransformer[Direction, WorldState]

}
