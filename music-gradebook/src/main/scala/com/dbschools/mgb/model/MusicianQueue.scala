package com.dbschools.mgb.model

class MusicianQueue {
  private var q = Set[EnqueuedMusician]()

  def isEmpty = q.isEmpty
  def nonEmpty = q.nonEmpty
  def size = q.size
  def exists(m: Int) = q.exists(_.musician.id == m)

  def ++=(musicians: Iterable[EnqueuedMusician]): Unit = synchronized {
    val currentIds = q.map(_.musician.id).toSet
    q ++= musicians.filterNot(currentIds contains _.musician.id)
  }

  def -=(m: EnqueuedMusician): Unit = synchronized {
    q -= m
  }

  def -=(musicianId: Int): Boolean = {
    val opEm = q.find(_.musician.musician_id.get == musicianId)
    opEm.foreach(testingState.enqueuedMusicians.-=)
    opEm.nonEmpty
  }

  /** Deletes any enqueued musicians with the specified IDs, and returns the number deleted */
  def --=(musicianIds: Iterable[Int]) = synchronized {
    val idsSet = musicianIds.toSet
    val ems = q.filter(idsSet contains _.musician.id)
    q --= ems
    ems.size
  }

  def empty(): Unit = synchronized {
    q = q.empty
  }

  def sorted = q.toSeq.sortBy(_.sortOrder)
}
