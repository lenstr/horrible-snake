package actors

import java.util.UUID

import akka.actor.{Actor, ActorRef, ActorSystem}
import shared.{Direction, NextDirection, WorldState}

import scala.concurrent.ExecutionContext

class SnakeSocketActor(
  out: ActorRef,
  master: ActorRef,
  clientId: UUID
)(implicit val system: ActorSystem, executionContext: ExecutionContext)
  extends Actor {

  override def preStart(): Unit = {
    master ! RegisterClient(self, clientId)
  }

  override def postStop(): Unit = {
    master ! UnregisterClient(clientId)
  }

  override def receive: Receive = {
    case NextDirection(Direction(direction)) =>
      master ! ChangeDirection(direction, clientId)
    case state: WorldState =>
      out ! state
  }
}


