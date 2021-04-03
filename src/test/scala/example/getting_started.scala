package example

import org.scalatest._
import org.scalatest.funsuite._
import util.chaining._

import zio._
import zio.console._

object MyApp extends zio.App {
  def run(args: List[String]) = myAppLogic.exitCode
  val myAppLogic = for {
    _ <- putStrLn("what is your name?")
    name <- getStrLn
    _ <- putStrLn(s"hello ${name}!")
  } yield ()
}

object IntegrationExample {
  val runtime = Runtime.default
  runtime.unsafeRun(Task(println("hello!")))
}

@Ignore
class getting_started extends AnyFunSuite {
  test("") {
    val runtime = Runtime.default
    //runtime.unsafeRun(MyApp.run(List()))
    IntegrationExample
    runtime.unsafeRun(putStr("hello!\n"))
  }
}
