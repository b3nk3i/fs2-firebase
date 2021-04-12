package org.benkei.firestore

import cats.effect._
import cats.implicits._
import com.google.api.core.{ApiFuture, ApiFutureCallback, ApiFutures}

import scala.concurrent.ExecutionContextExecutor

/*
  Integration with google ApiFuture
 */
object FutureOps {

  implicit class ApiFutureOps[F[_]: Async, A](fa: F[ApiFuture[A]])(implicit
    ec: ExecutionContextExecutor
  ) {
    def futureLift: F[A] = fromApiFuture[F, A](fa)
  }

  /**
    * Suspend a [[com.google.api.core.ApiFuture]] into the `F[_]`
    * context.
    *
   * @param fa The [[com.google.api.core.ApiFuture]] to
    * suspend in `F[_]`
    */
  def fromApiFuture[F[_]: Async, A](
    fa:          F[ApiFuture[A]]
  )(implicit ec: ExecutionContextExecutor): F[A] =
    fa.flatMap { future =>
      F.async[A] { cb =>
        F.delay {
          ApiFutures.addCallback(
            future,
            new ApiFutureCallback[A] {
              override def onFailure(throwable: Throwable): Unit = cb(Left(throwable))
              override def onSuccess(result:    A):         Unit = cb(Right(result))
            },
            ec
          )
        }
        F.delay(Some(F.void(F.delay(future.cancel(true)))))
      }
    }
}