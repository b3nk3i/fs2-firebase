package org.benkei.akka.persistence.firestore

import cats.effect.IO
import cats.effect.std.Dispatcher
import cats.effect.unsafe.implicits.global
import com.google.cloud.firestore.WriteResult
import org.benkei.akka.persistence.firestore.emulator.FirestoreEmulator
import org.benkei.firestore.StreamOps._
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}

import java.util
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import scala.jdk.CollectionConverters._

class StreamOpsSpec extends FirestoreEmulator with BeforeAndAfterEach with BeforeAndAfterAll {

  override def beforeEach(): Unit = {
    super.beforeEach()
  }

  override def afterAll(): Unit = {
    super.afterAll()
  }

  it should "stream elements" in {

    val batchSize = 400

    val db = firestore.value

    implicit val ec: ExecutionContextExecutor = ExecutionContext.global

    val insert: IO[util.List[WriteResult]] =
      IO {
        val batch = db.batch()

        List.tabulate(batchSize) { i =>
          batch.create(db.collection(s"test").document(s"$i"), Map("idx" -> i).asJava)
        }

        batch.commit().get()
      }

    (for {
      _               <- insert
      (dispatcher, _) <- Dispatcher[IO].allocated
      found           <- stream[IO](dispatcher, firestore.value.collection("test")).compile.toList

    } yield {
      found.size shouldBe batchSize
      found.map(_.getId).sorted shouldBe List.tabulate(400)(_.toString).sorted

    }).unsafeRunSync()
  }
}
