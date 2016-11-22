package actors

import java.util.UUID

import akka.actor.{Actor, ActorRef}
import com.softwaremill.quicklens._
import shared.{Direction, Down, Left, Right, SnakeState, Up, Vector2, WorldState}

import scala.concurrent.duration.{Duration, _}

case class SnakeClient(ref: ActorRef, snakeState: SnakeState)

case class RegisterClient(ref: ActorRef, clientId: UUID)
case class UnregisterClient(clientId: UUID)

case class ChangeDirection(direction: Direction, clientId: UUID)

class SnakeMasterActor extends Actor {
  val logicalW = 40
  val logicalH = 30

  var clients = Map.empty[UUID, SnakeClient]

  var applePosition = randomPosition()

  object Update

  import SnakeUtils._
  import context._

  private val schedule = context.system.scheduler.schedule(
    initialDelay = Duration.Zero,
    interval = 50.millis,
    receiver = self,
    message = Update
  )

  override def postStop(): Unit = {
    schedule.cancel()
  }

  override def receive: Receive = {
    case RegisterClient(ref, clientId) =>
      val state = SnakeState(
        position = randomPosition(),
        tail = Nil,
        direction = Direction.random(),
        score = 0
      )
      clients += clientId -> SnakeClient(ref, state)
    case UnregisterClient(clientId) =>
      clients -= clientId
    case ChangeDirection(direction, clientId) =>
      clients ++= clients.get(clientId).map { client =>
        clientId -> client.modify(_.snakeState.direction).setTo(direction)
      }
    case Update =>
      update()
  }

  def randomPosition(): Vector2 = {
    val x = math.floor(math.random * logicalW)
    val y = math.floor(math.random * logicalH)
    Vector2(x, y)
  }

  def update(): Unit = {
    clients = clients.map { case (clientId, client) =>
      val snakeState = client.snakeState
      var state = updateSnakePosition(snakeState)
      state = state.copy(position = handleBounds(logicalW, logicalH, state.position))
      state = handleApple(state)
      state = updateSnakeTail(snakeState.position, state)
      clientId -> client.copy(snakeState = state)
    }
    clients.values.foreach { client =>
      client.ref ! WorldState(
        player = client.snakeState,
        others = clients.values.filter(_ != client).map(_.snakeState).toList,
        applePosition = applePosition
      )
    }
  }

  def handleApple(state: SnakeState): SnakeState = {
    val snakePosition = state.position
    if (snakePosition == applePosition) {
      applePosition = randomPosition()
      state
        .modify(_.tail).using(tail => tail :+ tail.lastOption.getOrElse(snakePosition))
        .modify(_.score).using(_ + 1)
    } else {
      state
    }
  }
}

object SnakeUtils {

  def updateSnakeTail(initialPosition: Vector2, state: SnakeState): SnakeState = {
    val (snakeTail, _) = state.tail.foldLeft((List.empty[Vector2], initialPosition)) {
      case ((newTail, oldPosition), node) => (newTail :+ oldPosition, node)
    }
    state.copy(tail = snakeTail)
  }

  def updateSnakePosition(state: SnakeState): SnakeState = {
    val position = state.position
    val (posX, posY) = (position.x, position.y)
    val newPosition = state.direction match {
      case Left => Vector2(posX - 1, posY)
      case Right => Vector2(posX + 1, posY)
      case Up => Vector2(posX, posY - 1)
      case Down => Vector2(posX, posY + 1)
    }
    state.copy(position = newPosition)
  }

  def handleBounds(w: Int, h: Int, snakePosition: Vector2): Vector2 = {
    var position = snakePosition
    if (position.x < 0) {
      position = Vector2(w - 1, position.y)
    }
    if (position.x >= w) {
      position = Vector2(0, position.y)
    }
    if (position.y < 0) {
      position = Vector2(position.x, h - 1)
    }
    if (position.y >= h) {
      position = Vector2(position.x, 0)
    }
    position
  }
}