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

    val logicalW = 40
    val logicalH = 30

    val size = Vector2(logicalW, logicalH)
    val renderer = new SnakeRenderer("canvas", size)

    val ws = new WebSocket("ws://localhost:9000/ws")
    ws.onopen = (event: raw.Event) => {
    }
    ws.onclose = (event: CloseEvent) => {
      println(s"onclose ${event.reason}")
    }
    ws.onerror = (event: ErrorEvent) => {
      println("onerror")
    }

    val scores = org.scalajs.dom.document.getElementById("scores")
    ws.onmessage = (event: MessageEvent) => {
      val expr = event.data.asInstanceOf[String]
      val state = upickle.default.read[WorldState](expr)
      val playerScore = s"<h1>Score: ${state.player.score}</h1>"
      val otherScores = state.others.map(other => s"""<h1 style="color: orange">Score: ${other.score}</h1>""").mkString("")
      scores.innerHTML = playerScore + otherScores
      renderer.render(state)
    }

    document.addEventListener("keydown", (event: KeyboardEvent) => {
      handleArrows(event).foreach { direction =>
        val msg = upickle.default.write(NextDirection(direction))
        ws.send(msg)
      }
    })
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