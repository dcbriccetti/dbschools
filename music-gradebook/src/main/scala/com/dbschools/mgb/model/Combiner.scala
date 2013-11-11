package com.dbschools.mgb.model

import scalaz._
import Scalaz._
import org.squeryl.PrimitiveTypeMode._
import net.liftweb.common.Loggable
import com.dbschools.mgb.schema.AppSchema

object Combiner extends Loggable {
  /** Combines data for a student that has an old and current student ID */
  def combine(oldId: Int, currentId: Int): Unit = {
    AppSchema.musicians.where(_.student_id.get === oldId).headOption tuple
      AppSchema.musicians.where(_.student_id.get === currentId).headOption match {
      case Some((old, cur)) =>
        update(AppSchema.assessments)(a =>
          where(a.musician_id === old.musician_id.get)
          set(a.musician_id := cur.musician_id.get)
        )
        update(AppSchema.musicianGroups)(mg =>
          where(mg.musician_id === old.musician_id.get)
          set(mg.musician_id := cur.musician_id.get)
        )
        AppSchema.musicians.deleteWhere(_.musician_id.get === old.musician_id.get)
      case None =>
        logger.warn("Musician records not found")
    }
  }
}
