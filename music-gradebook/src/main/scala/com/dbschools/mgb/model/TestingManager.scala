package com.dbschools.mgb
package model

import scalaz._
import Scalaz._
import akka.actor.Actor
import org.apache.log4j.Logger
import org.scala_tools.time.Imports._
import org.squeryl.PrimitiveTypeMode._
import comet.{NoticesDispatcher, StudentCometDispatcher, StudentsCometDispatcher, StudentsCometActorMessages,
  TestingCometDispatcher, TestingCometActorMessages}
import com.dbschools.mgb.schema.{AppSchema, Musician, User}
import net.liftweb.util.Props
import com.dbschools.mgb.comet.TestingCometActorMessages.SetTimesUntilCall

class TestingManager extends Actor {
  val log = Logger.getLogger(getClass)
  import StudentsCometActorMessages._
  import TestingCometActorMessages.{ReloadPage, MoveMusician, UpdateAssessmentCount}
  import TestingManagerMessages._
  import TestingManager._
  import comet.NoticesMessages._
  import comet.StudentMessages._

  def receive = {

    case Tick =>
      TestingCometDispatcher ! SetTimesUntilCall(testingState.timesUntilCall)
      StudentCometDispatcher ! Next(called)

    case EnqueueMusicians(scheds) =>
      testingState.enqueuedMusicians ++= scheds
      TestingCometDispatcher ! ReloadPage
      updateStudentsPage()
      StudentCometDispatcher ! Next(called)

    case DequeueMusicians(ids) =>
      if ((testingState.enqueuedMusicians --= ids) > 0) {
        TestingCometDispatcher ! ReloadPage
        updateStudentsPage()
        StudentCometDispatcher ! Next(called)
      }

    case ToTop(ids) =>
      testingState.enqueuedMusicians.moveToTop(ids)
      TestingCometDispatcher ! ReloadPage

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
        TestingCometDispatcher ! MoveMusician(testingMusician, testingState.timesUntilCall)
        StudentCometDispatcher ! Next(called)
      }
      updateStudentsPage()

    case IncrementMusicianAssessmentCount(tester, musician) =>
      val tm = testingState.testingMusicians.find(tm => tm.tester.id == tester.id && tm.musician.id == musician.id) | {
        // This student wasn’t selected from the queue, so make a TestingMusician record now
        val newTm = TestingMusician(musician, tester, DateTime.now, None)
        testingState.testingMusicians += newTm
        TestingCometDispatcher ! MoveMusician(newTm, testingState.timesUntilCall)
        newTm
      }
      tm.numAsmts += 1
      tm.lastActivity = DateTime.now
      TestingCometDispatcher ! UpdateAssessmentCount(tm)

    case SetServicingQueue(user, servicing) =>
      if (servicing)
        testingState.servicingQueueTesterIds += user.id
      else
        testingState.servicingQueueTesterIds -= user.id

    case SetCallAfterMins(user, mins, callNow) =>
      testingState.callAfterMinsByTesterId += user.id -> mins
      if (callNow)
        testingState.callNowTesterIds += user.id
      else
        testingState.callNowTesterIds -= user.id
      StudentCometDispatcher ! Next(called)

    case SetLastTestOrder(lastTestOrder) =>
      testingState.enqueuedMusicians.lastTestOrder = lastTestOrder
      TestingCometDispatcher ! ReloadPage

    case ClearQueue =>
      testingState.enqueuedMusicians.empty()
      testingState.testingMusicians = testingState.testingMusicians.empty
      testingState.servicingQueueTesterIds = testingState.servicingQueueTesterIds.empty
      TestingCometDispatcher ! ReloadPage
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

  private def updateStudentsPage(): Unit =
    StudentsCometDispatcher ! QueueSize(testingState.enqueuedMusicians.size)
}

object TestingManager {
  val defaultNextCallMins = Props.getInt("defaultNextCallMins") getOrElse 5

  def called = sortedEnqueued.take(testingState.timesUntilCall.count(_.getMillis <= 0))

  def sortedEnqueued: Seq[EnqueuedMusician] = testingState.enqueuedMusicians.items
}

case class EnqueuedMusician(musician: Musician, sortOrder: Long, nextPieceName: String)

case class TestingMusician(musician: Musician, tester: User, startingTime: DateTime, fromQueue: Option[MusicianQueue]) {
  var numAsmts = 0
  var lastActivity = startingTime
}

case class ChatMessage(time: DateTime, user: User, msg: String)

object TestingManagerMessages {
  case class EnqueueMusicians(enqueuedMusicians: Iterable[EnqueuedMusician])
  case class ToTop(ids: Iterable[Int])
  case class DequeueMusicians(ids: Iterable[Int])
  case class DequeueInstrumentsOfMusicians(musicianIds: Iterable[Int])
  case class TestMusician(testingMusician: TestingMusician)
  case class IncrementMusicianAssessmentCount(tester: User, musician: Musician)
  case class SetServicingQueue(tester: User, servicing: Boolean)
  case class SetCallAfterMins(tester: User, mins: Option[Int], callNow: Boolean)
  case class SetLastTestOrder(lastTestOrder: Boolean)
  case object ClearQueue
  case class Chat(chatMessage: ChatMessage)
  case object ClearChat
  case object Tick
}

object testingState {
  val enqueuedMusicians = new MusicianQueue()
  var testingMusicians = Set[TestingMusician]()
  var chatMessages = List[ChatMessage]()
  var callAfterMinsByTesterId = Map[Int, Option[Int]]().withDefaultValue(Some(TestingManager.defaultNextCallMins))
  var servicingQueueTesterIds = Set[Int]()
  var callNowTesterIds = Set[Int]()

  /** Returns a Duration for each tester servicing the queue. */
  def timesUntilCall = {
    val now = DateTime.now
    val testingMusiciansFromQueueByTesterId = testingMusicians.filter(_.fromQueue.nonEmpty).groupBy(_.tester.id)
    val durationsFromQueueServicingSessions = (for {
      (testerId, testingMusicians) <- testingMusiciansFromQueueByTesterId
      if servicingQueueTesterIds contains testerId
    } yield {
      val lastStudentStart = testingMusicians.map(_.startingTime).reduce {(a, b) => if (a > b) a else b}
      val sessionAge = new Interval(lastStudentStart, now).toDuration
      val opCallNow = if (callNowTesterIds.contains(testerId)) Some(0) else None
      opCallNow orElse callAfterMinsByTesterId(testerId) map(mins => {
        val expectedSessionDuration = new Duration(mins * 60000)
        expectedSessionDuration - sessionAge
      })
    }).flatten
    val zeroDurations = (servicingQueueTesterIds -- testingMusiciansFromQueueByTesterId.keySet).toSeq.
      map(n => new Duration(0))
    durationsFromQueueServicingSessions ++ zeroDurations
  }
}
