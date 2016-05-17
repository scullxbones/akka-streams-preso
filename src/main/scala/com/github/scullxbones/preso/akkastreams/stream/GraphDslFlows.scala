package com.github.scullxbones.preso.akkastreams.stream

import akka.NotUsed
import akka.stream.FlowShape
import akka.stream.scaladsl._
import com.github.scullxbones.preso.akkastreams.App

object GraphDslFlows extends TickTock {
    import App._

    def sample(source: Source[String, NotUsed]): Flow[String, String, NotUsed] = {
        Flow.fromGraph(GraphDSL.create(source) { implicit builder => src =>
            import GraphDSL.Implicits._

            val zipWith = builder.add(UnzipWith[String, String, String](s => s.take(2) -> s.drop(2)))
            val merge = builder.add(MergePreferred[String](2))

            zipWith.out0    ~> merge.in(0)
            zipWith.out1    ~> merge.in(1)
            src.out         ~> merge.preferred

            FlowShape(zipWith.in, merge.out)
        })
    }

    import collection.immutable.Seq
    def graph(contained: Seq[String], logToPage: String => Unit) = {
        val feed = "ABC" :: "abc" :: "de" :: Nil

        tickTock(Source(feed))
            .via(sample(tickTock(Source(contained))))
            .runWith(Sink.foreach(logToPage))
    }

}
