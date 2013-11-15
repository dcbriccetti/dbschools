package com.dbschools.mgb.snippet

import org.apache.log4j.Logger
import org.squeryl.PrimitiveTypeMode._
import net.liftweb._
import http._
import net.liftweb.util.FieldError
import bootstrap.liftweb.ApplicationPaths
import com.dbschools.mgb.schema.{Musician, AppSchema}
import com.dbschools.mgb.model.{SelectedMusician, Terms}

class NewStudent extends LiftScreen with SelectedMusician {
  private val log = Logger.getLogger(getClass)
  private val musician = Musician.createRecord

  private val grade = field(s"Grade in ${Terms.formatted(Terms.currentTerm)}", 0, minVal(1, "Invalid value"))

  override def screenFields = List(musician.last_name, musician.first_name, musician.student_id, grade)

  def valUniqueStudentId(): Errors = {
    val opExisting = AppSchema.musicians.where(_.student_id.get === musician.student_id.get).headOption
    opExisting.map(existing => FieldError(musician.student_id, existing.name + " already has that student ID")).toList
  }

  override def validations = valUniqueStudentId _ :: super.validations

  def finish(): Unit = {
    musician.graduation_year.set(Terms.gradeAsGraduationYear(grade.get))
    AppSchema.musicians.insert(musician)
    log.info("Created musician " + musician)
    svSelectedMusician(Some(musician))
    S.redirectTo(ApplicationPaths.studentDetails.href)
  }
}
