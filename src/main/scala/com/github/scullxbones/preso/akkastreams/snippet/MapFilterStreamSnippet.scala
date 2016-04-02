package com.github.scullxbones.preso.akkastreams.snippet

import akka.Done
import com.github.scullxbones.preso.akkastreams.comet.SampleComet.AddMessages
import com.github.scullxbones.preso.akkastreams.comet.SampleCometDriver
import com.github.scullxbones.preso.akkastreams.stream.SimpleFlows
import net.liftweb.common.{Box, Loggable}
import net.liftweb.http.LiftCometActor

import scala.concurrent.Future

object MapFilterStreamSnippet extends SampleCometDriver[Done] with Loggable {
    override def name: String = "map-filter"

    override def runStream(comet: Box[LiftCometActor]): Future[Done] = {
        SimpleFlows.mapAndFilter(10,
            s => comet.foreach(_ ! AddMessages(s :: Nil))
        )
    }
}
