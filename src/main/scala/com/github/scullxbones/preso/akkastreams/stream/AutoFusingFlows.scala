package com.github.scullxbones.preso.akkastreams.stream

import akka.stream.scaladsl._
import com.github.scullxbones.preso.akkastreams.App

object AutoFusingFlows {
    import App._

    def autoFused(count: Int, logToPage: String => Unit) = {
        Source(1 to count)
            .via(Flow[Int].map("abc " * _).async)
            .to(Sink.foreach(logToPage))
              .run()
    }

}
