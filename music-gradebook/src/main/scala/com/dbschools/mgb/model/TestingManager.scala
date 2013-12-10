package com.dbschools.mgb
package model

import akka.actor.Actor
import org.joda.time.DateTime
import comet.{NoticesDispatcher, StudentCometDispatcher, StudentsCometDispatcher, StudentsCometActorMessages,
  TestingCometDispatcher, TestingCometActorMessages}
import schema.Musician
import com.dbschools.mgb.schema.User

class TestingManager extends Actor {
  import StudentsCometActorMessages._
  import TestingCometActorMessages.{ReloadPage, MoveMusician, UpdateAssessmentCount}
  import TestingManagerMessages._
  import TestingManager._
  import comet.NoticesMessages._
  import comet.StudentMessages._


  def receive = {

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
      })
      updateStudentsPage()

    case IncrementMusicianAssessmentCount(testerId, musicianId) =>
      testingState.testingMusicians.find(tm => tm.tester.id == testerId && tm.musician.id == musicianId).foreach(tm => {
        tm.numAsmts += 1
        TestingCometDispatcher ! UpdateAssessmentCount(tm)
      })

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
  case class IncrementMusicianAssessmentCount(testerId: Int, musicianId: Int)
  case object ClearQueue
  case class Chat(chatMessage: ChatMessage)
  case object ClearChat
}

object testingState {
  var enqueuedMusicians = Set[EnqueuedMusician]()
  var testingMusicians = Set[TestingMusician]()
  var chatMessages = List[ChatMessage]()
}
