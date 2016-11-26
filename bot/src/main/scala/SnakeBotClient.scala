import com.github.andyglow.websocket._
import shared.Formats._
import shared._
import upickle.{default => upickle}

object SnakeBotClient {
  object Logger {
    def info(msg: String): Unit = println(msg)
  }

  val HOST = "localhost:9000"
//  val HOST = "lenstr.io:9001"

  def main(args: Array[String]): Unit = {
    def makeProtocolHandler(snakeBot: SnakeBot) = new WebsocketHandler[String]() {
      def receive: PartialFunction[String, Unit] = {
        case str =>
          val state = upickle.read[WorldState](str)
          snakeBot.getNextDirection(state).foreach { nextDirection =>
            sender() ! upickle.write(nextDirection.value)
          }
      }
    }

    (1 to 5).foreach { _ =>
      val bot = SnakeBot()
      val handler = makeProtocolHandler(bot)
      WebsocketClient(Uri("ws://" + HOST + "/ws"), handler).open()
    }
  }
}

