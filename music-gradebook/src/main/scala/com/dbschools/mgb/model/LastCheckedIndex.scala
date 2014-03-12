package com.dbschools.mgb.model

object LastCheckedIndex {

  /** Returns -1 if no rows are checked, otherwise the 0-based index of the last checked row */
  def find[A](items: Iterable[A], selectedItems: Seq[A]): Int = {

    case class Indexes(currentIndex: Int, /** -1 means none checked */ highestCheckedIndex: Int)

    items.foldLeft(Indexes(0, -1))(
      (indexes, item) => Indexes(indexes.currentIndex + 1,
        if (selectedItems contains item)
          indexes.currentIndex
        else
          indexes.highestCheckedIndex
      )
    ).highestCheckedIndex
  }
}
