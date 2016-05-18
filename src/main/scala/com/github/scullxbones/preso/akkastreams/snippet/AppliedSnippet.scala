package com.github.scullxbones.preso.akkastreams.snippet

import akka.Done
import com.github.scullxbones.preso.akkastreams.App
import com.github.scullxbones.preso.akkastreams.comet.SampleComet.AddMessages
import com.github.scullxbones.preso.akkastreams.comet.SampleCometDriver
import com.github.scullxbones.preso.akkastreams.stream.{AppliedFlows, S3BucketAndKey, UploadFailure, UploadSuccess}
import net.liftweb.common.{Box, Loggable}
import net.liftweb.http.LiftCometActor
import org.joda.time.DateTime

import scala.concurrent.Future

object AppliedSnippet extends SampleCometDriver[Done] with Loggable {
    override def name = "applied-example"

    def keyFor(dt: DateTime) = {
        s"year=${dt.getYear}/month=${dt.getMonthOfYear}/day=${dt.getDayOfMonth}"
    }

    import App._
    override def runStream(comet: Box[LiftCometActor]): Future[Done] = {
        implicit val ec = nonblockingDispatcher

        val bucketName = "aggregated-timeseries-data"
        AppliedFlows.archiveAndAggregateGraph(DateTime.now,
            dt => S3BucketAndKey(bucketName, keyFor(dt)),
            s => comet.foreach(_ ! AddMessages(s :: Nil))
        ).run().flatMap(_.completeUpload).map {
            case UploadSuccess(lines) =>
                comet.foreach(_ ! AddMessages(s"$lines rows sent to S3" :: Nil))
                Done
            case UploadFailure(x) =>
                comet.foreach(_ ! AddMessages(s"Upload failed due to exception ${x.getMessage}" :: Nil))
                Done
        }
    }
}
