package omg.snake

import org.scalajs.dom._
import org.scalajs.dom.raw.WebSocket
import shared._
import upickle.Js
import upickle.Js.Value

import scala.scalajs.js

object SnakeClient extends js.JSApp {

  implicit val directionReader = new upickle.default.Reader[Direction] {
    override def read0: PartialFunction[Value, Direction] = {
      case Js.Str("up") => Up
      case Js.Str("down") => Down
      case Js.Str("left") => Left
      case Js.Str("right") => Right
    }
  }

  def main(): Unit = {

    val ws = new WebSocket("ws://localhost:9000/ws")
    ws.onopen = (event: raw.Event) => {

    }
    ws.onclose = (event: CloseEvent) => {
      println(s"onclose ${event.reason}")
    }
    ws.onerror = (event: ErrorEvent) => {
      println("onerror")
    }

    val logicalW = 40
    val logicalH = 30

    val size = Vector2(logicalW, logicalH)
    val renderer = new SnakeRenderer("canvas", size)

    def randomPosition(): Vector2 = {
      val x = math.floor(math.random * logicalW)
      val y = math.floor(math.random * logicalH)
      Vector2(x, y)
    }

    var state = WorldState(
      SnakeState(
        snakePosition = randomPosition(),
        snakeTail = Nil,
        direction = Direction.random(),
        score = 0
      ),
      applePosition = randomPosition()
    )

    ws.onmessage = (event: MessageEvent) => {
      val expr = event.data.asInstanceOf[String]
      state = upickle.default.read[WorldState](expr)
      println(state)
    }

    document.addEventListener("keydown", (event: KeyboardEvent) => {
      handleArrows(event).foreach { direction =>
        val msg = upickle.default.write(NextDirection(direction))
        ws.send(msg)
      }
    })

    renderer.start(getState = () => state)
  }

  def handleArrows(event: KeyboardEvent): Option[String] = {
    event.key match {
      case "ArrowLeft" => Some("left")
      case "ArrowRight" => Some("right")
      case "ArrowUp" => Some("up")
      case "ArrowDown" => Some("down")
      case _ => None
    }
  }
}