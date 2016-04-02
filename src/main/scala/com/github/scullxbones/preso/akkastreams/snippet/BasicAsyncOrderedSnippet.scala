package com.github.scullxbones.preso.akkastreams.snippet

import akka.Done
import com.github.scullxbones.preso.akkastreams.comet.SampleComet.AddMessages
import com.github.scullxbones.preso.akkastreams.comet.SampleCometDriver
import com.github.scullxbones.preso.akkastreams.stream.MapAsyncFlows
import net.liftweb.common.{Box, Loggable}
import net.liftweb.http.LiftCometActor

import scala.concurrent.Future

object BasicAsyncOrderedSnippet extends SampleCometDriver[Done] with Loggable {
    override def name: String = "basic-async-ordered"

    override def runStream(comet: Box[LiftCometActor]): Future[Done] =
        MapAsyncFlows.basicOrdered(2, s => comet.foreach(_ ! AddMessages(s :: Nil)))
}

object BasicAsyncUnorderedSnippet extends SampleCometDriver[Done] with Loggable {
    override def name: String = "basic-async-unordered"

    override def runStream(comet: Box[LiftCometActor]): Future[Done] =
        MapAsyncFlows.basicUnordered(2, s => comet.foreach(_ ! AddMessages(s :: Nil)))
}

object FailingAsyncOrderedSnippet extends SampleCometDriver[Done] with Loggable {
    override def name: String = "failure-async-ordered"

    override def runStream(comet: Box[LiftCometActor]): Future[Done] =
        MapAsyncFlows.mapAsyncFailure(2, s => comet.foreach(_ ! AddMessages(s :: Nil)))
}

object SupervisedAsyncOrderedSnippet extends SampleCometDriver[Done] with Loggable {
    override def name: String = "supervised-async-ordered"

    override def runStream(comet: Box[LiftCometActor]): Future[Done] =
        MapAsyncFlows.supervisedMapAsyncFailure(2, s => comet.foreach(_ ! AddMessages(s :: Nil)))
}

