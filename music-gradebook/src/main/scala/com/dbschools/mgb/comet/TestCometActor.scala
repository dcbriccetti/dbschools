package com.dbschools.mgb.comet

import net.liftweb.http.{ListenerManager, CometListener, CometActor}
import org.apache.log4j.Logger
import akka.actor.Actor
import net.liftweb.http.js.jquery.JqJsCmds.{FadeIn, FadeOut}
import net.liftweb.util.{Helpers, PassThru}
import Helpers._
import net.liftweb.actor.LiftActor
import net.liftweb.http.js.JsCmds.Reload
import net.liftweb.http.js.JE.JsRaw
import com.dbschools.mgb.schema.Musician
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

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

    case TestMusician(testingMusician) =>
      testing.scheduledMusicians.find(_.musician == testingMusician.musician).foreach(sm => {
        testing.scheduledMusicians -= sm
        testing.testingMusicians += testingMusician
      })
      TestCometDispatcher ! MoveMusician(testingMusician)

    case ClearSchedule =>
      testing.scheduledMusicians = testing.scheduledMusicians.empty
      testing.testingMusicians = testing.testingMusicians.empty
      TestCometDispatcher ! RedisplaySchedule
  }
}

class TestCometActor extends CometActor with CometListener {
  private val log = Logger.getLogger(getClass)
  def registerWith = TestCometDispatcher

  override def lowPriority = {

    case RedisplaySchedule =>
      partialUpdate(Reload)

    case MoveMusician(testingMusician) =>
      val m = testingMusician.musician
      val tn = testingMusician.testerName
      val rowId = "t" + m.id
      val tmf = DateTimeFormat.forStyle("-M")
      val now = tmf.print(DateTime.now)

      partialUpdate( // todo Remove duplication
        FadeOut(testingMusician.musician.id.toString, 0 seconds, 2 seconds) &
        JsRaw(s"""$$("#testingTable tbody").prepend("<tr id='$rowId' style='display: none;'>""" +
          s"""<td class='h4'>${m.first_name} ${m.last_name}</td><td class='h4'>$tn</td><td class='h4'>$now</td></tr>");""").cmd &
        FadeIn(rowId, 0 seconds, 2 seconds)
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
