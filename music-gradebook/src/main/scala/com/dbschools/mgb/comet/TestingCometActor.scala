package com.dbschools.mgb
package comet

import scala.language.postfixOps
import org.joda.time.format.DateTimeFormat
import net.liftweb.http.{Templates, CometListener, CometActor}
import net.liftweb.http.js.JsCmds.{After, Noop, Reload}
import net.liftweb.http.js.jquery.JqJsCmds.{FadeIn, FadeOut}
import net.liftweb.util.{Helpers, PassThru}
import Helpers._
import com.dbschools.mgb.model.{ChatMessage, TestingMusician}
import snippet.Testing
import snippet.LiftExtensions._
import Testing.{queueRowId, sessionRowId, sessionRow}
import model.BoxOpener._

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
        prependRowToSessionsTable(testingMusician) &
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

  private def prependRowToSessionsTable(testingMusician: TestingMusician) = {
    val row = elemFromTemplate("testing", ".sessionRow")
    val cssSelProcessRow = sessionRow(show = false)(testingMusician)
    JsJqPrepend("#testingTable tbody", cssSelProcessRow(row).toString().encJs)
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
