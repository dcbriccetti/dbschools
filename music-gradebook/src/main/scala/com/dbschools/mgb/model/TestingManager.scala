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
import com.dbschools.mgb.comet.TestingCometActorMessages.SetNumWaitingRoom

class TestingManager extends Actor {
  val log = Logger.getLogger(getClass)
  import StudentsCometActorMessages._
  import TestingCometActorMessages.{ReloadPage, MoveMusician, UpdateAssessmentCount}
  import TestingManagerMessages._
  import TestingManager._
  import comet.NoticesMessages._
  import comet.StudentMessages._

  val defaultNextCallMins = Props.getInt("defaultNextCallMins") getOrElse 5
  var numToCall = -1

  def receive = {

    case Tick =>
      if (numToCall != testingState.numToCall) {
        numToCall = testingState.numToCall
        log.info(s"Number to call is now $numToCall")
        TestingCometDispatcher ! SetNumWaitingRoom(numToCall)
      }

    case SetMinsToCall(testerId, minsToCall) =>
      testingState.setCallTime(testerId, minsToCall)

    case EnqueueMusicians(scheds) =>
      testingState.enqueuedMusicians ++= scheds
      TestingCometDispatcher ! ReloadPage
      updateStudentsPage()
      StudentCometDispatcher ! Next(opNext)

    case DequeueMusicians(ids) =>
      val idsSet = ids.toSet
      val sms = testingState.enqueuedMusicians.filter(idsSet contains _.musician.id)
      if (sms.nonEmpty) {
        testingState.enqueuedMusicians --= sms
        TestingCometDispatcher ! ReloadPage
        updateStudentsPage()
        StudentCometDispatcher ! Next(opNext)
      }

    case TestMusician(testingMusician) =>
      testingState.enqueuedMusicians.find(_.musician == testingMusician.musician).foreach(sm => {
        testingState.enqueuedMusicians -= sm
        val on = opNext
        val opNextId = on.map(_.musician.id)
        testingState.testingMusicians += testingMusician
        TestingCometDispatcher ! MoveMusician(testingMusician, opNextId)
        StudentCometDispatcher ! Next(on)
        testingState.setCallTime(testingMusician.tester.id, defaultNextCallMins)
      })
      updateStudentsPage()

    case IncrementMusicianAssessmentCount(tester, musician) =>
      val tm = testingState.testingMusicians.find(tm => tm.tester.id == tester.id && tm.musician.id == musician.id) | {
        // This student wasn’t selected from the queue, so make a TestingMusician record now
        val newTm = TestingMusician(musician, tester, DateTime.now)
        testingState.testingMusicians += newTm
        TestingCometDispatcher ! MoveMusician(newTm, None)
        newTm
      }
      tm.numAsmts += 1
      TestingCometDispatcher ! UpdateAssessmentCount(tm)

    case ClearQueue =>
      testingState.enqueuedMusicians = testingState.enqueuedMusicians.empty
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

  def opNext = sortedEnqueued.headOption

  def sortedEnqueued: Seq[EnqueuedMusician] = {
    testingState.enqueuedMusicians.toSeq.sortBy(_.sortOrder)
  }
}

case class EnqueuedMusician(musician: Musician, sortOrder: Long, nextPieceName: String)

case class TestingMusician(musician: Musician, tester: User, time: DateTime) {
  var numAsmts = 0
}

case class ChatMessage(time: DateTime, user: User, msg: String)

object TestingManagerMessages {
  case class EnqueueMusicians(enqueuedMusicians: Iterable[EnqueuedMusician])
  case class DequeueMusicians(ids: Iterable[Int])
  case class TestMusician(testingMusician: TestingMusician)
  case class IncrementMusicianAssessmentCount(tester: User, musician: Musician)
  case object ClearQueue
  case class Chat(chatMessage: ChatMessage)
  case object ClearChat
  case object Tick
  case class SetMinsToCall(testerId: Int, minsToCall: Int)
}

object testingState {
  var enqueuedMusicians = Set[EnqueuedMusician]()
  var testingMusicians = Set[TestingMusician]()
  var chatMessages = List[ChatMessage]()
  var callNextTime = Map[Int, DateTime]()
  def numToCall = callNextTime.values.map(callTime => if (DateTime.now > callTime) 1 else 0).sum
  def setCallTime(testerId: Int, mins: Int) = callNextTime += testerId -> DateTime.now.plusMinutes(mins)
}
