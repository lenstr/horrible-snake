import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import shared.{SnakeUtils, Vector2}

@RunWith(classOf[JUnitRunner])
class SnakeSpec extends Specification {

  "Snake" should {
    "not leave game boundaries" in {
      val w = 10
      val h = 10

      def handleBounds(snakePosition: Vector2): Vector2 = {
        SnakeUtils.handleBounds(w, h, snakePosition)
      }

      handleBounds(Vector2(w, 0)) should be equalTo Vector2(0, 0)
      handleBounds(Vector2(0, h)) should be equalTo Vector2(0, 0)
      handleBounds(Vector2(w, h)) should be equalTo Vector2(0, 0)
      handleBounds(Vector2(0, 0)) should be equalTo Vector2(0, 0)

      handleBounds(Vector2(w - 1, 0)) should be equalTo Vector2(w - 1, 0)
      handleBounds(Vector2(0, h - 1)) should be equalTo Vector2(0, h - 1)
      handleBounds(Vector2(w - 1, h)) should be equalTo Vector2(w - 1, 0)
      handleBounds(Vector2(w, h - 1)) should be equalTo Vector2(0, h - 1)
    }
  }

}
