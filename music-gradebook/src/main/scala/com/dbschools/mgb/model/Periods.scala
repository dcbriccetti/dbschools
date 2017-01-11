package com.dbschools.mgb.model

import org.scala_tools.time.Imports._

object Periods {
  case class SimpleTime(hour: Int, minute: Int) {
    def toDateTime(dateTime: DateTime = DateTime.now): DateTime =
      dateTime.withTimeAtStartOfDay.withHourOfDay(hour).withMinuteOfHour(minute)
  }
  sealed trait TimeClass
  object InSpecialSchedule extends TimeClass
  object NotInPeriod extends TimeClass

  case class Period(num: Int, start: SimpleTime, end: SimpleTime) extends TimeClass {
    def within(t: DateTime): Boolean = start.toDateTime(t).millis <= t.millis && t.millis < end.toDateTime(t).millis
    def timeRemainingMs: Long = end.toDateTime().millis - DateTime.now.millis
    def totalSecs: Long = (end.toDateTime().millis - start.toDateTime().millis) / 1000
    def timePassedSecs: Long = (DateTime.now.millis - start.toDateTime().millis) / 1000
    def startMs: Long = start.toDateTime().millis
    def endMs: Long = end.toDateTime().millis

    def formatted: String = {
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

  private val monFriAlt1 = Vector(
    Period(1,  8, 15,  8, 58),
    Period(2,  9,  2,  9, 43),
    Period(3,  9, 47, 10, 28),
    Period(4, 10, 42, 11, 23),
    Period(5, 11, 27, 12,  8),
    Period(6, 13, 27, 14,  8),
    Period(7, 14, 12, 14, 53)
  )

  private val bp1 = Bp(8, 15, 9, 22)
  private val bp2 = Bp(9, 26, 10, 31)
  private val bp3 = Bp(10, 45, 11, 50)
  private val bp4 = Bp(12, 39, 13, 44)
  private val bp5 = Bp(13, 48, 14, 53)

  private val bp1Alt1 = Bp( 8, 15,  9, 17)
  private val bp2Alt1 = Bp( 9, 21, 10, 21)
  private val bp3Alt1 = Bp(10, 33, 11, 33)
  private val bp4Alt1 = Bp(12, 49, 13, 49)
  private val bp5Alt1 = Bp(13, 53, 14, 53)

  private val tue = Vector(
    Period(1, bp1),
    Period(3, bp2),
    Period(5, bp3),
    Period(6, bp4),
    Period(7, bp5)
  )

  private val tueAlt1 = Vector(
    Period(1, bp1Alt1),
    Period(3, bp2Alt1),
    Period(5, bp3Alt1),
    Period(6, bp4Alt1),
    Period(7, bp5Alt1)
  )

  private val wed = Vector(
    Period(2, 9, 24, 10, 31),
    Period(4, bp3),
    Period(5, bp4),
    Period(6, bp5)
  )

  private val wedAlt1 = Vector(
    Period(2,  9, 24, 10, 25),
    Period(4, 10, 37, 11, 36),
    Period(5, 12, 51, 13, 50),
    Period(6, 13, 54, 14, 53)
  )

  private val thu = Vector(
    Period(1, bp1),
    Period(2, bp2),
    Period(3, bp3),
    Period(4, bp4),
    Period(7, bp5)
  )
  
  private val thuAlt1 = Vector(
    Period(1, bp1Alt1),
    Period(2, bp2Alt1),
    Period(3, bp3Alt1),
    Period(4, bp4Alt1),
    Period(7, bp5Alt1)
  )

  private val weekStandard = Vector(monFri, tue, wed, thu, monFri)
  private val weekAlt1 = Vector(monFriAlt1, tueAlt1, wedAlt1, thuAlt1, monFriAlt1)

  def periodWithin(dateTime: DateTime = DateTime.now): TimeClass = {
    val dayOfWeek = dateTime.getDayOfWeek
    if (dayOfWeek >= 1 && dayOfWeek <= 5) {
      val periods = getWeek(dateTime)(dayOfWeek - 1)
      periods.find(_.within(dateTime)) getOrElse NotInPeriod
    } else NotInPeriod
  }

  def isDuringClassPeriod(dateTime: DateTime = DateTime.now): Boolean = periodWithin(dateTime).isInstanceOf[Period]

  private def getWeek(d: DateTime) =
    if (d.getYear == 2017 && d.getMonthOfYear == 1) weekAlt1 else weekStandard
}
