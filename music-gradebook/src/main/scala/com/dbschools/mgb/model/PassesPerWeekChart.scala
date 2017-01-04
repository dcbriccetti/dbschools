package com.dbschools.mgb
package model

import scalaz._
import Scalaz._

import snippet.svStatsDisplay
import model.Cache.{containingTerm, currentMester}

import org.joda.time.Weeks

object PassesPerWeekChart {
  case class PF(passes: Int, failures: Int)
  case class Week(weekNum: Int, pf: PF)

  def json(musicianId: Option[Int]): String = {
    val allYear = svStatsDisplay.is == StatsDisplay.Year
    val rowsForTimePeriodMostRecentFirst = AssessmentRows(musicianId, limit = Int.MaxValue).filter { ar =>
      allYear || containingTerm(ar.date) == currentMester
    }
    val startOfPeriod = allYear ? Cache.yearStart | currentMester
    val testingWeeks =
      rowsForTimePeriodMostRecentFirst.map(ar => Weeks.weeksBetween(startOfPeriod, ar.date).getWeeks + 1).toSeq.distinct.reverse
    val byMusician = rowsForTimePeriodMostRecentFirst.groupBy(_.musician.id)
    val testCountsByWeekNumByStudent = byMusician.mapValues { tests =>
      val testsByWeek = tests.groupBy(test =>
        Weeks.weeksBetween(startOfPeriod, test.date).getWeeks + 1).toSeq.sortBy(_._1).toMap
      testingWeeks.map { weekNum =>
        Week(weekNum, testsByWeek.get(weekNum).map { tests =>
          PF(tests.seq.count(_.pass), tests.seq.count(!_.pass))
        }.getOrElse(PF(0, 0)))
      }
    }

    def makeLabelAndValue(pass: Boolean)(week: Week) =
      s"""
         |{
         |    "label": ${week.weekNum},
         |    "value": ${pass ? week.pf.passes | week.pf.failures}
         |}
       """.stripMargin

    val allStudentsData = testCountsByWeekNumByStudent.map {
      case (musicianId, testsByWeekNum) =>
        s"""
           |musician$musicianId: [
           |{
           |    key: "Passes",
           |    values: [${testsByWeekNum.map(makeLabelAndValue(pass = true)).mkString(",\n")}]
           |},
           |{
           |    key: "Failures",
           |    values: [${testsByWeekNum.map(makeLabelAndValue(pass = false)).mkString(",\n")}]
           |}
           |]
         """.stripMargin
    }.mkString(",")

    s"""
       |chartData = {
       |    $allStudentsData
       |};
     """.stripMargin
  }
}
