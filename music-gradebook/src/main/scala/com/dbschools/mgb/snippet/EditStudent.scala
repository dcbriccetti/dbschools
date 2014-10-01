package com.dbschools.mgb
package snippet

import scalaz._
import Scalaz._
import org.apache.log4j.Logger
import org.squeryl.PrimitiveTypeMode._
import net.liftweb._
import http._
import net.liftweb.util.FieldError
import bootstrap.liftweb.ApplicationPaths
import schema.{Musician, AppSchema}
import model.{SelectedMusician, Terms}

class EditStudent extends LiftScreen with SelectedMusician {
  private val log = Logger.getLogger(getClass)
  private val m = svSelectedMusician.is | Musician.createRecord

  private val grade = field(s"Grade in ${Terms.formatted(Terms.currentTerm)}",
    Terms.graduationYearAsGrade(m.graduation_year.get), minVal(1, "Invalid value"))

  override def screenFields = List(m.first_name, m.nickname, m.last_name, m.phoneticSpelling,
    m.permStudentId, grade, m.notes)

  def valUniqueStudentId(): Errors = {
    if (! m.isPersisted) {
      val opExisting = AppSchema.musicians.where(_.permStudentId.get === m.permStudentId.get).headOption
      opExisting.map(existing => FieldError(m.permStudentId, existing.name + " already has that student ID")).toList
    } else Nil
  }

  override def validations = valUniqueStudentId _ :: super.validations

  def finish(): Unit = {
    m.nickname.get match {
      case Some(s) if s.trim.length == 0 => m.nickname.set(None)
      case _ =>
    }
    m.graduation_year.set(Terms.gradeAsGraduationYear(grade.get))
    AppSchema.musicians.insertOrUpdate(m)
    val action = m.isPersisted ? "Edited" | "Created"
    log.info(s"$action musician $m")
    svSelectedMusician(Some(m))
    S.redirectTo(ApplicationPaths.studentDetails.href)
  }
}
