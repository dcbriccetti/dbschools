package com.dbschools.mgb.model

import java.sql.Timestamp

import org.scala_tools.time.Imports._
import org.squeryl.PrimitiveTypeMode._
import com.dbschools.mgb.schema.AppSchema
import org.squeryl.Query

/**
 * Functions for dealing with terms. A term is the year during which the term starts. For example,
 * in the 2012–13 term, the value is 2012.
 */
object Terms {
  val yearEndMonth = 7

  def currentTerm: Int = {
    val n = DateTime.now.withDayOfMonth(1)
    (if (n.getMonthOfYear >= yearEndMonth) n else n.minus(1.year)).getYear
  }

  def nextTerm: Int = currentTerm + 1

  def graduationYearAsGrade(graduationYear: Int, schoolYear: Int = currentTerm): Int =
    8 - (graduationYear - schoolYear)

  def gradeAsGraduationYear(grade: Int, schoolYear: Int = currentTerm): Int =
    8 - grade + schoolYear

  /** Returns all school years in a form suitable for a Select control.
    * For example: List(("2012", "2012–2013"), ("2011", "2011–2012"))
    */
  def allTermsFormatted: List[(String, String)] = allTerms.map(year => (year.toString, formatted(year))).toList

  def formatted(year: Int): String = {
    val enDash = '–'
    s"${year.toString.substring(2)}$enDash${(year + 1).toString.substring(2)}"
  }

  def allTerms: Query[Int] = from(AppSchema.musicianGroups)(mg => select(mg.school_year) orderBy(mg.school_year.desc)).distinct

  def toTs(dt: DateTime) = new Timestamp(dt.millis)

  def termStart(term: Int) = new DateTime(term, 8, 26, 0, 0, 0, 0) // todo don't hard code

  def termEnd(term: Int): DateTime = termStart(term) + 1.year

  def termFromTo(term: Int = currentTerm): (_root_.org.scala_tools.time.Imports.DateTime, DateTime) = (termStart(term), termEnd(term))

  def allTermsFromTo: (_root_.org.scala_tools.time.Imports.DateTime, DateTime) = {
    val at = allTerms.toSeq
    (termStart(at.min), termEnd(at.max))
  }
}
