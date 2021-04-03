import org.scalatest._
import org.scalatest.funsuite._
import util.chaining._
import cats.implicits._

import zio._

class creating_effects extends AnyFunSuite with example.io {
  test("succeed") {
    val s1 = ZIO.succeed(42); s1.unsafeTry() pipe println
    val s2: Task[Int] = Task.succeed(42); s2.unsafeTry() pipe println
    val now = ZIO.effectTotal(System.currentTimeMillis()); now.unsafeTry() pipe println
  }

  test("fail") {
    val f1 = ZIO.fail("uh oh!"); f1.unsafeTry()
    val f2 = Task.fail(new Exception("uh oh!")); f2.unsafeTry()
  }

  test("option") {
    val zopt: IO[Option[Nothing], Int] = ZIO.fromOption(1.some); zopt.unsafeTry() pipe println
    val zopt2: IO[String, Int] = ZIO.fromOption(2.some).mapError(_ => "no value"); zopt2.unsafeTry() pipe println
    val _zopt2: IO[String, Int] = ZIO.fromOption(None).mapError(_ => "no value"); _zopt2.unsafeTry()

    case class User(id: String, teamId: String = "")
    case class Team(id: String)
    def getUser(userId: String): IO[Throwable, Option[User]] = ZIO.succeed(new User(userId).some)
    def getTeam(teamId: String): IO[Throwable, Team] = ZIO.succeed(new Team(teamId))
    val maybeId: IO[Option[Nothing], String] = ZIO.fromOption("id".some)
    val result: IO[Throwable, Option[(User, Team)]] = (for {
      id <- maybeId
      user <- getUser(id).some
      team <- getTeam(user.teamId).asSomeError
    } yield (user, team)).optional
    result.unsafeTry() pipe println

    val zeitr = ZIO.fromEither(Right(1)); zeitr.unsafeTry() pipe println
    val zeitl = ZIO.fromEither(Left("left")); zeitl.unsafeTry()
    import scala.util.Try
    val ztry = ZIO.fromTry(Try(42 / 0)); ztry.unsafeTry()
    val zfun: URIO[Int, Int] = ZIO.fromFunction((i: Int) => i * i) // ??
    import scala.concurrent.Future
    lazy val future = Future.successful("hello!")
    val zfuture: Task[String] = ZIO.fromFuture { implicit ec => future.map(_ => "bye!") }
    zfuture.unsafeTry() pipe println
  }

  test("sync") {
    import scala.io.StdIn
    val getStrLn: Task[String] = ZIO.effect(StdIn.readLine()) //; getStrLn.unsafeTry() pipe println
    def putStrLn(line: String): UIO[Unit] = ZIO.effectTotal(println(line)) //; putStrLn("aaa").unsafeTry()
    import java.io.IOException
    val getStrLn2: IO[IOException, String] = ZIO.effect(StdIn.readLine()).refineToOrDie[IOException] //; getStrLn2.unsafeTry() pipe println
    val result: IO[Throwable, String] = for {
      str <- getStrLn
      str2 <- getStrLn2
      _ <- putStrLn(str + str2)
    } yield (str + str2)
    //result.unsafeTry() pipe println
  }
  test("async") {
    case class User()
    case class AuthError()
    object legacy {
      def login(onSuccess: User => Unit, onFailure: AuthError => Unit): Unit = ()
    }
    val login: IO[AuthError, User] = IO.effectAsync[AuthError, User] { callback =>
      legacy.login(user => callback(IO.succeed(user)), err => callback(IO.fail(err)))
    } //; login.unsafeTry() ??
  }
  test("blocking") {
    import zio.blocking._
    val sleeping = effectBlocking(Thread.sleep(3000)); sleeping.unsafeTry()
    "blocked!" pipe println
  }
}
