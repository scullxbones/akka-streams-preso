package com.github.scullxbones.preso.akkastreams.stream

import akka.stream.scaladsl._
import com.github.scullxbones.preso.akkastreams.App

object SimpleFlows {
    import App._

    def simple(count: Int, fn: Int => Unit) = {
        Source(1 to count)
            .runWith(Sink.foreach(fn))
    }

    def mapAndFilter(count: Int, fn: String => Unit) = {
        Source(1 to count)
              .filter(_ % 2 == 0)
              .map("a" * _)
              .runWith(Sink.foreach(fn))
    }
}
