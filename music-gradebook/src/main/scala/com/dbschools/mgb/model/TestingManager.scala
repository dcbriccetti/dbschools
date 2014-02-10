package com.dbschools.mgb
package model

import scalaz._
import Scalaz._
import akka.actor.Actor
import org.apache.log4j.Logger
import org.scala_tools.time.Imports._
import comet.{NoticesDispatcher, StudentCometDispatcher, StudentsCometDispatcher, StudentsCometActorMessages,
  TestingCometDispatcher, TestingCometActorMessages}
import schema.Musician
import com.dbschools.mgb.schema.User
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

    case EnqueueMusicians(scheds) =>
      testingState.enqueuedMusicians ++= scheds
      TestingCometDispatcher ! ReloadPage
      updateStudentsPage()
      StudentCometDispatcher ! Next(opNext)

    case DequeueMusicians(ids) =>
      if ((testingState.enqueuedMusicians --= ids) > 0) {
        TestingCometDispatcher ! ReloadPage
        updateStudentsPage()
        StudentCometDispatcher ! Next(opNext)
      }

    case TestMusician(testingMusician) =>
      if (testingState.enqueuedMusicians -= testingMusician.musician.id) {
        val on = opNext
        val opNextId = on.map(_.musician.id)
        testingState.testingMusicians += testingMusician
        TestingCometDispatcher ! MoveMusician(testingMusician, opNextId, testingState.timesUntilCall)
        StudentCometDispatcher ! Next(on)
      }
      updateStudentsPage()

    case IncrementMusicianAssessmentCount(tester, musician) =>
      val tm = testingState.testingMusicians.find(tm => tm.tester.id == tester.id && tm.musician.id == musician.id) | {
        // This student wasn’t selected from the queue, so make a TestingMusician record now
        val newTm = TestingMusician(musician, tester, DateTime.now, None)
        testingState.testingMusicians += newTm
        TestingCometDispatcher ! MoveMusician(newTm, None, testingState.timesUntilCall)
        newTm
      }
      tm.numAsmts += 1
      tm.lastActivity = DateTime.now
      TestingCometDispatcher ! UpdateAssessmentCount(tm)

    case SetCallAfterMins(user, mins) =>
      testingState.callAfterMinsByTesterId += user.id -> mins

    case ClearQueue =>
      testingState.enqueuedMusicians.empty()
      testingState.testingMusicians = testingState.testingMusicians.empty
      TestingCometDispatcher ! ReloadPage
      updateStudentsPage()
      StudentCometDispatcher ! Next(None)

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

  def opNext = sortedEnqueued.headOption

  def sortedEnqueued: Seq[EnqueuedMusician] = {
    testingState.enqueuedMusicians.sorted
  }
}

case class EnqueuedMusician(musician: Musician, sortOrder: Long, nextPieceName: String)

case class TestingMusician(musician: Musician, tester: User, startingTime: DateTime, fromQueue: Option[MusicianQueue]) {
  var numAsmts = 0
  var lastActivity = startingTime
}

case class ChatMessage(time: DateTime, user: User, msg: String)

object TestingManagerMessages {
  case class EnqueueMusicians(enqueuedMusicians: Iterable[EnqueuedMusician])
  case class DequeueMusicians(ids: Iterable[Int])
  case class TestMusician(testingMusician: TestingMusician)
  case class IncrementMusicianAssessmentCount(tester: User, musician: Musician)
  case class SetCallAfterMins(tester: User, mins: Option[Int])
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

  def timesUntilCall = {
    val now = DateTime.now
    testingMusicians.filter(_.fromQueue.nonEmpty).groupBy(_.tester.id).flatMap {
      case (id, tms) =>
        val lastStudentStart = tms.map(_.startingTime).reduce {(a, b) => if (a > b) a else b}
        val sessionAge = new Interval(lastStudentStart, now).toDuration
        callAfterMinsByTesterId(id).map(mins => {
          val expectedSessionDuration = new Duration(mins * 60000)
          expectedSessionDuration - sessionAge
        })
    }
  }
}
