package shared

import upickle.{Js, default => upickle}

object Formats {
  implicit val directionReader = new upickle.Reader[Direction] {
    override def read0: PartialFunction[Js.Value, Direction] = {
      case Js.Str(Up.value) => Up
      case Js.Str(Down.value) => Down
      case Js.Str(Left.value) => Left
      case Js.Str(Right.value) => Right
    }
  }
}
