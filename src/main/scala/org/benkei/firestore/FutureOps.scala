package org.benkei.firestore

import cats.effect._
import cats.effect.implicits._
import cats.implicits._
import com.google.api.core.{ApiFuture, ApiFutureCallback, ApiFutures}

import scala.concurrent.ExecutionContextExecutor

/*
  Integration with google ApiFuture
 */
object FutureOps {

  implicit class ApiFutureOps[F[_]: Concurrent: ContextShift, A](fa: F[ApiFuture[A]])(implicit ec: ExecutionContextExecutor) {
    def futureLift: F[A] = liftApiFuture[F, A](fa)
  }

  def liftApiFuture[F[_]: Concurrent: ContextShift, A](fa: F[ApiFuture[A]])(implicit ec: ExecutionContextExecutor): F[A] = {
    val lifted: F[A] =
      fa.flatMap { future =>
        F.cancelable { cb =>
          ApiFutures.addCallback(future, new ApiFutureCallback[A] {
            override def onFailure(throwable: Throwable): Unit = cb(Left(throwable))
            override def onSuccess(result: A): Unit = cb(Right(result))
          }, ec)

          F.delay(future.cancel(true)).void
        }
      }
    lifted.guarantee(F.shift)
  }
}