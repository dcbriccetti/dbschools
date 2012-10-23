package com.dbschools.mgb

import org.scala_tools.time.Imports._

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
}
