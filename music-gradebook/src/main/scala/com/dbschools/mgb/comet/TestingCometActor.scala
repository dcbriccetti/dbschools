package com.dbschools.mgb
package comet

import net.liftweb.http.{Templates, CometListener, CometActor}
import net.liftweb.http.js.jquery.JqJsCmds.{FadeIn, FadeOut}
import net.liftweb.util.{Helpers, PassThru}
import Helpers._
import net.liftweb.http.js.JsCmds.Reload
import net.liftweb.http.js.JE.JsRaw
import model.TestingMusician
import snippet.Testing
import Testing.{queueRowId, sessionRowId, sessionRow}
import model.BoxOpener._

object TestingCometActorMessages {
  case object RedisplaySchedule
  case class MoveMusician(testingMusician: TestingMusician)
}

class TestingCometActor extends CometActor with CometListener {
  import TestingCometActorMessages._
  import CommonCometActorMessages._

  def registerWith = TestingCometDispatcher

  override def lowPriority = {

    case RedisplaySchedule =>
      partialUpdate(Reload)

    case MoveMusician(testingMusician) =>
      val id = testingMusician.musician.id
      partialUpdate(
        FadeOut(queueRowId(id), 0 seconds, 2 seconds) &
        JsRaw(prependRowToSessionsTable(testingMusician)).cmd &
        FadeIn(sessionRowId(id), 0 seconds, 2 seconds)
      )

    case Start =>
  }

  private def prependRowToSessionsTable(testingMusician: TestingMusician) = {
    val testSchedTemplate = Templates(List("testing")).open
    val cssSelExtractRow = s".sessionRow ^^" #> ""
    val row = cssSelExtractRow(testSchedTemplate)
    val cssSelProcessRow = sessionRow(show = false)(testingMusician)
    val jsString = cssSelProcessRow(row).toString().encJs
    s""" $$("#testingTable tbody").prepend($jsString);"""
  }

  def render = PassThru
}

object TestingCometDispatcher extends CommonCometDispatcher
