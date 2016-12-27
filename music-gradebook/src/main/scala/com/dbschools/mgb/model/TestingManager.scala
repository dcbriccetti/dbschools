package com.dbschools.mgb
package model

import scalaz._
import Scalaz._
import akka.actor.Actor
import org.apache.log4j.Logger
import org.scala_tools.time.Imports._
import org.squeryl.PrimitiveTypeMode._
import net.liftweb.util.Props
import comet._
import comet.TestingCometActorMessages.UpdateQueueDisplay
import schema.{AppSchema, Musician, User}
import model.Periods.{TimeClass, NotInPeriod, InSpecialSchedule}
import snippet.{Testing, Selection}

class TestingManager extends Actor {
  val log = Logger.getLogger(getClass)
  private var lastPeriodValue: TimeClass = NotInPeriod
  var tickCount = 0L
  import StudentsCometActorMessages._
  import TestingCometActorMessages.{RebuildPage, MoveMusician, UpdateAssessmentCount}
  import TestingManagerMessages._
  import TestingManager._
  import comet.NoticesMessages._
  import comet.StudentMessages._
  import comet.GeneralSettingsCometActorMessages._

  def receive = {

    case Tick =>
      tickCount += 1
      TestingCometDispatcher ! UpdateQueueDisplay
      StudentCometDispatcher ! Next(called)

      val periodNow = Periods.periodWithin()
      if (periodNow != lastPeriodValue) {
        lastPeriodValue = periodNow
        GeneralSettingsCometDispatcher ! ChangePeriodElements
      }
      if (inQueueServiceTime) {
        testingState.servicingQueueTesterIdsReset = false
      } else {
        resetServicingQueueIdsIfNeeded()
      }

    case EnqueueMusicians(scheds) =>
      testingState.enqueuedMusicians ++= scheds
      TestingCometDispatcher ! RebuildPage
      updateStudentsPage()
      StudentCometDispatcher ! Next(called)

    case DequeueMusicians(ids) =>
      if ((testingState.enqueuedMusicians --= ids) > 0) {
        TestingCometDispatcher ! RebuildPage
        updateStudentsPage()
        StudentCometDispatcher ! Next(called)
      }

    case ToTop(ids) =>
      testingState.enqueuedMusicians.moveToTop(ids)
      TestingCometDispatcher ! RebuildPage

    case DequeueInstrumentsOfMusicians(musicianIds) =>
      if (musicianIds.nonEmpty) {
        val musicianIdsWithInstruments = transaction {
          val instQ = from(AppSchema.musicianGroups)(mg =>
            where(mg.musician_id in musicianIds and mg.school_year === Terms.currentTerm)
            select mg.instrument_id
          ).distinct
          from(AppSchema.musicianGroups)(mg =>
            where(mg.instrument_id in instQ and mg.school_year === Terms.currentTerm)
            select mg.musician_id
          ).distinct.toVector
        }
        Actors.testingManager ! DequeueMusicians(musicianIdsWithInstruments)
      }

    case TestMusician(testingMusician) =>
      if (testingState.enqueuedMusicians -= testingMusician.musician.id) {
        testingState.testingMusicians += testingMusician
        TestingCometDispatcher ! MoveMusician(testingMusician)
        StudentCometDispatcher ! Next(called)
      }
      updateStudentsPage()

    case IncrementMusicianAssessmentCount(tester, musician) =>
      val tm = testingState.testingMusicians.find(tm => tm.tester.id == tester.id && tm.musician.id == musician.id) | {
        // This student wasn’t selected from the queue, so make a TestingMusician record now
        val newTm = TestingMusician(musician, tester, DateTime.now, None)
        testingState.testingMusicians += newTm
        TestingCometDispatcher ! MoveMusician(newTm)
        newTm
      }
      tm.numTests += 1
      tm.lastActivity = DateTime.now
      TestingCometDispatcher ! UpdateAssessmentCount(tm)

    case SetServicingQueue(user, instrumentSelection) =>
      instrumentSelection.value match {
        case Left(false) =>
          testingState.servicingQueueTesterIds -= user.id
        case _ =>
          testingState.servicingQueueTesterIds += user.id -> instrumentSelection
      }
      StudentCometDispatcher ! Next(called)

    case SetCallAfterMins(user, mins, callNow) =>
      testingState.callAfterMinsByTesterId += user.id -> mins
      if (callNow)
        testingState.callNowTesterIds += user.id
      else
        testingState.callNowTesterIds -= user.id
      StudentCometDispatcher ! Next(called)

    case SetSpecialSchedule(specialSchedule) =>
      testingState.specialSchedule = specialSchedule
      GeneralSettingsCometDispatcher ! ChangePeriodElements
      GeneralSettingsCometDispatcher ! ChangeSpecialSchedule

    case ClearQueue =>
      testingState.enqueuedMusicians.empty()
      testingState.testingMusicians = testingState.testingMusicians.empty
      testingState.servicingQueueTesterIds = testingState.servicingQueueTesterIds.empty
      TestingCometDispatcher ! RebuildPage
      updateStudentsPage()
      StudentCometDispatcher ! Next(Nil)

    case Chat(chatMessage) =>
      testingState.chatMessages ::= chatMessage
      TestingCometDispatcher ! TestingCometActorMessages.Chat(chatMessage)
      NoticesDispatcher ! NumChatMsgs(testingState.chatMessages.size)

    case ClearChat =>
      testingState.chatMessages = Nil
      TestingCometDispatcher ! TestingCometActorMessages.ClearChat
      NoticesDispatcher ! NumChatMsgs(0)
  }

