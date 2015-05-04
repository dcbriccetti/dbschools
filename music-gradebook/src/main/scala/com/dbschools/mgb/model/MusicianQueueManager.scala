package com.dbschools.mgb
package model

import model.TestingManagerMessages.ToTop

object MusicianQueueManager {
  def moveInterestingElementsToTop(mq: MusicianQueue): Unit = {
    val durs = testingState.testingDurations
    val specificInstruments = durs.flatMap(_.selection.right.toOption)
    type QueueIndex = Int
    var claimedQueueElement = Map[QueueIndex, EnqueuedMusician]()
    val items = mq.items.zipWithIndex
    def claim(mi: (EnqueuedMusician, QueueIndex)): Unit = claimedQueueElement += mi._2 -> mi._1
    def notClaimed(i: QueueIndex) = ! claimedQueueElement.contains(i)
    durs.foreach(dur => {
      dur.selection match {
        case Right(iid) =>
          items.find {case (m, i) => m.instrumentId == iid && notClaimed(i)}.foreach(claim)
        case Left(b) =>
          items.find {case (m, i) => ! specificInstruments.contains(m.instrumentId) && notClaimed(i)}.foreach(claim)
      }
    })
    val numDurs = durs.size
    val toTopMusicians = claimedQueueElement.collect {
      case (i, m) if i >= numDurs => m.musician.id
    }
    if (toTopMusicians.nonEmpty) Actors.testingManager ! ToTop(toTopMusicians)
  }
}
