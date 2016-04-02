package com.github.scullxbones.preso.akkastreams.stream

import akka.stream.{ActorAttributes, Supervision}
import akka.stream.scaladsl._
import com.github.scullxbones.preso.akkastreams.App

import scala.concurrent.Future
import scala.util.Random
import scala.util.control.NoStackTrace

object MapAsyncFlows {
    import App._

    val ids = "a" :: "B" :: "C" :: "D" :: "e" :: "F" :: "g" :: "H" :: "I" :: Nil

    def basicOrdered(parallelism: Int, fn: String => Unit) = {
        Source(ids)
          .mapAsync(parallelism)(asyncApi _)
          .runWith(Sink.foreach(fn))
    }

    def basicUnordered(parallelism: Int, fn: String => Unit) = {
        Source(ids)
            .mapAsyncUnordered(parallelism)(asyncApi _)
            .runWith(Sink.foreach(fn))
    }

    def mapAsyncFailure(parallelism: Int, fn: String => Unit) = {
        Source(ids.take(parallelism) ++ ("X" :: ids.drop(parallelism)))
            .mapAsync(parallelism)(asyncApi _)
            .runWith(Sink.foreach(fn))
    }

    def supervisedMapAsyncFailure(parallelism: Int, fn: String => Unit) = {
        Source(ids.take(parallelism) ++ ("X" :: ids.drop(parallelism)))
            .mapAsync(parallelism)(asyncApi _)
            .withAttributes(ActorAttributes.supervisionStrategy(Supervision.resumingDecider))
            .runWith(Sink.foreach(fn))
    }

    def asyncApi(str: String): Future[String] = Future {
        if (str == "X") throw new RuntimeException with NoStackTrace
        else if (str.nonEmpty && str.head.isLower)
            Thread.sleep(4 * Random.nextInt(250).toLong + 1000L)
        else Thread.sleep(Random.nextInt(100).toLong + 100L)
        str
    }(blockingDispatcher)
}
