package org.benkei.firestore

import cats.effect.Async
import cats.effect.std.{Dispatcher, Queue}
import com.google.api.gax.rpc.ApiStreamObserver
import com.google.cloud.firestore.{DocumentSnapshot, Query}

object StreamOps {

  def stream[F[_]: Async](
    dispatcher: Dispatcher[F],
    query:      Query
  ): fs2.Stream[F, DocumentSnapshot] = {
    fs2.Stream
      .eval(Queue.unbounded[F, Option[Either[Throwable, DocumentSnapshot]]])
      .evalTap { queue =>
        F.delay {
          query.stream(new ApiStreamObserver[DocumentSnapshot] {
            override def onNext(doc: DocumentSnapshot): Unit =
              dispatcher.unsafeRunSync(queue.offer(Some(Right(doc))))

            override def onError(t: Throwable): Unit =
              dispatcher.unsafeRunSync(queue.offer(Some(Left(t))))

            override def onCompleted(): Unit =
              dispatcher.unsafeRunSync(queue.offer(None))
          })
        }
      }
      .flatMap(fs2.Stream.fromQueueNoneTerminated[F, Either[Throwable, DocumentSnapshot]](_))
      .evalMap {
        case Left(error)  => F.raiseError[DocumentSnapshot](error)
        case Right(value) => F.pure(value)
      }
  }
}
