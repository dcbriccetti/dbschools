package com.dbschools.mgb.schema

import org.squeryl.PrimitiveTypeMode._
import com.dbschools.mgb.model.Terms

case class MusicianGroup(
  id:             Int,
  musician_id:    Int,
  group_id:       Int,
  instrument_id:  Int,
  school_year:    Int
)

object MusicianGroup {
  def musiciansInCurrentTerm =
    from(AppSchema.musicianGroups, AppSchema.musicians)((mg, m) =>
    where(mg.musician_id === m.musician_id.is and mg.school_year === Terms.currentTerm)
    select(m))
}