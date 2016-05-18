package com.github.scullxbones.preso.akkastreams.stream

import akka.stream.ClosedShape
import akka.{Done, NotUsed}
import akka.stream.scaladsl._
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits._
import scala.util.Random

case class MetricValue(value: Double) extends AnyVal
case class DeviceIdentifier(id: Long) extends AnyVal
case class DeviceAttribute(name: String) extends AnyVal
case class TimeSeriesData(timestamp: DateTime, deviceId: DeviceIdentifier, deviceAttribute: DeviceAttribute, value: MetricValue)

trait TimeSeriesSource {

  val SECONDS_IN_A_DAY = 24 * 60 * 60
  val KNOWN_ATTRIBUTES = "foo" :: "bar" :: "baz" :: "qux" :: "quux" :: "frobnobz" :: Nil

  private def topSecretDataGenerator: TimeSeriesData =
      TimeSeriesData(
        timestamp = DateTime.now().plusDays(1).minusSeconds(Random.nextInt(SECONDS_IN_A_DAY)),
        deviceId = DeviceIdentifier(Random.nextInt(10000).toLong),
        deviceAttribute = DeviceAttribute(KNOWN_ATTRIBUTES(Random.nextInt(KNOWN_ATTRIBUTES.size))),
        value = MetricValue(Random.nextDouble()))


  def cassandraQuery(from: DateTime, to: DateTime)(): Iterator[TimeSeriesData] =
      (1 to 100).map(_ => topSecretDataGenerator).sortBy(_.timestamp.getMillis).toIterator

  def cassandraSource(forDate: DateTime): Source[TimeSeriesData, NotUsed] = {
      val from = forDate.withTimeAtStartOfDay()
      val to = forDate.withTimeAtStartOfDay().plusDays(1)

      Source.fromIterator(cassandraQuery(from, to))
  }
}

case class S3BucketAndKey(bucket: String, key: String)
sealed trait UploadReport
case class UploadSuccess(segments: Int) extends UploadReport
case class UploadFailure(exception: Throwable) extends UploadReport
trait UploadCompleter { def completeUpload: Future[UploadReport] }

trait ArchivalBranch {

  val dtFormat = DateTimeFormat.fullDateTime()
  def convertToCsvRow(tsData: TimeSeriesData): String =
      List(dtFormat.print(tsData.timestamp), tsData.deviceId.id.toString, tsData.deviceAttribute.name, f"${tsData.value.value}%1.3f", Nil) mkString ","

  def s3sink(s3info: S3BucketAndKey): Sink[String, Future[UploadCompleter]] = {
    Sink.fold[Int, String](0){ case(accum,_) => accum + 1}.mapMaterializedValue{ lines =>
      for {
         l <- lines
      } yield new UploadCompleter {
          def completeUpload = Future.successful(UploadSuccess(l))
      }
    }
  }
}

sealed trait Interval {
    def newInterval(ts: DateTime): Boolean
}
object Interval {
    private class HourIntervalImpl(modulo: Int) extends Interval {
        override def newInterval(ts: DateTime) = ts.minuteOfHour().get() % modulo == 0
        override def toString = s"Every $modulo minutes per hour"
    }

    val `15m`: Interval = new HourIntervalImpl(15)
    val `30m`: Interval = new HourIntervalImpl(30)
    val `1h`: Interval = new HourIntervalImpl(60)
}

case class Statistics(mean: Double, stddev: Double, median: Double)
case class AggregatedData(deviceIdentifier: DeviceIdentifier,
                          deviceAttribute: DeviceAttribute,
                          asOf: DateTime,
                          interval: Interval,
                          statistics: Statistics)

trait AggregationBranch {
  import collection.immutable.Seq

  def aggregate(intervalData: Seq[TimeSeriesData]): Option[Statistics] = {
    val values = intervalData.map(_.value.value).sorted
    for {
        mean <- values.reduceLeftOption[Double](_ + _).map(_ / intervalData.size)
        stddev <- values.reduceLeftOption[Double]{case(accum,value) => accum + math.pow(value - mean, 2)}.map(_ / values.size).map(math.sqrt)
        median <- Option(values(values.size / 2))
    } yield Statistics(mean, stddev, median)
  }

  val MAXIMUM_GROUPS_INFLIGHT = 100 // Memory-bounded requirement of akka streams
  def groupByKey() = {
    Flow[TimeSeriesData]
        .groupBy(MAXIMUM_GROUPS_INFLIGHT, tsd => (tsd.deviceId, tsd.deviceAttribute))
  }

  def groupByInterval(interval: Interval, log: String => Unit): Sink[TimeSeriesData, NotUsed] = {
    Flow[TimeSeriesData].splitWhen(tsd => interval.newInterval(tsd.timestamp)).to(intervalSink(interval, log))
  }

  def intervalSink(interval: Interval, log: String => Unit): Sink[TimeSeriesData, NotUsed] = {
      Flow[TimeSeriesData]
          .fold(Seq.empty[TimeSeriesData])(_ :+ _)
          .map[Option[AggregatedData]](ts =>
              for {
               head <- ts.headOption
               statistics <- aggregate(ts)
              } yield AggregatedData(
                  deviceIdentifier = head.deviceId,
                  deviceAttribute = head.deviceAttribute,
                  asOf = head.timestamp,
                  interval = interval,
                  statistics = statistics
              )
          )
          .map(_.map(saveAggregateToCassandra).getOrElse(Future.successful(Done)))
          .to(Sink.ignore)
  }

  def aggregateBranch(log: String => Unit): Sink[TimeSeriesData, NotUsed] = {
      val fifteen = groupByInterval(Interval.`15m`, log)
      val thirty = groupByInterval(Interval.`30m`, log)
      val sixty = groupByInterval(Interval.`1h`, log)

      groupByKey().to(Sink.combine(fifteen, thirty, sixty)(_ => Broadcast(3)))
  }

  def saveAggregateToCassandra(aggregatedData: AggregatedData): Future[Done] = {
      Future.successful(Done)
  }
}

object AppliedFlows extends TimeSeriesSource with ArchivalBranch with AggregationBranch {
    def archiveAndAggregateGraph(forDate: DateTime, s3Gen: DateTime => S3BucketAndKey, log: String => Unit) = {
        val sink = s3sink(s3Gen(forDate))
        RunnableGraph.fromGraph(GraphDSL.create(cassandraSource(forDate), sink)(Keep.right) { implicit builder => (cass, snk) =>
            import GraphDSL.Implicits._

            val split = builder.add(Broadcast[TimeSeriesData](2))
            val convertToCsv = builder.add(Flow[TimeSeriesData].map(convertToCsvRow))
            val aggSnk = builder.add(Flow[TimeSeriesData].to(aggregateBranch(log)))

            cass ~> split.in
                    split.out(0) ~> convertToCsv ~> snk
                    split.out(1) ~> aggSnk

           ClosedShape
        })
    }
}
