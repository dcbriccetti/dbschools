package com.dbschools.perclock.model

import scala.scalajs.js
import js.Date

sealed trait TimeClass
object InSpecialSchedule extends TimeClass
object NotInPeriod extends TimeClass

case class SimpleTime(hour: Int, minute: Int) {
  def toMillis = {
    val c = new Date()
    c.setHours(hour)
    c.setMinutes(minute)
    c.setSeconds(0)
    c.setMilliseconds(0)
    c.getTime()
  }
}

case class Period(num: Int, start: SimpleTime, end: SimpleTime) extends TimeClass {
  def within(t: Double) = start.toMillis <= t && t < end.toMillis
  def timeRemainingMs = end.toMillis - nowMillis
  def totalSecs = (end.toMillis - start.toMillis) / 1000
  def timePassedSecs = (nowMillis - start.toMillis) / 1000
  def startMs = start.toMillis
  def endMs = end.toMillis

  def formattedStart = {
    val sh = fh(start.hour)
    val sm = twoDigits(start.minute)
    s"$sh:$sm"
  }

  def formattedEnd = {
    val eh = fh(end.hour)
    val em = twoDigits(end.minute)
    s"$eh:$em"
  }

  def formattedRange = s"$formattedStart–$formattedEnd"

  def formattedTimeRemaining = {
    var s = timeRemainingMs / 1000
    var m = Math.floor(s / 60)
    s -= m * 60
    var wb = ""
    var wbm = Periods.WarnBellMins
    if (wbm > 0 && m >= wbm) {
        m -= wbm
        wb = wbm + " + "
    }
    val mm = twoDigits(m.toInt)
    val ss = twoDigits(s.toInt)
    s"$wb$mm:$ss"
  }

  private def nowMillis = Periods.nowMs
  private def fh(h: Int) = (if (h > 12) h - 12 else h).toString
  private def twoDigits(i: Int) = f"$i%02d"
}

/** “Block period” */
case class Bp(sh: Int, sm: Int, eh: Int, em: Int)

object Period {
  def apply(num: Int, sh: Int, sm: Int, eh: Int, em: Int): Period = Period(num, SimpleTime(sh, sm), SimpleTime(eh, em))
  def apply(num: Int, bp: Bp): Period =
    Period(num, SimpleTime(bp.sh, bp.sm), SimpleTime(bp.eh, bp.em))
}

object Periods {

  val WarnBellMins = 3

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
  
  val week = Vector(monFri, tue, wed, thu, monFri)

  def periodWithin: TimeClass =
    periodsToday.find(_.within(nowMs)) getOrElse NotInPeriod

  def periodsToday = new Date().getDay match {
    case d if d >= 2 && d <= 6 => week(d - 2)
    case _                     => week.head // Use Monday for out of range
  }

  def nowMs = new Date().getTime() // adjust as needed for testing  - 1000 * 60 * 60 * 12
}
