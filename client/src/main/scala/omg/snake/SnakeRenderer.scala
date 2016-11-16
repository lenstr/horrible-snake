package omg.snake

import org.scalajs.dom
import org.scalajs.dom.{html, _}

/**
  * Created by lenstr on 11/17/2016.
  */
class SnakeRenderer(canvasId: String, size: Vector2) {

  private[this] val canvas = document.getElementById(canvasId).asInstanceOf[html.Canvas]
  private[this] val brush = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
  private[this] val pointSize = Vector2(canvas.width / size.x, canvas.height / size.y)

  def clear() = brush.clearRect(0, 0, canvas.width, canvas.height)

  def renderSnake(head: Vector2, tail: List[Vector2]): Unit = {
    brush.fillStyle = "black"
    (head +: tail).foreach { tail =>
      brush.fillRect(tail.x * pointSize.x, tail.y * pointSize.y, pointSize.x, pointSize.y)
    }
  }

  def renderApple(position: Vector2): Unit = {
    brush.fillStyle = "red"
    brush.fillRect(position.x * pointSize.x, position.y * pointSize.y, pointSize.x, pointSize.y)
  }

  def renderBorder(): Unit = {
    brush.strokeStyle = "black"
    brush.strokeRect(0, 0, canvas.width, canvas.height)
  }

  def start(getState: () => State): Unit = {
    def request(): Unit = {
      dom.window.requestAnimationFrame { (_: Double) =>
        render(getState())
        request()
      }
    }
    request()
  }

  def render(state: State): Unit = {
    clear()
    renderBorder()
    renderApple(state.applePosition)
    renderSnake(state.snakePosition, state.snakeTail)
  }
}
