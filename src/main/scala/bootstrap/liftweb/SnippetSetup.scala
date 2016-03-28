package bootstrap.liftweb

import net.liftweb.http._
import net.liftweb.sitemap._

object SnippetSetup {
  def setup(): Unit = {
    // Where to search for snippets.
    LiftRules.addToPackages("com.github.scullxbones.preso.akkastreams")
    LiftRules.noticesToJsCmd = () => js.JsCmds.Noop

    setupSiteMap()
    setupStatelessness()

    LiftRules.stripComments.default.set(() => false)
    ()
  }

  def siteMap(): SiteMap = {
    SiteMap(
      Menu.i("Home") / "index"
    )
  }

  private def setupSiteMap(): Unit = {
    LiftRules.liftRequest.append {
      case Req("sitemap" :: Nil, "xml", _) => false
    }

    LiftRules.setSiteMapFunc(siteMap _)
  }

  private def setupStatelessness(): Unit = {
    LiftRules.statelessReqTest.prepend {
      case StatelessReqTest("javascripts" :: _, _)  => true
      case StatelessReqTest("favicon.ico" :: _, _)  => true
      case StatelessReqTest("classpath" :: _, _)    => true
      case StatelessReqTest("stylesheets" :: _, _)  => true
    }

    // Don't include AJAX on stateless pages so that we can avoid triggering
    // a session via the AJAX script.

    def onlyIncludeOnStateful()(s: LiftSession): Boolean = s.stateful_?

    LiftRules.autoIncludeAjaxCalc.default.set(onlyIncludeOnStateful _)
    ()
  }
}
