package example

import util.chaining._
import zio._

trait io {
  implicit class RuntimeZ(r: Runtime[ZEnv]) {
    def unsafeTry[E, A](z: ZIO[ZEnv, E, A], i: Int = 2) =
      try r.unsafeRun(z)
      catch { case e: Throwable => e.toString.split("\n").lift(i).map(_ pipe println) }
  }
  implicit class ZIOZ[E, A](z: ZIO[ZEnv, E, A]) {
    def unsafeRun(implicit r: Runtime[ZEnv]) =
      r.unsafeRun(z)
    def unsafeTry(i: Int = 2)(implicit r: Runtime[ZEnv]) =
      r.unsafeTry(z, i)
  }

  implicit val runtime = Runtime.default
}
