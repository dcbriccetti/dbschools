package com.dbschools.mgb.snippet

import net.liftweb.http.SessionVar
import com.dbschools.mgb.schema.Musician

/** Some types operate on one selected musician */
trait SelectedMusician {
  object svSelectedMusician extends SessionVar[Option[Musician]](None)
  val opMusician = svSelectedMusician.is
}
