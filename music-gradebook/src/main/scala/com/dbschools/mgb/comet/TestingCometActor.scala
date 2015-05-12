package com.dbschools.mgb
package comet

import scala.language.postfixOps
import scala.xml.Text
import net.liftweb.http.{CometListener, CometActor}
import net.liftweb.http.js.JsCmds.{Replace, JsShowId}
import net.liftweb.http.js.JE.JsRaw
import net.liftweb.util.{Helpers, PassThru}
import Helpers._
import snippet.Testing
import snippet.LiftExtensions._
import Testing.{queueRowId, sessionRowId, sessionRow}
import model.{SessionStats, TesterDuration, ChatMessage, TestingMusician, testingState}
import schema.User

class TestingCometActor extends CometActor with CometListener {
  import TestingCometActorMessages._
  import CommonCometActorMessages._

  def registerWith = TestingCometDispatcher

  override def lowPriority = {

    case RebuildPage =>
      replacePageSection("testingContents")

    case MoveMusician(testingMusician, timesUntilCall) =>
      val id = testingMusician.musician.id
      val queueRowSel = "#" + queueRowId(id)

      partialUpdate(
        JsJqRemove(queueRowSel) &
        showSessionsTable(testingMusician.tester) &
        prependRowToSessionsTable(testingMusician) &
        clearOldSessionsTableRows(testingMusician.tester) &
        updateStats(testingMusician.tester) &
        JsShowId(sessionRowId(id)) &
        JsRaw("activateTips();").cmd
      )

    case UpdateAssessmentCount(tm) =>
      val testerId = uid(tm.tester)
      val rowId = sessionRowId(tm.musician.id)
      val sel = s"$testerId #$rowId .srasmts"
      partialUpdate(
        JsJqHtml(sel, tm.numTests) &
        JsJqHilite(sel)
      )

    case UpdateQueueDisplay =>
      partialUpdate(Testing.makeQueueUpdateAndNotificationJs)

    case Chat(chatMessage) =>
      partialUpdate(Testing.addMessage(chatMessage))

    case ClearChat =>
      partialUpdate(Testing.clearMessages)

    case Start =>
  }

  private def uid(user: User) = s"#user${user.id}"

  private def updateStats(user: User) = {
    val ss = SessionStats(user.id)
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

  private def replacePageSection(elemId: String) {
    val elem = elemFromTemplate("testing", s"#$elemId")
    partialUpdate(Replace(elemId, elem))
  }

  def render = PassThru
}

object TestingCometDispatcher extends CommonCometDispatcher

object TestingCometActorMessages {
  case object RebuildPage
  /** Removes a musician from the queue (if it exists), and adds it to a testing session */
  case class MoveMusician(testingMusician: TestingMusician, timesUntilCall: Iterable[TesterDuration])
  case class UpdateAssessmentCount(testingMusician: TestingMusician)
  object UpdateQueueDisplay
  case class Chat(chatMessage: ChatMessage)
  case object ClearChat
}
