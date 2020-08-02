package org.benkei.firestore

import cats.effect.{Async, Bracket, ConcurrentEffect, ContextShift, IO}
import com.google.api.gax.rpc.ApiStreamObserver
import com.google.cloud.firestore.{DocumentSnapshot, Query}
import fs2.concurrent.Queue

object StreamOps {

  type BracketThrowable[F[_]] = Bracket[F, Throwable]

  /*
  Based on fs2 guide https://fs2.io/guide.html#synchronous-effects,
  Integrate FS2 with query.stream based on Google ApiStreamObserver.
  > it requires ConcurrentEffect to run the Enqueue effect as ApiStreamObserver is expecting UNIT.
  > use Either to propagate the error back to Stream. (Error fails the stream)
  > unNoneTerminate halts the stream at the first `None`.
   */
  def stream[F[_]: BracketThrowable: ConcurrentEffect: ContextShift](
    query: Query
  ): fs2.Stream[F, DocumentSnapshot] = {
    fs2.Stream
      .eval(Queue.unbounded[F, Either[Throwable, Option[DocumentSnapshot]]])
      .evalTap { queue =>
        Async[F].delay {
          query.stream(new ApiStreamObserver[DocumentSnapshot] {
            override def onNext(doc: DocumentSnapshot): Unit =
              ConcurrentEffect[F]
                .runAsync(queue.enqueue1(Right(Some(doc))))(_ => IO.unit)

            override def onError(t: Throwable): Unit =
              ConcurrentEffect[F].runAsync(queue.enqueue1(Left(t)))(_ => IO.unit)

            override def onCompleted(): Unit =
              ConcurrentEffect[F]
                .runAsync(queue.enqueue1(Right(None)))(_ => IO.unit)
          })
        }
      }
      .flatMap(queue => queue.dequeue)
      .evalMap {
        case Left(error)  => Bracket[F, Throwable].raiseError[Option[DocumentSnapshot]](error)
        case Right(value) => Bracket[F, Throwable].pure(value)
      }
      .unNoneTerminate
  }
}
