package bootstrap.liftweb

import net.liftweb.common._
import net.liftweb.http._
import js.{JsCmd, JE}
import net.liftweb.util.Helpers._

import scala.collection.immutable.Set

object MiscellaneousSetup {

  def setup(): Unit = {
    utf8Encoding()
    clearParsedSuffixes()
    forceChromeFrame()
    renderHtml5()
    cacheBuster()
    setupAjaxErrorHandling()
    ()
  }

  def utf8Encoding() = LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

  def clearParsedSuffixes() = LiftRules.explicitlyParsedSuffixes = Set()

  def forceChromeFrame() = {
    LiftRules.defaultHeaders = {
      case (_,_) => List("X-UA-Compatible" -> "IE=edge")
    }
  }

  def renderHtml5() = LiftRules.htmlProperties.default.set((r: Req) => new Html5Properties(r.userAgent))

  def cacheBuster() = {
    LiftRules.supplementalHeaders.default.set(
      List(
        "Cache-Control" -> "no-cache, no-store, max-age=0, must-revalidate",
        "Pragma" -> "no-cache",
        "Expires" -> "Fri, 31 Dec 1999 00:00:00 GMT"
      )

    )
  }

  def setupAjaxErrorHandling() = {
    LiftRules.ajaxRetryCount = Full(1)
    LiftRules.ajaxPostTimeout = 3000

    LiftRules.redirectAsyncOnSessionLoss = true
    LiftRules.noAjaxSessionCmd.default.set(new JE.JsRaw("window.serverReload ? window.serverReload() : window.location.reload();") with JsCmd)
  }
}
