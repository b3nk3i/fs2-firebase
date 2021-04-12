package org.benkei.akka.persistence.firestore.emulator

import cats.Eval
import com.dimafeng.testcontainers.{ForAllTestContainer, GenericContainer}
import com.google.api.gax.core.FixedCredentialsProvider
import com.google.cloud.firestore.{Firestore, FirestoreOptions}
import org.scalatest.flatspec.AnyFlatSpec
import org.testcontainers.containers.wait.strategy.Wait

trait FirestoreEmulator extends AnyFlatSpec with ForAllTestContainer {

  override val container: GenericContainer = FirestoreEmulator.firestoreContainer()

  def firestore: Eval[Firestore] =
    Eval.later {
      FirestoreOptions.newBuilder
        .setProjectId("project-id")
        .setEmulatorHost(s"${container.host}:${container.mappedPort(8080)}")
        .setCredentialsProvider(
          FixedCredentialsProvider.create(new FirestoreOptions.EmulatorCredentials)
        )
        .build()
        .getService
    }
}

object FirestoreEmulator {

  def firestoreContainer(): GenericContainer = {
    GenericContainer(
      dockerImage = "ridedott/firestore-emulator:1.11.12",
      exposedPorts = Seq(8080),
      waitStrategy = Wait.forHttp("/")
    )
  }
}
