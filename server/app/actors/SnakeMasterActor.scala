package actors

import java.util.UUID

import akka.actor.{Actor, ActorRef}
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

  import context._
  import SnakeUtils._

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
      clients.get(clientId).foreach { client =>
        val snakeState = client.snakeState
        clients += clientId -> client.copy(snakeState = snakeState.copy(direction = direction))
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
      val snakeTail = state.tail
      val snakeState = state.copy(
        tail = snakeTail :+ snakeTail.lastOption.getOrElse(snakePosition),
        score = state.score + 1
      )
      snakeState
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
    state.direction match {
      case Left => state.copy(position = Vector2(posX - 1, posY))
      case Right => state.copy(position = Vector2(posX + 1, posY))
      case Up => state.copy(position = Vector2(posX, posY - 1))
      case Down => state.copy(position = Vector2(posX, posY + 1))
    }
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