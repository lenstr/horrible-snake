package controllers

import java.util.UUID
import javax.inject.Inject

import actors.{SnakeMasterActor, SnakeSocketActor}
import akka.actor.{ActorSystem, Props}
import akka.stream.Materializer
import play.api.libs.streams.ActorFlow
import play.api.mvc.{Controller, WebSocket}
import shared._

class SnakeController @Inject()(
  actorSystem: ActorSystem,
  materializer: Materializer
) extends Controller {

  import models.Formats._

  lazy val master = actorSystem.actorOf(Props(new SnakeMasterActor()))

  def ws() = WebSocket.accept[Direction, WorldState] { rs =>
    val clientId = UUID.randomUUID()
    ActorFlow.actorRef(out => Props(new SnakeSocketActor(out, master, clientId)))(actorSystem, materializer)
  }

}
