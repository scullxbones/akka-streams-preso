package com.github.scullxbones.preso.akkastreams.snippet

import akka.Done
import com.github.scullxbones.preso.akkastreams.comet.SampleComet.AddMessages
import com.github.scullxbones.preso.akkastreams.comet.SampleCometDriver
import com.github.scullxbones.preso.akkastreams.stream.GraphDslFlows
import net.liftweb.common.{Box, Loggable}
import net.liftweb.http.LiftCometActor

import scala.concurrent.Future

object GraphDslSnippet extends SampleCometDriver[Done] with Loggable {
    override def name: String = "graph-dsl"

    override def runStream(comet: Box[LiftCometActor]): Future[Done] = {
        val contained = "!!!" :: "@@@" :: Nil
        GraphDslFlows.graph(contained,
            s => comet.foreach(_ ! AddMessages(s :: Nil))
        )
    }
}
