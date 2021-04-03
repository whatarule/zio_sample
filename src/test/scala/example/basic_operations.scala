import org.scalatest._
import org.scalatest.funsuite._
import util.chaining._
import cats.implicits._

import zio._

class basic_operations extends AnyFunSuite with example.io {
  test(":exec") {
    val succeed: UIO[Int] = IO.succeed(21).map(_ * 2); succeed.unsafeTry() pipe println
    val failed: IO[Exception, Unit] = IO.fail("no!").mapError(msg => new Exception(msg)); failed.unsafeTry()
    ZIO.fail("uh oh!").unsafeTry()
    Task.fail(new Exception("uh oh!")).unsafeTry()

    import scala.io.StdIn
    val getStrLn: Task[String] = ZIO.effect(StdIn.readLine()) //; getStrLn.unsafeTry() pipe println
    def putStrLn(line: String): UIO[Unit] = ZIO.effectTotal(println(line)) //; putStrLn("aaa").unsafeTry()
    val sequenced = getStrLn.flatMap(input => putStrLn(s"you entered: $input")) //; sequenced.unsafeTry()
    IO.fail(new Exception("failed flatMap")).flatMap(input => putStrLn(s"you entered: $input")).unsafeTry()

    val zipped: UIO[(String, Int)] = ZIO.succeed("aaa").zip(ZIO.succeed(2)); zipped.unsafeTry() pipe println
    val zipRight1 = putStrLn("what is your name?").zipRight(getStrLn) //; zipRight1.unsafeTry() pipe println
    val zipRight2 = putStrLn("what is your name?") *> getStrLn //; zipRight1.unsafeTry() pipe println
  }
}
