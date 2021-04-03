package example

import org.scalatest._
import org.scalatest.funsuite._
import util.chaining._

@Ignore
class HelloSpec extends AnyFunSuite {
  test("") {
    assert(Hello.greeting == "hello")
  }
}
