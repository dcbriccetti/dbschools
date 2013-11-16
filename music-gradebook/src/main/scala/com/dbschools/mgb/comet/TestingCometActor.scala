package com.dbschools.mgb
package comet

import scala.language.postfixOps
import net.liftweb.http.{Templates, CometListener, CometActor}
import net.liftweb.http.js.JsCmds.{After, Noop, Reload}
import net.liftweb.http.js.jquery.JqJsCmds.{FadeIn, FadeOut}
import net.liftweb.util.{Helpers, PassThru}
import Helpers._
import model.TestingMusician
import snippet.Testing
import snippet.LiftExtensions._
import Testing.{queueRowId, sessionRowId, sessionRow}
import model.BoxOpener._

class TestingCometActor extends CometActor with CometListener {
  import TestingCometActorMessages._
  import CommonCometActorMessages._

  def registerWith = TestingCometDispatcher

  override def lowPriority = {

    case RedisplaySchedule =>
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

    case Start =>
  }

  private def prependRowToSessionsTable(testingMusician: TestingMusician) = {
    val testSchedTemplate = Templates(List("testing")).open
    val cssSelExtractRow = s".sessionRow ^^" #> ""
    val row = cssSelExtractRow(testSchedTemplate)
    val cssSelProcessRow = sessionRow(show = false)(testingMusician)
    JsJqPrepend("#testingTable tbody", cssSelProcessRow(row).toString().encJs)
  }

  def render = PassThru
}

object TestingCometDispatcher extends CommonCometDispatcher

object TestingCometActorMessages {
  case object RedisplaySchedule
  case class MoveMusician(testingMusician: TestingMusician, opNextMusicianId: Option[Int])
  case class UpdateAssessmentCount(testingMusician: TestingMusician)
}
