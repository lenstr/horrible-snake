package shared

import scala.util.Random

case class Vector2(x: Double, y: Double)

sealed abstract class Direction(val value: String)
object Left extends Direction("left")
object Right extends Direction("right")
object Up extends Direction("up")
object Down extends Direction("down")

object Direction {
  def random() = Random.shuffle(Seq(Up, Down, Left, Right)).head
}

case class SnakeState(
  position: Vector2,
  tail: List[Vector2],
  direction: Direction,
  score: Int
)

case class WorldState(
  player: SnakeState,
  others: List[SnakeState],
  applePosition: Vector2
)