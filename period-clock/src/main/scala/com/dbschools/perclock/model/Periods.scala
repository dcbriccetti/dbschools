package com.dbschools.perclock.model

import scala.scalajs.js
import js.Date
import Periods.WarnBellMins

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
  def formattedStart = s"${fh(start.hour)}:${fms(start.minute)}"
  def formattedEnd   = s"${fh(end.hour)}:${fms(end.minute)}"
  def formattedRange = s"$formattedStart–$formattedEnd"

  def formattedTimeRemaining = {
    var secondsRemaining = timeRemainingMs / 1000
    var minutesRemaining = Math.floor(secondsRemaining / 60)
    secondsRemaining -= minutesRemaining * 60
    val warningBellPrefix =
      if (WarnBellMins > 0 && minutesRemaining >= WarnBellMins) {
        minutesRemaining -= WarnBellMins
        s"$WarnBellMins + "
      } else ""
    s"$warningBellPrefix${fms(minutesRemaining.toInt)}:${fms(secondsRemaining.toInt)}"
  }

  private def nowMillis = Periods.nowMs
  /** Format hour */
  private def fh(h: Int) = (if (h > 12) h - 12 else h).toString
  /** Format minutes or seconds */
  private def fms(i: Int) = f"$i%02d"
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

  // Times used more than once
  private val bpA = Bp(8, 15, 9, 22)
  private val bpB = Bp(9, 26, 10, 31)
  private val bpC = Bp(10, 45, 11, 50)
  private val bpD = Bp(12, 39, 13, 44)
  private val bpE = Bp(13, 48, 14, 53)

  private val tue = Vector(
    Period(1, bpA),
    Period(3, bpB),
    Period(5, bpC),
    Period(6, bpD),
    Period(7, bpE)
  )

  private val wed = Vector(
    Period(2, 9, 24, 10, 31),
    Period(4, bpC),
    Period(5, bpD),
    Period(6, bpE)
  )

  private val thu = Vector(
    Period(1, bpA),
    Period(2, bpB),
    Period(3, bpC),
    Period(4, bpD),
    Period(7, bpE)
  )
  
  val week = Vector(monFri, tue, wed, thu, monFri)

  def periodWithin: TimeClass = periodsToday.find(_.within(nowMs)) getOrElse NotInPeriod

  def periodsToday = new Date().getDay match {
    case d if d >= 1 && d <= 5 => week(d - 1)
    case _                     => week.head // Use Monday for out of range
  }

  def nowMs = new Date().getTime() // adjust as needed for testing  - 1000 * 60 * 60 * 12
}
