package com.dbschools.mgb.model

import scalaz._
import Scalaz._
import org.squeryl.PrimitiveTypeMode._
import net.liftweb.common.Loggable
import com.dbschools.mgb.schema.AppSchema

object Combiner extends Loggable {
  /** Combines data for a student that has an old and current student ID */
  def combine(oldId: Int, currentId: Int): Unit = {
    (AppSchema.musicians.where(_.student_id.is === oldId    ).headOption <|*|>
     AppSchema.musicians.where(_.student_id.is === currentId).headOption) match {
      case Some((old, cur)) =>
        update(AppSchema.assessments)(a =>
          where(a.musician_id === old.musician_id.is)
          set(a.musician_id := cur.musician_id.is)
        )
        update(AppSchema.musicianGroups)(mg =>
          where(mg.musician_id === old.musician_id.is)
          set(mg.musician_id := cur.musician_id.is)
        )
        AppSchema.musicians.deleteWhere(_.musician_id.is === old.musician_id.is)
      case None =>
        logger.warn("Musician records not found")
    }
  }
}
