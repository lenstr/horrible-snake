package controllers

import java.util.UUID
import javax.inject.Inject

import actors.{SnakeMasterActor, SnakeSocketActor}
import akka.actor.{ActorSystem, Props}
import akka.stream.Materializer
import play.api.libs.json._
import play.api.libs.streams.ActorFlow
import play.api.mvc.WebSocket.MessageFlowTransformer
import play.api.mvc.{Controller, WebSocket}
import shared.{Direction, Down, Left, NextDirection, Right, SnakeState, Up, Vector2, WorldState}

import scala.concurrent.ExecutionContext

class SnakeController @Inject()(
  implicit val actorSystem: ActorSystem,
  materializer: Materializer,
  executionContext: ExecutionContext
) extends Controller {

  implicit val directionFormat = new Writes[Direction] {
    override def writes(o: Direction): JsValue = {
      val s = o match {
        case Left => "left"
        case Right => "right"
        case Up => "up"
        case Down => "down"
      }
      JsString(s)
    }
  }
  implicit val vector2Format = Json.writes[Vector2]
  implicit val snakeStateFormat = Json.writes[SnakeState]
  implicit val stateFormat = Json.writes[WorldState]
  implicit val directoinFormat = Json.reads[NextDirection]
  implicit val transformer = MessageFlowTransformer.jsonMessageFlowTransformer[NextDirection, WorldState]

  lazy val master = actorSystem.actorOf(Props(new SnakeMasterActor(actorSystem)))

  def ws() = WebSocket.accept[NextDirection, WorldState] { rs =>
    val clientId = UUID.randomUUID()
    ActorFlow.actorRef(out => Props(new SnakeSocketActor(out, master, clientId)))
  }

}
