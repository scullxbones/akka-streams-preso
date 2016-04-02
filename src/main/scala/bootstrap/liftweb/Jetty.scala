package bootstrap.liftweb

import java.io.File

import net.liftweb.common._
import net.liftweb.util.Helpers.tryo
import org.eclipse.jetty.rewrite.handler.{HeaderPatternRule, RewriteHandler, Rule}
import org.eclipse.jetty.server.handler.HandlerCollection
import org.eclipse.jetty.server.{Handler, Server, ServerConnector}
import org.eclipse.jetty.webapp.WebAppContext

object Jetty extends App {

  def configureContext: WebAppContext = {
    val context = new WebAppContext
    context.setMaxFormContentSize(2000000)
    context
  }

  def configureCookies(context: WebAppContext): WebAppContext = {
    val cookieConfig = tryo(context.getSessionHandler.getSessionManager.getSessionCookieConfig)
    cookieConfig.map(_.setHttpOnly(true))
    context
  }

  def configureStaticContent(context: WebAppContext): WebAppContext = {
    def pathCheck(path: String) =
      Option(new File(path)).filter(_.exists()).exists(_.isDirectory())

    val webappDir = if (pathCheck("target/webapp")) {
      "target/webapp"
    } else if (pathCheck("src/main/webapp")) {
      "src/main/webapp"
    } else {
      tryo(context.getClass.getClassLoader.getResource("webapp")).flatMap(Option(_)).map(_.toExternalForm) ?~ "Webapp not found" match {
        case Full(x) => x
        case Empty =>
          System.exit(-1)
          ""
        case Failure(_, bx, _) =>
          bx.foreach(_.printStackTrace())
          System.exit(-1)
          "" // For the compiler
      }
    }
    context.setWar(webappDir)
    context
  }

  def configureXssHeaders(context: WebAppContext): Handler = {
    def run[U,T](block: (U,T) => Unit)(accum: U, arg: T) = { block(accum,arg); accum }
    def toHeader: ((String,String)) => Rule = {
      case (name,value) =>
        val headerRule = new HeaderPatternRule
        headerRule.setAdd(true)
        headerRule.setPattern("/*")
        headerRule.setName(name)
        headerRule.setValue(value)
        headerRule
    }

    val headers =
      "X-Frame-Options" -> "SAMEORIGIN" ::
      "X-XSS-Protection" -> "1; mode=block" ::
      "X-Content-Type-Options" -> "nosniff" ::
      Nil

    val rewriteHandler = headers.map(toHeader).foldLeft(new RewriteHandler)(run(_.addRule(_)))
    val handlerCollection =
      (rewriteHandler :: context :: Nil).foldLeft(new HandlerCollection())(run(_.addHandler(_)))

    handlerCollection
  }

  def startServer() = {
    val server = new Server()
    val port: Int = tryo(args(0).toInt).filter(_ > 0).filter(_ < 65536).openOr(8080)
    val conn = new ServerConnector(server)
    conn.setPort(port)
    server.setConnectors(Array(conn))

    val context = (configureCookies _ andThen configureStaticContent andThen configureXssHeaders)(configureContext)
    server.setHandler(context)
    server.start()
    sys.addShutdownHook {
      server.stop()
    }
  }

  startServer()
}