  private def resetServicingQueueIdsIfNeeded(): Unit = {
    if (! testingState.servicingQueueTesterIdsReset) {
      testingState.servicingQueueTesterIds = testingState.servicingQueueTesterIds.empty
      GeneralSettingsCometDispatcher ! ChangeServicingQueueSelection
      testingState.servicingQueueTesterIdsReset = true
    }
  }

  private def updateStudentsPage(): Unit =
    StudentsCometDispatcher ! QueueSize(testingState.enqueuedMusicians.size)

  private def inQueueServiceTime = {
    val Minutes = 1000 * 60
    Periods.periodWithin() match {
      case period: Periods.Period => period.timeRemainingMs > Minutes * 3
      case NotInPeriod            => false
      case InSpecialSchedule      => true
    }
  }
}

object TestingManager {
  val defaultNextCallMins = Props.getInt("defaultNextCallMins") getOrElse 5

  def called = Testing.rowIdMusicianAndTimes.filter(_.time.isEmpty).map(_.musician)
}

case class EnqueuedMusician(musician: Musician, instrumentId: Int, nextPieceName: String)

case class TestingMusician(musician: Musician, tester: User, startingTime: DateTime, fromQueue: Option[MusicianQueue]) {
  var numTests = 0
  var lastActivity = startingTime
}

case class ChatMessage(time: DateTime, user: User, msg: String)

case class TesterAvailableTime(testerId: Int, selection: Selection, time: Option[DateTime] /* None = now */) {
  def matchesInstrument(id: Int) = selection.value.right.toOption.map(_ == id) getOrElse true
}

object TestingManagerMessages {
  case class EnqueueMusicians(enqueuedMusicians: Iterable[EnqueuedMusician])
  case class ToTop(ids: Iterable[Int])
  case class DequeueMusicians(ids: Iterable[Int])
  case class DequeueInstrumentsOfMusicians(musicianIds: Iterable[Int])
  case class TestMusician(testingMusician: TestingMusician)
  case class IncrementMusicianAssessmentCount(tester: User, musician: Musician)
  case class SetServicingQueue(tester: User, instrumentSelection: Selection)
  case class SetCallAfterMins(tester: User, mins: Option[Int], callNow: Boolean)
  case class SetSpecialSchedule(specialSchedule: Boolean)
  case object ClearQueue
  case class Chat(chatMessage: ChatMessage)
  case object ClearChat
  case object Tick
}
