package com.dbschools.mgb.model

class MusicianQueue {
  private var q = Vector[EnqueuedMusician]()

  def isEmpty = q.isEmpty
  def nonEmpty = q.nonEmpty
  def size = q.size
  def exists(m: Int) = q.exists(_.musician.id == m)

  def ++=(musicians: Iterable[EnqueuedMusician]): Unit = synchronized {
    val currentIds = q.map(_.musician.id).toSet
    q ++= musicians.filterNot(currentIds contains _.musician.id)
  }

  def -=(m: EnqueuedMusician): Unit = synchronized {
    q = q.filterNot(_ == m)
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
    q = q.filterNot(ems.contains)
    ems.size
  }

  def moveToTop(musicianIds: Iterable[Int]): Unit = synchronized {
    val ids = musicianIds.toSet
    def matching(m: EnqueuedMusician) = ids.contains(m.musician.id)
    q = q.filter(matching) ++ q.filterNot(matching)
  }

  def empty(): Unit = synchronized {
    q = Vector()
  }

  def items = q
}
