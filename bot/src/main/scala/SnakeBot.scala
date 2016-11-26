import java.time.Instant

import shared.{Direction, Down, Left, Right, Up, Vector2, WorldState}

import scala.util.Random

class SnakeBot {

  import SnakeBot._

  private[this] var nextMoveTime = Instant.now()

  def getNextDirection(worldState: WorldState): Option[Direction] = {
    val now = Instant.now()
    if (now.compareTo(nextMoveTime) > 0) {
      val applePosition = worldState.applePosition
      val snakePosition = worldState.player.position
      chooseNextDirection(applePosition, snakePosition).collect {
        case direction if direction != worldState.player.direction =>
          nextMoveTime = now.plusMillis(100 + Random.nextInt(2000))
          direction
      }
    } else {
      None
    }
  }

}
object SnakeBot {
  def apply(): SnakeBot = new SnakeBot()

  def chooseNextDirection(applePosition: Vector2, snakePosition: Vector2): Option[Direction] = {
    if (snakePosition.x < applePosition.x) {
      Some(Right)
    } else if (snakePosition.x > applePosition.x) {
      Some(Left)
    } else if (snakePosition.y > applePosition.y) {
      Some(Up)
    } else if (snakePosition.y < applePosition.y) {
      Some(Down)
    } else {
      None
    }
  }

}