package actors

import java.util.UUID

import akka.actor.{Actor, ActorRef, ActorSystem}
import shared.{Direction, Down, Left, Right, SnakeState, Up, Vector2, WorldState}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.{Duration, _}

case class SnakeClient(ref: ActorRef, snakeState: SnakeState)

case class RegisterClient(ref: ActorRef, clientId: UUID)
case class UnregisterClient(clientId: UUID)

case class ChangeDirection(direction: Direction, clientId: UUID)

class SnakeMasterActor(system: ActorSystem)(implicit val executionContext: ExecutionContext) extends Actor {
  val logicalW = 40
  val logicalH = 30

  var clients = Map.empty[UUID, SnakeClient]

  var applePosition = randomPosition()

  object Update

  private val schedule = system.scheduler.schedule(
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
        snakePosition = randomPosition(),
        snakeTail = Nil,
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
      state = handleBounds(logicalW, logicalH, state)
      state = handleApple(state)
      state = updateSnakeTail(snakeState.snakePosition, state)
      clientId -> client.copy(snakeState = state)
    }
    clients.values.foreach { client =>
      client.ref !  WorldState(
        player = client.snakeState,
        others = clients.values.filter(_ != client).map(_.snakeState).toList,
        applePosition = applePosition
      )
    }
  }

  def updateSnakeTail(initialPosition: Vector2, state: SnakeState): SnakeState = {
    val (snakeTail, _) = state.snakeTail.foldLeft((List.empty[Vector2], initialPosition)) {
      case ((newTail, oldPosition), node) => (newTail :+ oldPosition, node)
    }
    state.copy(snakeTail = snakeTail)
  }

  def handleApple(state: SnakeState): SnakeState = {
    val snakePosition = state.snakePosition
    if (snakePosition == applePosition) {
      applePosition = randomPosition()
      val snakeTail = state.snakeTail
      state.copy(
        snakeTail = snakeTail :+ snakeTail.lastOption.getOrElse(snakePosition),
        score = state.score + 1
      )
    } else {
      state
    }
  }

  def updateSnakePosition(state: SnakeState): SnakeState = {
    val position = state.snakePosition
    val (posX, posY) = (position.x, position.y)
    state.direction match {
      case Left => state.copy(snakePosition = Vector2(posX - 1, posY))
      case Right => state.copy(snakePosition = Vector2(posX + 1, posY))
      case Up => state.copy(snakePosition = Vector2(posX, posY - 1))
      case Down => state.copy(snakePosition = Vector2(posX, posY + 1))
    }
  }

  def handleBounds(w: Int, h: Int, state: SnakeState): SnakeState = {
    if (state.snakePosition.x < 0) {
      state.copy(snakePosition = Vector2(w - 1, state.snakePosition.y))
    } else if (state.snakePosition.x >= w) {
      state.copy(snakePosition = Vector2(0, state.snakePosition.y))
    } else if (state.snakePosition.y < 0) {
      state.copy(snakePosition = Vector2(state.snakePosition.x, h - 1))
    } else if (state.snakePosition.y >= h) {
      state.copy(snakePosition = Vector2(state.snakePosition.x, 0))
    } else state
  }
}
