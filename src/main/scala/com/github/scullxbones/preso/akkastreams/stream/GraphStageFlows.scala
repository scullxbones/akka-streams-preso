package com.github.scullxbones.preso.akkastreams.stream

import akka.stream.scaladsl._
import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import akka.util.ByteString
import com.github.scullxbones.preso.akkastreams.App

import scala.annotation.tailrec
import scala.concurrent.{Future, Promise}
import scala.util.hashing.MurmurHash3.{bytesHash, mix}

import scala.collection.immutable.Seq

object GraphStageFlows extends TickTock {
    import App._

    private def splitPairs(content: String): Seq[String] = {
        @tailrec
        def _splitPairs(remaining: String, soFar: Seq[String]): Seq[String] = {
            if (remaining.length <= 2) soFar :+ remaining
            else _splitPairs(remaining.drop(2), soFar :+ remaining.take(2))
        }
        _splitPairs(content, Seq.empty[String])
    }

    def graphStage(content: String, fn: ByteString => Unit) = {
        tickTock(Source(splitPairs(content)))
            .via(Flow[String].map(ByteString.apply))
            .viaMat(Flow.fromGraph(new MurmurHasher()))(Keep.right)
            .toMat(Sink.foreach(fn))(Keep.left)
            .run()
    }

    case class HashValue(value: Int)

    // Adapted from akka cookbook
    import akka.stream.stage._
    class MurmurHasher extends GraphStageWithMaterializedValue[FlowShape[ByteString, ByteString], Future[Option[HashValue]]] {
        val in = Inlet[ByteString]("Hasher.in")
        val out = Outlet[ByteString]("Hasher.out")
        override val shape = FlowShape.of(in, out)

        override def createLogicAndMaterializedValue(inheritedAttributes: Attributes): (GraphStageLogic, Future[Option[HashValue]]) = {
            val mat = Promise[Option[HashValue]]()
            val logic = new GraphStageLogic(shape) {
                var hashSoFar: Option[Int] = None

                setHandler(out, new OutHandler {
                    override def onPull() = {
                        pull(in)
                    }
                })

                setHandler(in, new InHandler {
                    override def onPush() = {
                        val chunk = grab(in)
                        val hash = bytesHash(chunk.toArray)
                        hashSoFar = hashSoFar.map(mix(hash,_))
                                             .orElse(Option(hash))
                        push(out, chunk)
                    }

                    override def onUpstreamFinish(): Unit = {
                        mat.success(hashSoFar.map(HashValue.apply))
                        completeStage()
                    }
                })
            }
            (logic, mat.future)

        }
    }
}
