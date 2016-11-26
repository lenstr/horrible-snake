package shared

object SnakeUtils {

  import com.softwaremill.quicklens._

  def updateSnakeTail(initialPosition: Vector2, state: SnakeState): SnakeState = {
    val (snakeTail, _) = state.tail.foldLeft((List.empty[Vector2], initialPosition)) {
      case ((newTail, oldPosition), node) => (newTail :+ oldPosition, node)
    }
    state.copy(tail = snakeTail)
  }

  def updateSnakePosition(state: SnakeState): SnakeState = {
    state.direction match {
      case Left => state.modify(_.position.x).using(_ - 1)
      case Right => state.modify(_.position.x).using(_ + 1)
      case Up => state.modify(_.position.y).using(_ - 1)
      case Down => state.modify(_.position.y).using(_ + 1)
    }
  }

  def handleBounds(w: Int, h: Int, position: Vector2): Vector2 = {
    position.copy(
      x = handleBound(0, w, position.x),
      y = handleBound(0, h, position.y)
    )
  }

  @inline
  private[this] def handleBound(min: Int, max: Int, value: Double): Double = {
    if (value < min) {
      max - 1
    } else if (value >= max) {
      min
    } else {
      value
    }
  }
}
