package com.github.scullxbones.preso.akkastreams.stream

import akka.stream.SourceShape
import akka.stream.scaladsl._

trait TickTock {
    import concurrent.duration._

    def tickTock[A,Mat](source: Source[A,Mat]): Source[A,Mat] = {
        Source.fromGraph(GraphDSL.create(source) { implicit builder => src =>
            import GraphDSL.Implicits._

            val tick = builder.add(Source.tick(0.millis, 500.millis, 'tick))
            val zip = builder.add(Zip[A,Symbol]())
            val trim = builder.add(Flow[(A,Symbol)].map(_._1))

            src  ~> zip.in0
            tick ~> zip.in1
                    zip.out ~> trim.in

            SourceShape(trim.out)
        })
    }

}
