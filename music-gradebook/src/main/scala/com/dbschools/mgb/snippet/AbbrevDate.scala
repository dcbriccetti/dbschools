package com.dbschools.mgb.snippet

import org.joda.time.{Years, DateTimeComparator}
import org.scala_tools.time.Imports._

object AbbrevDate {

  private val c = DateTimeComparator.getDateOnlyInstance
  private val mdf   = DateTimeFormat.forPattern("MMM d")
  private val df    = DateTimeFormat.forStyle("S-")
  private val hmsf  = DateTimeFormat.forPattern("hh:mm:ss")
  private val ampmf = DateTimeFormat.forPattern("aa")

  def apply(dateTime: DateTime, dateOnly: Boolean = false): String = {
    val date = {
      val yearsAgo = Years.yearsBetween(dateTime, DateTime.now).getYears
      val dateIsToday = c.compare(null, dateTime) == 0
      if (dateIsToday && ! dateOnly) None else
        Some((if (yearsAgo > 0) df else mdf).print(dateTime))
    }
    val time = {
      if (dateOnly) None else {
        val uncommonHour = dateTime.getHourOfDay < 8 || dateTime.getHourOfDay > 17
        val ampm = if (uncommonHour) ampmf.print(dateTime) else ""
        Some(hmsf.print(dateTime) + " " + ampm)
      }
    }
    Seq(date, time).flatten.mkString(" ")
  }
}
