package com.dbschools.mgb
package model

import scalaz._
import Scalaz._
import org.apache.log4j.Logger
import snippet.svStatsDisplay
import model.Cache.mesters

object PassesPerWeekChart {
  private val log = Logger.getLogger(getClass)
  case class PF(passes: Int, failures: Int)
  case class PassFailsForWeek(weekNum: Int, pf: PF)

  def generateDataAsJson(opMusicianId: Option[Int]): String = {
    val entireSchoolYear = svStatsDisplay.is == StatsDisplay.Year

    val testRows = AssessmentRows(opMusicianId, limit = Int.MaxValue).filter { test =>
      entireSchoolYear || mesters.containing(test.date) == mesters.current }

    val testingWeekNums = entireSchoolYear ? Cache.activeTestingWeeks.forSchoolYear(SchoolYears.current) |
      Cache.activeTestingWeeks.forSchoolTerm(SchoolYears.current, mesters.yearStart, mesters.current)

    val testRowsByMusician = testRows.groupBy(_.musician.id)

    val weekCountsByMusician: Map[Int, Seq[PassFailsForWeek]] =
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
    PassFailsForWeek(weekNum, PF(assessmentRowsForWeek.count(_.pass), assessmentRowsForWeek.count(!_.pass)))
  }

  private def weekNumForTest(test: AssessmentRow) = ActiveTestingWeeks.weekNum(mesters.yearStart, test.date)

  private def studentsDataFromWeekCounts(weekCountsByMusician: Map[Int, Seq[PassFailsForWeek]]) =
    weekCountsByMusician.map {
      case (mId, passFailsForWeeks) =>
        val barGroups = Seq(
          barGroup(passFailsForWeeks, pass = true),
          barGroup(passFailsForWeeks, pass = false)
        ).mkString(",\n")

        s"""
           |musician$mId: [
           |$barGroups
           |]
         """.stripMargin
    }.mkString(",")

  private def barGroup(passFailsForWeeks: Seq[PassFailsForWeek], pass: Boolean) = s"""
     |{
     |    key: "${if (pass) "Passes" else "Failures"}",
     |    values: [${passFailsForWeeks.map(makeLabelAndValue(pass = pass)).mkString(",\n")}]
     |}""".stripMargin

  private def makeLabelAndValue(pass: Boolean)(passFailsForWeek: PassFailsForWeek) = s"""
     |{
     |    "label": ${passFailsForWeek.weekNum},
     |    "value": ${pass ? passFailsForWeek.pf.passes | passFailsForWeek.pf.failures}
     |}""".stripMargin
}
