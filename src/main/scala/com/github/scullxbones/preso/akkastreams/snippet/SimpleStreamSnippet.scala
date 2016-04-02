package com.github.scullxbones.preso.akkastreams.snippet

import akka.Done
import com.github.scullxbones.preso.akkastreams.comet.{SampleComet, SampleCometDriver}
import com.github.scullxbones.preso.akkastreams.stream.SimpleFlows
import net.liftweb.common.{Box, Loggable}
import net.liftweb.http._

object SimpleStreamSnippet extends SampleCometDriver[Done] with Loggable {
    import SampleComet._

    override def name = "simple"

    override def runStream(comet: Box[LiftCometActor]) =
        SimpleFlows.simple(5,
            i => comet.foreach(_ ! AddMessages(i.toString :: Nil))
        )
}
