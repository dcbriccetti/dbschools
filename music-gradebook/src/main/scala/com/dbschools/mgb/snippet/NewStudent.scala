package com.dbschools.mgb.snippet

import org.squeryl.PrimitiveTypeMode._
import net.liftweb._
import net.liftweb.common.Loggable
import http._
import net.liftweb.util.FieldError
import com.dbschools.mgb.schema.{Musician, AppSchema}
import com.dbschools.mgb.model.Terms

class NewStudent extends LiftScreen with Loggable {
  private val musician = Musician.createRecord

  private val grade = field(s"Grade in ${Terms.formatted(Terms.currentTerm)}", 0, minVal(1, "Invalid value"))

  override def screenFields = List(musician.last_name, musician.first_name, musician.student_id, grade)

  def valUniqueStudentId(): Errors = {
    val opExisting = AppSchema.musicians.where(_.student_id.is === musician.student_id.is).headOption
    opExisting.map(existing => FieldError(musician.student_id, existing.name + " already has that student ID")).toList
  }

  override def validations = valUniqueStudentId _ :: super.validations

  def finish(): Unit = {
    musician.graduation_year.set(Terms.gradeAsGraduationYear(grade.get))
    AppSchema.musicians.insert(musician)
    S.redirectTo(Students.urlToDetails(musician))
  }
}
