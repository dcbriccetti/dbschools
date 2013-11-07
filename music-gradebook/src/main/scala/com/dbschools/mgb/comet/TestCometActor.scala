package com.dbschools.mgb.comet

import net.liftweb.http.{ListenerManager, CometListener, CometActor}
import org.apache.log4j.Logger
import net.liftweb.http.js.jquery.JqJsCmds.FadeOut
import net.liftweb.util.PassThru
import net.liftweb.actor.LiftActor
import net.liftweb.http.js.JsCmds.Reload
import net.liftweb.util.Helpers._
import com.dbschools.mgb.schema.Musician
import akka.actor.Actor

case class ScheduledMusician(musician: Musician, sortOrder: Int, nextPieceName: String)

object testing {
  var scheduledMusicians = Set[ScheduledMusician]()
}

class TestSchedulerActor extends Actor {
  def receive = {

    case ScheduleMusicians(scheds) =>
      testing.scheduledMusicians ++= scheds
      TestCometDispatcher ! RedisplaySchedule

    case RemoveMusician(musician) =>
      testing.scheduledMusicians.find(_.musician == musician).foreach(testing.scheduledMusicians -= _)
      TestCometDispatcher ! RemoveMusician(musician)

    case ClearSchedule =>
      testing.scheduledMusicians.foreach(sm => TestCometDispatcher ! RemoveMusician(sm.musician))
      testing.scheduledMusicians = testing.scheduledMusicians.empty
  }
}

class TestCometActor extends CometActor with CometListener {
  private val log = Logger.getLogger(getClass)
  def registerWith = TestCometDispatcher

  override def lowPriority = {

    case RedisplaySchedule =>
      partialUpdate(Reload)

    case RemoveMusician(musician) =>
      partialUpdate(FadeOut(musician.id.toString, 0 seconds, 2 seconds))

    case Welcome =>
  }

  def render = PassThru
}

case object RedisplaySchedule
case class ScheduleMusicians(scheds: Iterable[ScheduledMusician])
case class RemoveMusician(musician: Musician)
case object ClearSchedule
case object Welcome

object TestCometDispatcher extends LiftActor with ListenerManager {
  def createUpdate = Welcome

  override def lowPriority = {
    case msg => updateListeners(msg)
  }
}
