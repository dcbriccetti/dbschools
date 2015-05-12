package com.dbschools.mgb
package model

import scalaz._
import Scalaz._
import org.scala_tools.time.Imports._
import snippet.Authenticator
import snippet.Selection

object testingState {
  val enqueuedMusicians = new MusicianQueue()
  var testingMusicians = Set[TestingMusician]()
  var chatMessages = List[ChatMessage]()
  var callAfterMinsByTesterId = Map[Int, Option[Int]]().withDefaultValue(Some(TestingManager.defaultNextCallMins))
  var servicingQueueTesterIds = Map[Int, Selection]()
  var servicingQueueTesterIdsReset = false
  var callNowTesterIds = Set[Int]()
  var desktopNotifyByTesterId = Map[Int, Boolean]().withDefaultValue(false)
  def desktopNotify = Authenticator.opLoggedInUser.map(user => desktopNotifyByTesterId(user.id)) | false
  var specialSchedule = false

  def multiQueueItems = {
    val uniqueSels = servicingQueueTesterIds.values.toSet + Selection.AllItems
    if (uniqueSels.size == 1) Seq(QueueSubset(Selection.AllItems, enqueuedMusicians.items)) else {
      val sortedSels = uniqueSels.toSeq.sortBy(selSortOrderSpecificInstrumentsFirst)
      val unsortedSubsets = enqueuedMusicians.items.groupBy(m =>
        sortedSels.find(_.matches(m.instrumentId)) | Selection.NoItems
      ).map { case (sel, musicians) => QueueSubset(sel, musicians) }
      unsortedSubsets.toSeq.sortBy(qs => selSortOrderSpecificInstrumentsFirst(qs.sel)).reverse // “All” first
    }
  }

  /** Returns a TestingDuration for each tester servicing the queue. */
  def testingDurations = {
    val now = DateTime.now
    val testingMusiciansFromQueueByTesterId = testingMusicians.filter(_.fromQueue.nonEmpty).groupBy(_.tester.id)
    val durationsFromQueueServicingSessions =
    (for {
      (testerId, testingMusicians)  <- testingMusiciansFromQueueByTesterId
      selection                     <- servicingQueueTesterIds get testerId
      lastStudentStart  = testingMusicians.map(_.startingTime).reduce {(a, b) => if (a > b) a else b}
      sessionAge        = new Interval(lastStudentStart, now).toDuration
      opCallNow         = if (callNowTesterIds.contains(testerId)) Some(0) else None
    } yield {
      opCallNow orElse callAfterMinsByTesterId(testerId) map(mins => {
        val expectedSessionDuration = new Duration(mins * 60000)
        TesterDuration(testerId, selection, expectedSessionDuration - sessionAge)
      })
    }).flatten
    val zeroDurations = (servicingQueueTesterIds -- testingMusiciansFromQueueByTesterId.keySet).
      map {case (testerId, selection) => TesterDuration(testerId, selection, new Duration(0))}
    (durationsFromQueueServicingSessions ++ zeroDurations).toSeq.sortBy(_.duration.millis)
  }

  private def selSortOrderSpecificInstrumentsFirst(sel: Selection) = sel.value match {
      case Right(num) => num
      case Left(b) => Int.MaxValue // All and None sort to the end so a specific instrument matches first
    }
}

case class QueueSubset(sel: Selection, musicians: Seq[EnqueuedMusician])
