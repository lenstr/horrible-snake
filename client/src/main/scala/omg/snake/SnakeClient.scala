package omg.snake

import org.scalajs.dom._
import org.scalajs.dom.raw.WebSocket
import shared.Formats._
import shared._

import scala.scalajs.js

object SnakeClient extends js.JSApp {

  def main(): Unit = {

    val logicalW = 40
    val logicalH = 30

    val size = Vector2(logicalW, logicalH)
    val renderer = new SnakeRenderer("canvas", size)

    val scores = document.getElementById("scores")
    val ws = new WebSocket(s"ws://${window.location.host}/ws")

    ws.onmessage = (event: MessageEvent) => {
      val data = event.data.asInstanceOf[String]
      val state = upickle.default.read[WorldState](data)
      val playerScore = s"<h1>Score: ${state.player.score}</h1>"
      val otherScores = state.others.map(other => s"""<h1 style="color: orange">Score: ${other.score}</h1>""").mkString("")
      scores.innerHTML = playerScore + otherScores
      renderer.render(state)
    }

    document.addEventListener("keydown", (event: KeyboardEvent) => {
      handleArrows(event).foreach { direction =>
        val msg = upickle.default.write(direction.value)
        ws.send(msg)
      }
    })
  }

  def handleArrows(event: KeyboardEvent): Option[Direction] = {
    event.key match {
      case "ArrowLeft" => Some(Left)
      case "ArrowRight" => Some(Right)
      case "ArrowUp" => Some(Up)
      case "ArrowDown" => Some(Down)
      case _ => None
    }
  }
}