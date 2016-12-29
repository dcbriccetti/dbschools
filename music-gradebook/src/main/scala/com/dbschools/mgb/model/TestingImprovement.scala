package com.dbschools.mgb.model

import scalaz._
import Scalaz._
import org.joda.time.Days
import Cache.MusicianTestInfo

/**
  * Information showing recent testing improvement
  * @param slope the slope of the line of passes and test dates
  * @param recentDailyPassCounts the number of passes for recent test dates
  */
case class TestingImprovement(slope: Double, recentDailyPassCounts: Seq[Int])

/** Generates TrendInfo */
object OptionTestingImprovement {
  def apply(musicianTestInfos: Iterable[MusicianTestInfo], numDays: Int = 5): Option[TestingImprovement] = {
    musicianTestInfos.toSeq.groupBy(_.time.withTimeAtStartOfDay) match {

      case passesByDay if passesByDay.size >= numDays =>
        val datesOfRecentTests = passesByDay.keys.toSeq.sortBy(_.getMillis).takeRight(numDays)
        val testsByDay: Seq[Seq[MusicianTestInfo]] = datesOfRecentTests map passesByDay
        val timeOfFirstTestOfFirstDay = testsByDay.head.head.time

        case class DaysDeltaAndPasses(daysDelta: Int, passes: Int)
        val daysDeltaAndPasses = for {
          testsOfOneDay        <- testsByDay
          timeOfFirstTestOfDay  = testsOfOneDay.head.time
          passes                = testsOfOneDay.count(_.pass == true)
          daysDelta             = Days.daysBetween(timeOfFirstTestOfFirstDay, timeOfFirstTestOfDay).getDays
        } yield DaysDeltaAndPasses(daysDelta, passes)

        val trendLinePoints = daysDeltaAndPasses.map(d => Stats.Point(d.daysDelta, d.passes))
        val improvementSlope = Stats.regressionSlope(trendLinePoints)
        Some(TestingImprovement(improvementSlope, daysDeltaAndPasses.map(_.passes)))

      case _ => // Not enough days to calculate
        none[TestingImprovement]
    }
  }
}
