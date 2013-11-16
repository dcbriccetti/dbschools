package com.dbschools.mgb
package model

import akka.actor.Actor
import org.joda.time.DateTime
import comet.{StudentsCometDispatcher, StudentsCometActorMessages, TestingCometDispatcher, TestingCometActorMessages}
import schema.Musician

class TestingManager extends Actor {
  import StudentsCometActorMessages._
  import TestingCometActorMessages._
  import TestingManagerMessages._

  def receive = {

    case EnqueueMusicians(scheds) =>
      testingState.enqueuedMusicians ++= scheds
      TestingCometDispatcher ! RedisplaySchedule
      updateStudentsPage()

    case DequeueMusicians(ids) =>
      val idsSet = ids.toSet
      val sms = testingState.enqueuedMusicians.filter(idsSet contains _.musician.id)
      if (sms.nonEmpty) {
        testingState.enqueuedMusicians --= sms
        TestingCometDispatcher ! RedisplaySchedule
        updateStudentsPage()
      }

    case TestMusician(testingMusician) =>
      testingState.enqueuedMusicians.find(_.musician == testingMusician.musician).foreach(sm => {
        testingState.enqueuedMusicians -= sm
        var opNextId = testingState.enqueuedMusicians.toSeq.sortBy(_.sortOrder).headOption.map(_.musician.id)
        testingState.testingMusicians += testingMusician
        TestingCometDispatcher ! MoveMusician(testingMusician, opNextId)
      })
      updateStudentsPage()

    case IncrementMusicianAssessmentCount(musicianId) =>
      testingState.testingMusicians.find(_.musician.id == musicianId).foreach(tm => {
        tm.numAsmts += 1
        TestingCometDispatcher ! UpdateAssessmentCount(tm)
      })

    case ClearQueue =>
      testingState.enqueuedMusicians = testingState.enqueuedMusicians.empty
      testingState.testingMusicians = testingState.testingMusicians.empty
      TestingCometDispatcher ! RedisplaySchedule
      updateStudentsPage()
  }

  private def updateStudentsPage(): Unit =
    StudentsCometDispatcher ! QueueSize(testingState.enqueuedMusicians.size)
}

case class EnqueuedMusician(musician: Musician, sortOrder: Long, nextPieceName: String)

case class TestingMusician(musician: Musician, testerName: String, time: DateTime) {
  var numAsmts = 0
}

object TestingManagerMessages {
  case class EnqueueMusicians(enqueuedMusicians: Iterable[EnqueuedMusician])
  case class DequeueMusicians(ids: Iterable[Int])
  case class TestMusician(testingMusician: TestingMusician)
  case class IncrementMusicianAssessmentCount(musicianId: Int)
  case object ClearQueue
}

object testingState {
  var enqueuedMusicians = Set[EnqueuedMusician]()
  var testingMusicians = Set[TestingMusician]()
}
