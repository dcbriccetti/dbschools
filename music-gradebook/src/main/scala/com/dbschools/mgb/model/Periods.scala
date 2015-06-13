package com.dbschools.mgb.model

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
    def startMs = start.toDateTime.millis
    def endMs = end.toDateTime.millis

    def formatted = {
      def fh(h: Int) = (if (h > 12) h - 12 else h).toString
      def fm(m: Int) = f"$m%02d"

      val sh = fh(start.hour)
      val sm = fm(start.minute)
      val eh = fh(end.hour)
      val em = fm(end.minute)

      s"$sh:$sm–$eh:$em"
    }
  }

  /** “Block period” */
  case class Bp(sh: Int, sm: Int, eh: Int, em: Int)
  
  object Period {
    def apply(num: Int, sh: Int, sm: Int, eh: Int, em: Int): Period = Period(num, SimpleTime(sh, sm), SimpleTime(eh, em))
    def apply(num: Int, bp: Bp): Period = 
      Period(num, SimpleTime(bp.sh, bp.sm), SimpleTime(bp.eh, bp.em))
  }

  private val monFri = Vector(
    Period(1, 8, 15, 9, 2),
    Period(2, 9, 6, 9, 51),
    Period(3, 9, 55, 10, 40),
    Period(4, 10, 56, 11, 41),
    Period(5, 11, 45, 12, 30),
    Period(6, 13, 19, 14, 4),
    Period(7, 14, 8, 14, 53)
  )

  private val bp1 = Bp(8, 15, 9, 22)
  private val bp2 = Bp(9, 26, 10, 31)
  private val bp3 = Bp(10, 45, 11, 50)
  private val bp4 = Bp(12, 39, 13, 44)
  private val bp5 = Bp(13, 48, 14, 53)

  private val tue = Vector(
    Period(1, bp1),
    Period(3, bp2),
    Period(5, bp3),
    Period(6, bp4),
    Period(7, bp5)
  )

  private val wed = Vector(
    Period(2, 9, 24, 10, 31),
    Period(4, bp3),
    Period(5, bp4),
    Period(6, bp5)
  )

  private val thu = Vector(
    Period(1, bp1),
    Period(2, bp2),
    Period(3, bp3),
    Period(4, bp4),
    Period(7, bp5)
  )
  
  private val week = Vector(monFri, tue, wed, thu, monFri)

  def periodWithin: TimeClass = if (testingState.specialSchedule) InSpecialSchedule else
    periodsToday.find(_.within(DateTime.now)) getOrElse NotInPeriod

  private def periodsToday = DateTime.now.getDayOfWeek match {
    case d if d >= 1 && d <= 5 => week(d - 1)
    case _                     => week.head // Use Monday for out of range
  }
}
