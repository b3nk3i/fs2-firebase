package org.benkei.firestore

import cats.effect.{Async, Bracket, ConcurrentEffect, ContextShift, IO}
import com.google.api.gax.rpc.ApiStreamObserver
import com.google.cloud.firestore.{DocumentSnapshot, Query}
import fs2.concurrent.Queue
import cats.implicits._

object StreamOps {

  /*
  Based on fs2 guide https://fs2.io/guide.html#synchronous-effects,
  Integrate FS2 with query.stream based on Google ApiStreamObserver.
  > it requires ConcurrentEffect to run the Enqueue effect as ApiStreamObserver is expecting UNIT.
  > use Either to propagate the error back to Stream. (Error fails the stream)
  > unNoneTerminate halts the stream at the first `None`.
   */
  def stream[F[_]: Bracket[*[_], Throwable]: ConcurrentEffect: ContextShift](
    query: Query
  ): fs2.Stream[F, DocumentSnapshot] = {
    fs2.Stream
      .eval(Queue.unbounded[F, Either[Throwable, Option[DocumentSnapshot]]])
      .evalTap { queue =>
        F.delay {
          query.stream(new ApiStreamObserver[DocumentSnapshot] {
            override def onNext(doc: DocumentSnapshot): Unit =
              F.runAsync(queue.enqueue1(Right(Some(doc))))(_ => IO.unit)

            override def onError(t: Throwable): Unit =
              F.runAsync(queue.enqueue1(Left(t)))(_ => IO.unit)

            override def onCompleted(): Unit =
              F.runAsync(queue.enqueue1(Right(None)))(_ => IO.unit)
          })
        }
      }
      .flatMap(queue => queue.dequeue)
      .evalMap {
        case Left(error)  => F.raiseError[Option[DocumentSnapshot]](error)
        case Right(value) => F.pure(value)
      }
      .unNoneTerminate
  }
}
