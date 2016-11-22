package actors

import java.util.UUID

import akka.actor.{Actor, ActorRef}
import shared.{Direction, WorldState}

class SnakeSocketActor(
  out: ActorRef,
  master: ActorRef,
  clientId: UUID
)  extends Actor {

  override def preStart(): Unit = {
    master ! RegisterClient(self, clientId)
  }

  override def postStop(): Unit = {
    master ! UnregisterClient(clientId)
  }

  override def receive: Receive = {
    case direction: Direction =>
      master ! ChangeDirection(direction, clientId)
    case state: WorldState =>
      out ! state
  }
}


