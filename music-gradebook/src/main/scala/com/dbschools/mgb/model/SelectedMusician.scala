package com.dbschools.mgb.model

import net.liftweb.http.SessionVar
import com.dbschools.mgb.schema.Musician

/** Some types operate on one selected musician */
trait SelectedMusician {
  object svSelectedMusician extends SessionVar[Option[Musician]](None)
  def opMusician = svSelectedMusician.is
}
