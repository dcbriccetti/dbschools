package com.dbschools.mgb.model

import scalaz._
import Scalaz._
import org.squeryl.PrimitiveTypeMode._
import net.liftweb.common.Loggable
import com.dbschools.mgb.schema.AppSchema

object Combiner extends Loggable {
  /** Combines data for a student that has an old and current student ID */
  def combine(oldId: Int, currentId: Int) {
    (AppSchema.musicians.where(m => m.student_id === oldId).headOption <|*|>
    AppSchema.musicians.where(m => m.student_id === currentId).headOption) match {
      case Some((old, cur)) =>
        update(AppSchema.assessments)(a =>
          where(a.musician_id === old.musician_id)
          set(a.musician_id := cur.musician_id)
        )
        update(AppSchema.musicianGroups)(mg =>
          where(mg.musician_id === old.musician_id)
          set(mg.musician_id := cur.musician_id)
        )
        AppSchema.musicians.deleteWhere(m => m.musician_id === old.musician_id)
      case None =>
        logger.warn("Musician records not found")
    }
  }
}
