import org.scalatest._
import org.scalatest.funsuite._
import util.chaining._
import cats.implicits._

import zio._
import java.io._
import java.nio.file.Files

class handling_errors extends AnyFunSuite with example.io {
  test("") {
    val zeither: UIO[Either[String, Int]] = IO.fail("uh oh!").either; zeither.unsafeTry() pipe println
    ZIO.fromEither(Left("left")).unsafeTry()
    def sqrt(io: UIO[Double]): IO[String, Double] = ZIO.absolve(
      io.map(value =>
        if (value < 0.0) Left("value must be >= 0.0")
        else Right(Math.sqrt(value))
      )
    )
    sqrt(ZIO.succeed(2.0)).unsafeTry() pipe println
    sqrt(ZIO.succeed(-2.0)).unsafeTry()
  }

  import java.nio.charset.StandardCharsets.UTF_8
  lazy val DefaultData: Array[Byte] = Array(0, 0)
  def openFile(path: String): IO[IOException, Array[Byte]] = {
    def in(path: String) = ZIO.effect(new FileInputStream("src/test/resources/" + path)).refineToOrDie[IOException]
    def out(in: FileInputStream) = {
      var byte = new ByteArrayOutputStream(); var c = 0; var buff = new Array[Byte](1024)
      while ({ c = in.read(buff); c != -1 }) { byte.write(buff, 0, c) }; byte.toByteArray()
    }
    for {
      in <- in(path)
      out <- ZIO.succeed(out(in))
    } yield out
  }
  test("catch") {
    val z: IO[IOException, Array[Byte]] = openFile("primary.json").catchAll(_ => openFile("backup.json")); z.unsafeTry()
    val data: IO[IOException, Array[Byte]] = openFile("primary.json").catchSome { case _: FileNotFoundException => openFile("backup.json") }; data.unsafeTry()
    //val data: IO[IOException, Array[Byte]] = openFile("primary.json").catchSome { case _: NullPointerException => openFile("backup.json") }; data.unsafeTry()
    val primaryOrBackupData: IO[IOException, Array[Byte]] = openFile("primary.json").orElse(openFile("backup.json")); primaryOrBackupData.unsafeTry()
    openFile("primary.json").orElse(openFile("_backup.json")).unsafeTry().asInstanceOf[Array[Byte]] pipe (b => new String(b, UTF_8) pipe println)
  }
  test("fold") {
    val primaryOrDefaultData: UIO[Array[Byte]] = openFile("primary.json").fold(_ => DefaultData, data => data); primaryOrDefaultData.unsafeTry().asInstanceOf[Array[Byte]].mkString(", ") pipe println
    val primaryOrSecondaryData: IO[IOException, Array[Byte]] = openFile("primary.json").foldM(_ => openFile("secondary.json"), data => ZIO.succeed(data)); primaryOrSecondaryData.unsafeTry()
    trait Content
    case class ContentClass() extends Content
    case class NoContent(error: IOException) extends Content
    def content: Content = { new FileInputStream(""); new ContentClass() }
    def realUrls(path: String): IO[IOException, Content] = ZIO.effect(content).refineToOrDie[IOException]
    def fetchConent(c: Content): UIO[Content] = ZIO.succeed(c)
    val urls: UIO[Content] = realUrls("urls.json").foldM(error => IO.succeed(NoContent(error)), success => fetchConent(success)); urls.unsafeTry() pipe println
  }
  test("retry:exec") {
    import zio.clock._
    import zio.duration._
    val retriedOpenFile: ZIO[Clock, IOException, Array[Byte]] = openFile("primary.json").retry(Schedule.recurs(1) && Schedule.spaced(1.second)); retriedOpenFile.unsafeTry()
    openFile("primary.json").retryOrElse(Schedule.recurs(1) && Schedule.spaced(1.second), (_: IOException, _: (Long, Long)) => IO.succeed(DefaultData)).unsafeTry().asInstanceOf[Array[Byte]].mkString(", ") pipe println
    openFile("_backup.json").retryOrElseEither(Schedule.recurs(1), (_: IOException, _: Long) => IO.succeed(DefaultData)).unsafeTry().asInstanceOf[Either[String, Array[Byte]]].map(new String(_, UTF_8)) pipe println
    openFile("primary.json").retryOrElseEither(Schedule.recurs(1), (_: IOException, _: Long) => IO.succeed("left")).unsafeTry() pipe println
  }
}
