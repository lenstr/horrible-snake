package omg.snake

import org.scalajs.dom._
import shared.{Vector2, WorldState}

import scala.collection.mutable
import scala.util.Random

class SnakeRenderer(canvasId: String, size: Vector2) {

  private[this] val canvas = document.getElementById(canvasId).asInstanceOf[html.Canvas]
  private[this] val pointSize = Vector2(canvas.width / size.x, canvas.height / size.y)
  private[this] val colors = mutable.Map.empty[Int, String]
  private[this] val brush = canvas.getContext("2d").asInstanceOf[CanvasRenderingContext2D]

  def clear(): Unit = brush.clearRect(0, 0, canvas.width, canvas.height)

  def renderSnake(head: Vector2, tail: List[Vector2], color: String): Unit = {
    brush.fillStyle = color
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

  def start(getState: () => WorldState): Unit = {
    def request(): Unit = {
      window.requestAnimationFrame { (_: Double) =>
        render(getState())
        request()
      }
    }
    request()
  }

  def render(state: WorldState): Unit = {
    clear()
    renderBorder()
    renderApple(state.applePosition)
    renderSnake(state.player.position, state.player.tail, "black")
    state.others.zipWithIndex.foreach { case (snake, i) =>
      val color = colors.getOrElseUpdate(i, {
        def c = Random.nextFloat() * 50 + 50
        s"rgb($c%, $c%, $c%)"
      })
      renderSnake(snake.position, snake.tail, color)
    }
  }
}
