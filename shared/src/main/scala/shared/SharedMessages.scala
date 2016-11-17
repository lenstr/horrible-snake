package shared

import scala.util.Random

case class Vector2(x: Double, y: Double)

case class NextDirection(direction: String)

sealed abstract class Direction
object Left extends Direction
object Right extends Direction
object Up extends Direction
object Down extends Direction

object Direction {
  def random() = Random.shuffle(Seq(Up, Down, Left, Right)).head
  def unapply(arg: String): Option[Direction] = arg.toLowerCase() match {
    case "up" => Some(Up)
    case "down" => Some(Down)
    case "left" => Some(Left)
    case "right" => Some(Right)
    case _ => None
  }
}

case class SnakeState(
  snakePosition: Vector2,
  snakeTail: List[Vector2],
  direction: Direction,
  score: Int
)

case class WorldState(
  player: SnakeState,
  others: List[SnakeState],
  applePosition: Vector2
)