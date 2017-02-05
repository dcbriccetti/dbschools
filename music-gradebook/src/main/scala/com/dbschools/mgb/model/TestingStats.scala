package com.dbschools.mgb.model

import com.dbschools.mgb.model.Cache.MusicianTestInfo
import org.joda.time.DateTime

/** Statistics for a musician, calculated from all tests in a term or school year */
case class TestingStats(
  totalPassed:                Int,
  totalFailed:                Int,
  totalDaysTested:            Int,
  inClassDaysTested:          Int,
  outsideClassDaysTested:     Int,
  outsideClassPassed:         Int,
  outsideClassFailed:         Int,
  longestPassingStreakTimes:  Seq[DateTime],
  opTestingImprovement:       Option[TestingImprovement]
) {
  val NeededPassesPerInClassTestDay = 3

  def passesNeeded: Int = math.max(0, inClassDaysTested * NeededPassesPerInClassTestDay - totalPassed)
  def passesPerInClassTestDays: Double = if (inClassDaysTested != 0) totalPassed.toDouble / inClassDaysTested else 0
  def numTests: Int = totalPassed + totalFailed
  def percentPassed: Int = if (numTests == 0) 0 else math.round(totalPassed * 100.0 / numTests).toInt
  def testScorePercent: Double = math.min(100.0, passesPerInClassTestDays / NeededPassesPerInClassTestDay * 100)
}

object TestingStats {
  def apply(mtis: Iterable[MusicianTestInfo]): TestingStats = {
    val passFails = mtis.map(_.pass)
    val totalPassed = passFails.count(_ == true)
    val totalFailed = passFails.size - totalPassed

    def uniqueDays(mtps: Iterable[MusicianTestInfo]): Int =
      mtps.map(a => new DateTime(a.time).withTimeAtStartOfDay.getMillis).toSet.size

    def daysTested(duringClass: Boolean): Int = uniqueDays(mtis.filter(_.duringClass == duringClass))

    val outsideClassTests = mtis.filterNot(_.duringClass)

    TestingStats(totalPassed, totalFailed,
      uniqueDays(mtis), daysTested(duringClass = true), daysTested(duringClass = false),
      outsideClassTests.count(_.pass),
      outsideClassTests.count(!_.pass),
      longestStreak(mtis), OptionTestingImprovement(mtis))
  }

  private def longestStreak(mtps: Iterable[MusicianTestInfo]) = {
    type Times = Seq[DateTime]
    case class StreakInfo(longest: Times, current: Times)
    mtps.foldLeft(StreakInfo(Seq(), Seq()))((si, mtp) => {
      val newCurrent = if (mtp.pass) si.current :+ mtp.time else Seq()
      StreakInfo(if (newCurrent.size > si.longest.size) newCurrent else si.longest, newCurrent)
    }).longest
  }
}
