package com.github.scullxbones.preso.akkastreams

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

object App {

    implicit val actorSystem = ActorSystem("akka-streams-preso")
    implicit val materializer = ActorMaterializer()

    def blockingDispatcher = actorSystem.dispatchers.lookup("blocking-dispatcher")
}
