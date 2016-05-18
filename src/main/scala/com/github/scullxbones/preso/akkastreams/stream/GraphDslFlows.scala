package com.github.scullxbones.preso.akkastreams.stream

import akka.Done
import akka.stream.FlowShape
import akka.stream.scaladsl._
import com.github.scullxbones.preso.akkastreams.App

import scala.concurrent.{Future, Promise}

object GraphDslFlows extends TickTock {
    import App._

    def sample[Mat1,Mat2](source: Source[String, Mat1], tap: Sink[Int, Mat2]): Flow[String, String, Mat2] = {
        Flow.fromGraph(GraphDSL.create(source, tap)(Keep.right) { implicit builder => (src,snk) =>
            import GraphDSL.Implicits._

            val bcast = builder.add(Broadcast[String](2))
            val len = builder.add(Flow[String].map(_.length))
            val merge = builder.add(Merge[String](2))

            bcast.out(0)    ~> len          ~> snk.in
            bcast.out(1)    ~> merge.in(0)
            src.out         ~> merge.in(1)

            FlowShape(bcast.in, merge.out)
        })
    }

    import collection.immutable.Seq
    def graph(contained: Seq[String], logToPage: String => Unit): Future[(Done,Int)] = {
        val feed = "ABC" :: "abc" :: "de" :: Nil
        val lengthPromise = Promise[Int]()
        val lengthCalc = Sink.fold[Int,Int](0)(_ + _)

        val done = tickTock(Source(feed))
            .viaMat(sample(tickTock(Source(contained)), lengthCalc))(Keep.right)
            .mapMaterializedValue(lengthPromise.completeWith)
            .toMat(Sink.foreach(logToPage))(Keep.right)
            .run()

        implicit val ec = nonblockingDispatcher
        for {
            d <- done
            l <- lengthPromise.future
        } yield (d,l)
    }

}
