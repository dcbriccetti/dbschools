package com.dbschools.mgb.snippet

import scalaz._
import Scalaz._
import org.squeryl.PrimitiveTypeMode._
import net.liftweb._
import common.Loggable
import http._
import com.dbschools.mgb.schema.{Musician, AppSchema}

object NewStudent extends LiftScreen with Loggable {
  val musician = Musician.createRecord
  addFields(() => musician)

  def valUniqueStudentId(): Errors =
    AppSchema.musicians.where(_.student_id.is === musician.student_id.is).headOption match {
      case Some(existing) => existing.name + " already has that student ID"
      case _              => Nil
    }

  override def validations = valUniqueStudentId _ :: super.validations

  def finish(): Unit = AppSchema.musicians.insert(musician)
}
