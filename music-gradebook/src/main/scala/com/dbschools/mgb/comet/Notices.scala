package com.dbschools.mgb
package comet

import xml.Text
import net.liftweb.http.{CometListener, CometActor}
import net.liftweb.http.js.JsCmds.{SetHtml, Noop}
import snippet.LiftExtensions.{JsShowIdIf, JsJqHilite}
import model.testingState
import snippet.Authenticator

/** Pushes notices to the top of every page */
class Notices extends CometActor with CometListener {
  import NoticesMessages._
  import CommonCometActorMessages._

  def registerWith = NoticesDispatcher

  override protected def dontCacheRendering = true

  override def lowPriority = {

    case NumChatMsgs(num) =>
      partialUpdate(
        SetHtml("numChatMsgs", Text(num.toString)) &
        JsShowIdIf("messagesNotice", Authenticator.loggedIn && num > 0) &
        (if (num > 0) JsJqHilite("#messagesNotice") else Noop)
      )

    case Start =>
  }

  def render = {
    val show = Authenticator.loggedIn && testingState.chatMessages.nonEmpty
    "#messagesNotice [style+]"  #> (if (show) "" else "display: none;") &
    "#numChatMsgs *"            #> (if (show) testingState.chatMessages.size else 0)
  }
}

object NoticesMessages {
  case class NumChatMsgs(num: Int)
}

object NoticesDispatcher extends CommonCometDispatcher
