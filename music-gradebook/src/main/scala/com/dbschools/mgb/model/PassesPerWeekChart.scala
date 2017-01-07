package com.dbschools.mgb
package model

import org.joda.time.Weeks
import scalaz._
import Scalaz._
import org.apache.log4j.Logger
import snippet.svStatsDisplay
import model.Cache.mesters

object PassesPerWeekChart {
  private val log = Logger.getLogger(getClass)
  case class PF(passes: Int, failures: Int)
  case class WeekCounts(weekNum: Int, pf: PF)

  def generateDataAsJson(opMusicianId: Option[Int]): String = {
    val entireSchoolYear = svStatsDisplay.is == StatsDisplay.Year

    val testRows = AssessmentRows(opMusicianId, limit = Int.MaxValue).filter { test =>
      entireSchoolYear || mesters.containing(test.date) == mesters.current }

    val testingWeekNums = entireSchoolYear ? Cache.activeTestingWeeks.forSchoolYear(SchoolYears.current) |
      Cache.activeTestingWeeks.forSchoolTerm(SchoolYears.current, mesters.yearStart, mesters.current)

    val testRowsByMusician = testRows.groupBy(_.musician.id)

    val weekCountsByMusician: Map[Int, Seq[WeekCounts]] =
      testRowsByMusician mapValues weekCountsFromTestRows(testingWeekNums)

    val allStudentsData = studentsDataFromWeekCounts(weekCountsByMusician)

    s"""
       |chartData = {
       |    $allStudentsData
       |};
     """.stripMargin
  }

  private def weekCountsFromTestRows(testingWeekNums: Seq[Int])(oneMusicianTests: Iterable[AssessmentRow]) = {
    val oneMusicianTestsByWeekNum = oneMusicianTests groupBy weekNumForTest
    testingWeekNums map weekCountsForWeekNum(oneMusicianTestsByWeekNum)
  }

  private def weekCountsForWeekNum(testsByWeek: Map[Int, Iterable[AssessmentRow]])(weekNum: Int) = {
    val assessmentRowsForWeek: Seq[AssessmentRow] = testsByWeek.get(weekNum).toSeq.flatten
    WeekCounts(weekNum, PF(assessmentRowsForWeek.count(_.pass), assessmentRowsForWeek.count(!_.pass)))
  }

  private def weekNumForTest(test: AssessmentRow) = {
    val wn = ActiveTestingWeeks.weekNum(mesters.yearStart, test.date)
    log.info(s"weekNumForTest ${mesters.yearStart}, ${test.date}, $wn")
    wn
  }

  private def studentsDataFromWeekCounts(weekCountsByMusician: Map[Int, Seq[WeekCounts]]) = {
    weekCountsByMusician.map {
      case (mId, testsByWeekNum) =>
        s"""
           |musician$mId: [
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
  }

  private def makeLabelAndValue(pass: Boolean)(week: WeekCounts) =
    s"""
       |{
       |    "label": ${week.weekNum},
       |    "value": ${pass ? week.pf.passes | week.pf.failures}
       |}
     """.stripMargin
}
