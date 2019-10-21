package com.dbschools.perclock.model

import scala.scalajs.js
import js.Date

sealed trait TimeClass
object InSpecialSchedule extends TimeClass
object NotInPeriod extends TimeClass

case class SimpleTime(hour: Int, minute: Int) {
  def toMillis: Double = {
    val c = new Date()
    c.setHours(hour)
    c.setMinutes(minute)
    c.setSeconds(0)
    c.setMilliseconds(0)
    c.getTime()
  }
}

case class Period(number: Int, start: SimpleTime, end: SimpleTime) extends TimeClass {
  val WarnBellMins = 3

  def within(t: Double): Boolean = start.toMillis <= t && t < end.toMillis
  def timeRemainingMs: Double = end.toMillis - nowMillis
  def totalSecs: Double = (end.toMillis - start.toMillis) / 1000
  def timePassedSecs: Double = (nowMillis - start.toMillis) / 1000
  def startMs: Double = start.toMillis
  def endMs: Double = end.toMillis
  def formattedStart = s"${fh(start.hour)}:${fms(start.minute)}"
  def formattedEnd   = s"${fh(end.hour)}:${fms(end.minute)}"
  def formattedRange = s"$formattedStart–$formattedEnd"

  def formattedTimeRemaining: String = {
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

  private def nowMillis: Double = DefaultPeriods.nowMs
  /** Format hour */
  private def fh(h: Int): String = (if (h > 12) h - 12 else h).toString
  /** Format minutes or seconds */
  private def fms(i: Int) = f"$i%02d"
}

/** “Block period” */
case class Bp(startHour: Int, startMinute: Int, endHour: Int, endMinute: Int)

object Period {
  def apply(number: Int, startHour: String, startMinute: String, endHour: String, endMinute: String): Period =
    Period(number, SimpleTime(startHour.toInt, startMinute.toInt), SimpleTime(endHour.toInt, endMinute.toInt))
  def apply(number: Int, startHour: Int, startMinute: Int, endHour: Int, endMinute: Int): Period =
    Period(number, SimpleTime(startHour, startMinute), SimpleTime(endHour, endMinute))
  def apply(number: Int, bp: Bp): Period =
    Period(number, SimpleTime(bp.startHour, bp.startMinute), SimpleTime(bp.endHour, bp.endMinute))
}

trait Periods {
  val week: Vector[Vector[Period]]

  def periodWithin: TimeClass = periodsToday.find(_.within(nowMs)) getOrElse NotInPeriod

  def periodsToday: Vector[Period] = dowToday match {
    case d if d < 5 => week(d)
    case _          => week.head // Use Monday for out of range
  }

  /** Returns the day of the week, where 0 = Monday */
  def dowToday: Int = {
    val sundayBasedDow = new Date().getDay()
    (sundayBasedDow + 6) % 7
  }

  def nowMs: Double = new Date().getTime() // adjust as needed for testing  - 1000 * 60 * 60 * 12
}

object DefaultPeriods extends Periods {

  val week: Vector[Vector[Period]] = {
    // Times used more than once
    val bpA = Bp( 8, 15,  9, 22)
    val bpB = Bp( 9, 26, 10, 31)
    val bpC = Bp(10, 45, 11, 50)
    val bpD = Bp(12, 39, 13, 44)
    val bpE = Bp(13, 48, 14, 53)

    val mon = Vector(
      Period(1,  8, 15,  9,  2),
      Period(2,  9,  6,  9, 51),
      Period(3,  9, 55, 10, 40),
      Period(4, 10, 56, 11, 41),
      Period(5, 11, 45, 12, 30),
      Period(6, 13, 19, 14,  4),
      Period(7, 14,  8, 14, 53)
    )

    val tue = Vector(
      Period(1, bpA),
      Period(3, bpB),
      Period(5, bpC),
      Period(6, bpD),
      Period(7, bpE)
    )

    val wed = Vector(
      Period(2, 9, 24, 10, 31),
      Period(4, bpC),
      Period(5, bpD),
      Period(6, bpE)
    )

    val thu = Vector(
      Period(1, bpA),
      Period(2, bpB),
      Period(3, bpC),
      Period(4, bpD),
      Period(7, bpE)
    )

    val fri = mon;

    Vector(mon, tue, wed, thu, fri)
  }

  def dump() {
    println("Stanley Middle School")
    week.foreach {day =>
      day.foreach {p =>
        print(s"${p.number} ${p.start.hour}:${p.start.minute}-${p.end.hour}:${p.end.minute}\t")
      }
      println()
    }
  }
}
