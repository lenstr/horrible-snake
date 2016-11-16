package omg.snake

import org.scalajs.dom.{KeyboardEvent, document}

import scala.scalajs.js
import scala.util.Random

case class Vector2(x: Double, y: Double)

sealed abstract class Direction
object Left extends Direction
object Right extends Direction
object Up extends Direction
object Down extends Direction

case class State(
  snakePosition: Vector2,
  snakeTail: List[Vector2],
  snakeDirection: Direction,
  applePosition: Vector2
)

object ScalaJSExample extends js.JSApp {

  def main(): Unit = {
    val logicalW = 40
    val logicalH = 30

    val size = Vector2(logicalW, logicalH)
    val renderer1 = new SnakeRenderer("canvas1", size)
    val renderer2 = new SnakeRenderer("canvas2", size)

    def randomPosition(): Vector2 = {
      val x = math.floor(math.random * logicalW)
      val y = math.floor(math.random * logicalH)
      Vector2(x, y)
    }

    var state = State(
      snakePosition = randomPosition(),
      snakeTail = Nil,
      snakeDirection = Random.shuffle(Seq(Up, Down, Left, Right)).head,
      applePosition = randomPosition()
    )

    def update(): Unit = {
      val initialState = state
      state = updateSnakePosition(state)
      state = handleBounds(logicalW, logicalH, state)
      state = handleApple(randomPosition, state)
      state = updateSnakeTail(initialState, state)
    }

    js.timers.setInterval(50)(update())

    document.addEventListener("keydown", (event: KeyboardEvent) => {
      state = handleArrows(event, state)
    })

    renderer1.start(getState = () => state)
    renderer2.start(getState = () => state)
  }

  def updateSnakeTail(initialState: State, state: State): State = {
    val (snakeTail, _) = state.snakeTail.foldLeft((List.empty[Vector2], initialState.snakePosition)) {
      case ((newTail, oldPosition), node) => (newTail :+ oldPosition, node)
    }
    state.copy(snakeTail = snakeTail)
  }

  def handleApple(randomPosition: () => Vector2, state: State): State = {
    val snakePosition = state.snakePosition
    if (snakePosition == state.applePosition) {
      val snakeTail = state.snakeTail
      state.copy(applePosition = randomPosition(), snakeTail = snakeTail :+ snakeTail.lastOption.getOrElse(snakePosition))
    } else {
      state
    }
  }

  def updateSnakePosition(state: State): State = {
    val position = state.snakePosition
    val (posX, posY) = (position.x, position.y)
    state.snakeDirection match {
      case Left => state.copy(snakePosition = Vector2(posX - 1, posY))
      case Right => state.copy(snakePosition = Vector2(posX + 1, posY))
      case Up => state.copy(snakePosition = Vector2(posX, posY - 1))
      case Down => state.copy(snakePosition = Vector2(posX, posY + 1))
    }
  }

  def handleBounds(w: Int, h: Int, state: State): State = {
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

  def handleArrows(event: KeyboardEvent, state: State): State = {
    event.key match {
      case "ArrowLeft" => state.copy(snakeDirection = Left)
      case "ArrowRight" => state.copy(snakeDirection = Right)
      case "ArrowUp" => state.copy(snakeDirection = Up)
      case "ArrowDown" => state.copy(snakeDirection = Down)
      case _ => state
    }
  }
}