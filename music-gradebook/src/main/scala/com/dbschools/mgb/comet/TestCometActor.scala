package com.dbschools.mgb
package comet

import scala.xml.NodeSeq
import org.apache.log4j.Logger
import org.joda.time.DateTime
import akka.actor.Actor
import net.liftweb.http.{Templates, ListenerManager, CometListener, CometActor}
import net.liftweb.http.js.jquery.JqJsCmds.{FadeIn, FadeOut}
import net.liftweb.util.{Helpers, PassThru}
import Helpers._
import net.liftweb.actor.LiftActor
import net.liftweb.http.js.JsCmds.Reload
import net.liftweb.http.js.JE.JsRaw
import com.dbschools.mgb.schema.Musician
import com.dbschools.mgb.snippet.Testing
import model.BoxOpener._

case class ScheduledMusician(musician: Musician, sortOrder: Int, nextPieceName: String)
case class TestingMusician(musician: Musician, testerName: String, time: DateTime)

object testing {
  var scheduledMusicians = Set[ScheduledMusician]()
  var testingMusicians = Set[TestingMusician]()
}

class TestSchedulerActor extends Actor {
  def receive = {

    case ScheduleMusicians(scheds) =>
      testing.scheduledMusicians ++= scheds
      TestCometDispatcher ! RedisplaySchedule
      updateStudentsPage()

    case TestMusician(testingMusician) =>
      testing.scheduledMusicians.find(_.musician == testingMusician.musician).foreach(sm => {
        testing.scheduledMusicians -= sm
        testing.testingMusicians += testingMusician
        TestCometDispatcher ! MoveMusician(testingMusician)
      })
      updateStudentsPage()

    case ClearSchedule =>
      testing.scheduledMusicians = testing.scheduledMusicians.empty
      testing.testingMusicians = testing.testingMusicians.empty
      TestCometDispatcher ! RedisplaySchedule
      updateStudentsPage()
  }

  private def updateStudentsPage(): Unit =
    StudentsCometDispatcher ! TestingUpdate(testing.scheduledMusicians)
}

class TestCometActor extends CometActor with CometListener {
  private val log = Logger.getLogger(getClass)
  def registerWith = TestCometDispatcher

  override def lowPriority = {

    case RedisplaySchedule =>
      partialUpdate(Reload)

    case MoveMusician(testingMusician) =>
      val m = testingMusician.musician
      val prependRowToSessionsTable = {
        val testSchedTemplate: NodeSeq = Templates(List("testing")).open
        val cssSelExtractRow = s".sessionRow ^^" #> ""
        val row = cssSelExtractRow(testSchedTemplate)
        val cssSelProcessRow =
          Testing.sessionRow(show = false)(TestingMusician(m, testingMusician.testerName, DateTime.now))
        val jsString = cssSelProcessRow(row).toString.encJs
        s"""$$("#testingTable tbody").prepend($jsString);"""
      }

      partialUpdate(
        FadeOut("qr" + m.id, 0 seconds, 2 seconds) &
        JsRaw(prependRowToSessionsTable).cmd &
        FadeIn("sr" + m.id, 0 seconds, 2 seconds)
      )

    case Welcome =>
  }

  def render = PassThru
}

case object RedisplaySchedule
case class ScheduleMusicians(scheduledMusicians: Iterable[ScheduledMusician])
case class TestMusician(testingMusician: TestingMusician)
case class MoveMusician(testingMusician: TestingMusician)
case object ClearSchedule
case object Welcome

object TestCometDispatcher extends LiftActor with ListenerManager {
  def createUpdate = Welcome

  override def lowPriority = {
    case msg => updateListeners(msg)
  }
}
