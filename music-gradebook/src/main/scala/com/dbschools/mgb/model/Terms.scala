package com.dbschools.mgb.model

import java.sql.Timestamp
import org.scala_tools.time.Imports._
import org.squeryl.PrimitiveTypeMode._
import com.dbschools.mgb.schema.AppSchema

/**
 * Functions for dealing with terms. A term is the year during which the term starts. For example,
 * in the 2012–13 term, the value is 2012.
 */
object Terms {
  val yearEndMonth = 7

  def currentTerm = {
    val n = DateTime.now.withDayOfMonth(1)
    (if (n.getMonthOfYear >= yearEndMonth) n else n.minus(1.year)).getYear
  }

  def termFromTo(term: Int = currentTerm) = {
    val dtFrom = {
      val n = DateTime.now.withDayOfMonth(1)
      if (n.getMonthOfYear >= yearEndMonth) n.withMonth(yearEndMonth) else n.withMonth(yearEndMonth).minus(1.year)
    }
    val dtTo = dtFrom + 1.year
    (dtFrom, dtTo)
  }

  def graduationYearAsGrade(graduationYear: Int, schoolYear: Int = currentTerm) =
    8 - (graduationYear - schoolYear)

  def gradeAsGraduationYear(grade: Int, schoolYear: Int = currentTerm) =
    8 - grade + schoolYear

  /** Returns all school years in a form suitable for a Select control.
    * For example: List(("2012", "2012–2013"), ("2011", "2011–2012"))
    */
  def allTermsFormatted = allTerms.map(year => (year.toString, formatted(year))).toList

  def formatted(year: Int) = {
    val enDash = '–'
    "%s%c%s".format(year.toString.substring(2), enDash, (year + 1).toString.substring(2))
  }

  def allTerms = from(AppSchema.musicianGroups)(mg => select(mg.school_year) orderBy(mg.school_year desc)).distinct

  def toTs(dt: DateTime) = new Timestamp(dt.millis)

  def termEnd(term: Int) = new DateTime(term + 1, yearEndMonth, 1, 0, 0, 0, 0)
}
