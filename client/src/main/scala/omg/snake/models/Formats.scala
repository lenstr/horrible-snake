package omg.snake.models

import shared.{Direction, Down, Left, Right, Up}
import upickle.Js

/**
  * Created by lenstr on 11/18/2016.
  */
object Formats {

  case class Color(a: Int, b: Int, c: Int)

  val x = upickle.default.write(Color(1, 2, 3))

  implicit val directionReader = new upickle.default.Reader[Direction] {
    override def read0 = {
      case Js.Str(Up.value) => Up
      case Js.Str(Down.value) => Down
      case Js.Str(Left.value) => Left
      case Js.Str(Right.value) => Right
    }
  }
}
