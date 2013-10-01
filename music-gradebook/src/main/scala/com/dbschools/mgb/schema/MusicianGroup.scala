package com.dbschools.mgb.schema

import org.squeryl.PrimitiveTypeMode._
import com.dbschools.mgb.model.Cache

case class MusicianGroup(
  id:             Int,
  musician_id:    Int,
  group_id:       Int,
  instrument_id:  Int,
  school_year:    Int
)

case class MusicianGroupMusician(mg: MusicianGroup, m: Musician)

object MusicianGroup {
  def selectedMusicians(term: Option[Int] = None, musicGroupId: Option[Int] = None,
      instrumentId: Option[Int] = None) =
    from(AppSchema.musicianGroups, AppSchema.musicians)((mg, m) =>
    where(mg.musician_id === m.musician_id.is and mg.school_year === term.? and
      mg.group_id === musicGroupId.? and mg.instrument_id === instrumentId.?)
    select MusicianGroupMusician(mg, m))

  def selectedInstruments(term: Option[Int] = None, musicGroupId: Option[Int] = None) = {
    val instrumentsMap = Cache.instruments.map(i => i.id -> i).toMap
    from(AppSchema.musicianGroups, AppSchema.instruments)((mg, i) =>
      where(mg.instrument_id === i.id and mg.school_year === term.? and
        mg.group_id === musicGroupId.?)
      groupBy i.id
      compute count(i.name.is)
    ).map(g => instrumentsMap(g.key) -> g.measures).toSeq.sortBy(_._1.sequence.is)
  }
}