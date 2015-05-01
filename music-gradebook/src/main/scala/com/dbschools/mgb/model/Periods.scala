package com.dbschools.mgb.model

import org.joda.time.DateTimeConstants
import org.scala_tools.time.Imports._

object Periods {
  case class SimpleTime(hour: Int, minute: Int) {
    def toDateTime = DateTime.now.withTimeAtStartOfDay.withHourOfDay(hour).withMinuteOfHour(minute)
  }
  sealed trait TimeClass
  object InSpecialSchedule extends TimeClass
  object NotInPeriod extends TimeClass

  case class Period(num: Int, start: SimpleTime, end: SimpleTime) extends TimeClass {
    def within(t: DateTime) = start.toDateTime.millis <= t.millis && t.millis < end.toDateTime.millis
    def timeRemainingMs = end.toDateTime.millis - DateTime.now.millis
    def totalSecs = (end.toDateTime.millis - start.toDateTime.millis) / 1000
    def timePassedSecs = (DateTime.now.millis - start.toDateTime.millis) / 1000
  }
  object Period {
    def apply(num: Int, sh: Int, sm: Int, eh: Int, em: Int): Period = Period(num, SimpleTime(sh, sm), SimpleTime(eh, em))
  }

  // todo Create database tables and import feature
  private val periodsNormal = Vector(
    Period(1, 8, 15, 9, 2),
    Period(2, 9, 5, 9, 50),
    Period(3, 9, 53, 10, 38),
    Period(4, 10, 56, 11, 41),
    Period(5, 11, 44, 12, 29),
    Period(6, 13, 20, 14, 5),
    Period(7, 14, 8, 14, 55),
    Period(8, 14, 58, 15, 43)
  )

  private val periodsWeds = Vector(
    Period(1, 9, 0, 9, 40),
    Period(2, 9, 43, 10, 23),
    Period(3, 10, 38, 11, 18),
    Period(4, 11, 21, 12, 1),
    Period(5, 12, 4, 12, 44),
    Period(6, 13, 30, 14, 10),
    Period(7, 14, 13, 14, 53),
    Period(8, 14, 56, 15, 41)
  )


  def periodWithin: TimeClass = if (testingState.specialSchedule) InSpecialSchedule else
    periodsToday.find(_.within(DateTime.now)) getOrElse NotInPeriod

  private def periodsToday = DateTime.now.getDayOfWeek match {
    case DateTimeConstants.WEDNESDAY  => periodsWeds
    case _                            => periodsNormal
  }
}
