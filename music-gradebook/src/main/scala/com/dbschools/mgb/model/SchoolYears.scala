package com.dbschools.mgb.model

import java.sql.Timestamp
import org.scala_tools.time.Imports._
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.Query
import com.dbschools.mgb.schema.AppSchema

/**
  * Functions for dealing with school years. They are identified by the calendar year
  * during which the school year starts. For example,
  * in the 2012–13 school year, the value is 2012.
  */
object SchoolYears {
  val yearEndMonth = 7

  def current: Int = {
    val n = DateTime.now.withDayOfMonth(1)
    (if (n.getMonthOfYear >= yearEndMonth) n else n.minus(1.year)).getYear
  }

  def graduationYearAsGrade(graduationYear: Int, schoolYear: Int = current): Int =
    8 - (graduationYear - schoolYear)

  def gradeAsGraduationYear(grade: Int, schoolYear: Int = current): Int =
    8 - grade + schoolYear

  /** Returns all school years in a form suitable for a Select control.
    * For example: List(("2012", "2012–2013"), ("2011", "2011–2012"))
    */
  def allFormatted: List[(String, String)] = all.map(year => (year.toString, formatted(year))).toList

  def formatted(year: Int): String = {
    val enDash = '–'
    s"${year.toString.substring(2)}$enDash${(year + 1).toString.substring(2)}"
  }

  def all: Query[Int] =
    from(AppSchema.musicianGroups)(mg =>
      select(mg.school_year)
        orderBy mg.school_year.desc).distinct

  def toTs(dt: DateTime) = new Timestamp(dt.millis)

  def startDate(year: Int) = new DateTime(year, 8, 26, 0, 0, 0, 0) // todo don't hard code

  def endDate(year: Int): DateTime = startDate(year) + 1.year

  def fromToDates(year: Int = current): (DateTime, DateTime) = (startDate(year), endDate(year))

  def allFromToDates: (DateTime, DateTime) = {
    val at = all.toSeq
    (startDate(at.min), endDate(at.max))
  }
}
