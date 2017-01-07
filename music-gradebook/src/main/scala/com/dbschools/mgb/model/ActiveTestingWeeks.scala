package com.dbschools.mgb
package model

import org.apache.log4j.Logger
import org.joda.time.{DateTime, Weeks}
import org.squeryl.PrimitiveTypeMode._
import Terms.toTs
import schema.AppSchema

/** Given test dates, provides the week numbers of weeks during which testing took place. */
class ActiveTestingWeeks() {
  private val log = Logger.getLogger(getClass)
  private var activeWeeksByYear: Map[Int, Set[Int]] = Map().withDefaultValue(Set[Int]())

  /** Returns the one-based week numbers when testing occurred, for the given school year */
  def forSchoolYear(year: Int): Seq[Int] = synchronized {
    activeWeeksByYear(year).toSeq.sorted
  }

  /** Returns the one-based week numbers when testing occurred, for the given term within the given school year */
  def forSchoolTerm(year: Int, schoolYearStart: DateTime, termStart: DateTime): Seq[Int] = {
    val termStartWeekNum = ActiveTestingWeeks.weekNum(schoolYearStart, termStart)
    forSchoolYear(year).filter(_ >= termStartWeekNum)
  }

  /**
    * Records the week numbers of the given dates, based on the given date of the start of the school year.
    * @param dates the dates that tests took place
    * @param schoolYearStart the start of the school year
    */
  def addFrom(dates: Iterable[DateTime], schoolYearStart: DateTime): Unit = synchronized {
    dates.foreach { date =>
      val setForSchoolYear = activeWeeksByYear(Terms.currentTerm) + ActiveTestingWeeks.weekNum(schoolYearStart, date)
      activeWeeksByYear += Terms.currentTerm -> setForSchoolYear
    }
  }

  /** Loads the active testing weeks from the assessment table */
  def loadCurrentSchoolYearFromDatabase(schoolYearStart: DateTime): Unit = inTransaction {
    val dateTimesQ = from(AppSchema.assessments)(a =>
      where(a.assessment_time >= toTs(schoolYearStart))
        select new DateTime(a.assessment_time.getTime)
    ).distinct

    addFrom(dateTimesQ, schoolYearStart)
  }
}

object ActiveTestingWeeks {
  /** Returns the one-based week number of a date, given the date of the start of the school year */
  def weekNum(schoolYearStart: DateTime, date: DateTime): Int = {
    Weeks.weeksBetween(schoolYearStart, date).getWeeks + 1
  }
}
