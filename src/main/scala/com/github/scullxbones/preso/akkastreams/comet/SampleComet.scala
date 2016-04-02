package com.github.scullxbones.preso.akkastreams.comet

import com.github.scullxbones.preso.akkastreams.comet.SampleComet.{Clear, StreamComplete, StreamFailed}
import net.liftweb.common.{Box, Full, Loggable}
import net.liftweb.http._
import net.liftweb.http.js.JsCmds
import net.liftweb.util.BindHelpers

import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.xml.Elem

object SampleComet {
    sealed trait Protocol
    case object Clear extends Protocol
    case class AddMessages(messages: TraversableOnce[String]) extends Protocol
    case object StreamComplete extends Protocol
    case object StreamFailed extends Protocol
}

class SampleComet extends CometActor with Loggable {
    import SampleComet._

    private var results: Vector[Elem] = Vector.empty

    override def localSetup = {
        logger.info(s"Local setup!!!!")
    }

    override def defaultPrefix = Full("sample")

    override def render =
        bind("messages" -> results)

    override def lowPriority = {
        case p:Protocol => p match {
            case AddMessages(messages) =>
                results = results ++ messages.map(m => <li>{m}</li>)
                reRender(true)
            case Clear =>
                results = Vector.empty
                reRender(true)
            case StreamComplete =>
                results = results :+ <li><i class="fa fa-check"> </i> Stream completed</li>
                reRender(true)
            case StreamFailed =>
                results = results :+ <li><i class="fa fa-times"> </i> Stream failed</li>
                reRender(true)
        }
        case x =>
            logger.warn(s"Unexpected message $x")
    }
}

trait SampleCometDriver[A] extends BindHelpers {

    def name: String

    def runStream(comet: Box[LiftCometActor]): Future[A]

    def lookupComet =
        for {
            sess <- S.session
            comet <- sess.findComet("SampleComet", Full(name))
        } yield comet

    def run() = {
        import scala.concurrent.ExecutionContext.Implicits.global

        val comet = lookupComet
        comet.foreach(_ ! Clear)
        runStream(comet).onComplete {
            case Success(_) => comet.foreach(_ ! StreamComplete)
            case Failure(_) => comet.foreach(_ ! StreamFailed)
        }
        JsCmds.Noop
    }

    def render = {
        "#run" #> SHtml.ajaxButton("Run", run _)
    }

}
