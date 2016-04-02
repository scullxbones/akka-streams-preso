package com.github.scullxbones.preso.akkastreams.snippet

import akka.Done
import com.github.scullxbones.preso.akkastreams.comet.SampleComet.AddMessages
import com.github.scullxbones.preso.akkastreams.comet.SampleCometDriver
import com.github.scullxbones.preso.akkastreams.stream.GraphStageFlows
import com.github.scullxbones.preso.akkastreams.stream.GraphStageFlows.HashValue
import net.liftweb.common.{Box, Loggable}
import net.liftweb.http.LiftCometActor

import scala.concurrent.Future

object GraphStageSnippet extends SampleCometDriver[Done] with Loggable {
    import concurrent.ExecutionContext.Implicits.global

    override def name = "graph-stage"

    override def runStream(comet: Box[LiftCometActor]): Future[Done] = {
        val content = "abcdefghijklmnopqrstuvwxyz0123456789"
        val fut = GraphStageFlows.graphStage(content,
            s => comet.foreach(_ ! AddMessages("Received element..." :: Nil))
        )
        fut.onSuccess {
            case Some(HashValue(hash)) =>
                comet.foreach(_ ! AddMessages(s"Hash value of $hash" :: Nil))
            case None =>
                comet.foreach(_ ! AddMessages("No hash calculated" :: Nil))
        }
        fut.map(_ => Done)
    }
}
