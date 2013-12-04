package com.dbschools.mgb
package comet

import scala.language.postfixOps
import net.liftweb.http.{CometListener, CometActor}
import net.liftweb.http.js.JsCmds.{After, Noop, Reload, JsShowId}
import net.liftweb.http.js.jquery.JqJsCmds.{FadeIn, FadeOut}
import net.liftweb.util.{Helpers, PassThru}
import Helpers._
import com.dbschools.mgb.model.{ChatMessage, TestingMusician}
import snippet.Testing
import snippet.LiftExtensions._
import Testing.{queueRowId, sessionRowId, sessionRow}
import com.dbschools.mgb.schema.User
import scala.xml.Text

class TestingCometActor extends CometActor with CometListener {
  import TestingCometActorMessages._
  import CommonCometActorMessages._

  def registerWith = TestingCometDispatcher

  override def lowPriority = {

    case ReloadPage =>
      partialUpdate(Reload)

    case MoveMusician(testingMusician, opNextMusicianId) =>
      val id = testingMusician.musician.id
      val fadeTime = 2.seconds
      val queueRowSel = "#" + queueRowId  (id)
      val sessRowSel  = "#" + sessionRowId(id)

      partialUpdate(
        FadeOut(queueRowId(id), 0 seconds, fadeTime) &
        After(fadeTime, JsJqRemove(queueRowSel)) &
        showSessionsTable(testingMusician.tester) &
        prependRowToSessionsTable(testingMusician) &
        clearOldSessionsTableRows(testingMusician.tester) &
        updateStats(testingMusician.tester) &
        FadeIn(sessionRowId(id), 0 seconds, fadeTime) &
        After(fadeTime, JsJqHilite(sessRowSel)) &
        (opNextMusicianId.map(nextId => After(fadeTime, JsJqHilite("#" + queueRowId(nextId), 60000))) getOrElse Noop)
      )

    case UpdateAssessmentCount(tm) =>
      val rowId = sessionRowId(tm.musician.id)
      val sel = s"#$rowId .srasmts"
      partialUpdate(
        JsJqHtml(sel, tm.numAsmts) &
        JsJqHilite(sel)
      )

    case Chat(chatMessage) =>
      partialUpdate(Testing.addMessage(chatMessage))

    case ClearChat =>
      partialUpdate(Testing.clearMessages)

    case Start =>
  }

  private def uid(user: User) = s"#user${user.id}"

  private def updateStats(user: User) = {
    val ss = Testing.SessionStats(user)
    val id = uid(user)
    JsJqHtml(s"$id .avgMins", Text(ss.avgMinsStr)) &
    JsJqHtml(s"$id .numSessions", Text(ss.num.toString)) &
    JsJqHtml(s"$id .stdev", Text(ss.σStr))
  }

  private def showSessionsTable(user: User) = JsShowId(s"user${user.id}")

  private def clearOldSessionsTableRows(user: User) = JsJqDelRows(s"${uid(user)} table",
    Testing.SessionsToShowPerTester)

  private def prependRowToSessionsTable(testingMusician: TestingMusician) = {
    val row = elemFromTemplate("testing", ".sessionRow")
    val cssSelProcessRow = sessionRow(show = false)(testingMusician)
    JsJqPrepend(s"${uid(testingMusician.tester)} table tbody", cssSelProcessRow(row).toString().encJs)
  }

  def render = PassThru
}

object TestingCometDispatcher extends CommonCometDispatcher

object TestingCometActorMessages {
  case object ReloadPage
  case class MoveMusician(testingMusician: TestingMusician, opNextMusicianId: Option[Int])
  case class UpdateAssessmentCount(testingMusician: TestingMusician)
  case class Chat(chatMessage: ChatMessage)
  case object ClearChat
}
