package com.dbschools.mgb

import java.io._

import scalaz._
import Scalaz._
import net.liftweb.http._
import net.liftweb.http.rest.RestHelper
import org.scala_tools.time.Imports.DateTimeFormat
import com.norbitltd.spoiwo.model._
import com.norbitltd.spoiwo.natures.xlsx.Model2XlsxConversions._
import com.norbitltd.spoiwo.model.enums.CellFill
import bootstrap.liftweb.ApplicationPaths.logIn
import schema.Subinstrument
import model._
import model.Cache.{lastInClassTestTime, selectedTestingStatsByMusician, terms}
import snippet.{Authenticator, svGroupAssignments, svStatsDisplay}

/** Processes requests to download a spreadsheet of students */
object ExportStudentsRestHelper extends RestHelper {
  serve {
    case Req("export" :: _, _, GetRequest) =>
      if (! Authenticator.loggedIn)
        S.redirectTo(logIn.href)
      else {
        val filename = File.createTempFile("students", "xlsx").getPath
        val outStream = new FileOutputStream(filename)
        Exporter.exportStudents(outStream)
        outStream.close()
        val file = new File(filename)
        val in = new FileInputStream(filename)

        StreamingResponse(in, () => {
          in.close()
          file.delete()
        }, file.length, List("Content-Type" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
          "Content-Disposition" -> "attachment; filename=students.xlsx"), Nil, 200)
      }
   }
}

object Exporter {
  /** Writes a spreadsheet file of exported students to the specified output stream */
  def exportStudents(os: OutputStream): Unit = {
    val fmt = DateTimeFormat.forStyle("S-")
    val lastPassesByMusician = new LastPassFinder().lastPassed().groupBy(_.musicianId)
    val headerStyle =
      CellStyle(fillPattern = CellFill.Solid, fillForegroundColor = Color.LightBlue, font = Font(bold = true))
    val hr = Row(style = headerStyle).withCellValues(
      "Group", "Name", "Gr", "Instrument", "Pass", "Fail", "OP", "OF", "Days", "P/D", "Score", "PN", "Str", "IC Test", "Last Passed")

    val statsRows = List(hr) ++
        svGroupAssignments.is.sortBy(ga => (ga.group.name, ga.musician.nameLastFirstNick)).map { row =>
      val opStats = selectedTestingStatsByMusician(row.musician.id)
      def stat(fn: TestingStats => Int) = ~opStats.map(fn)
      def statd(fn: TestingStats => Double) = ~opStats.map(fn)
      val passed  = stat(_.totalPassed)
      val inClassDaysTested = stat(_.inClassDaysTested)
      val passes = lastPassesByMusician.getOrElse(row.musician.id, Seq[LastPass]())

      Row().withCellValues(
        row.group.name,
        row.musician.nameLastFirstNick,
        SchoolYears.graduationYearAsGrade(row.musician.graduation_year.get),
        row.instrument.name.get,
        passed,
        stat(_.totalFailed),
        stat(_.outsideClassPassed),
        stat(_.outsideClassFailed),
        inClassDaysTested,
        if (inClassDaysTested == 0) 0d else passed.toDouble / inClassDaysTested,
        statd(_.testScorePercent),
        stat(_.passesNeeded),
        stat(_.longestPassingStreakTimes.size),
        lastInClassTestTime(row.musician.id).map(fmt.print),
        passes.map(lp => lp.formatted(passes.size > 1 || lp.instrumentId != row.instrument.id)).mkString(", ")
      )
    }
    
    val dhr = Row(style = headerStyle).withCellValues(
      "Name", "Time", "Tester", "Extra", "Pass", "Piece", "Instrument", "Notes")
    val musicianIds = svGroupAssignments.is.map(_.musician.id)
    val allYear = svStatsDisplay.is == StatsDisplay.Year
    val filteredRows = AssessmentRows(None, limit = Int.MaxValue).filter { ar =>
      (musicianIds contains ar.musician.id) && (allYear || terms.containing(ar.date) == terms.current) }
    // todo Select only the needed rows from the database, rather than filtering after selecting
    val sortedAssessments = filteredRows.toList.sortBy(ar =>
      (ar.musician.nameLastFirstNick, -ar.date.getMillis))
    val ddf = DateTimeFormat.forStyle("MM")

    val detailRows = List(dhr) ++ sortedAssessments.map { ar =>
      Row().withCellValues(
        ar.musician.nameLastFirstNick,
        ddf.print(ar.date),
        ar.tester,
        if (ar.outsideClass) "✔︎" else "",
        if (ar.pass) "✔︎" else "",
        ar.piece,
        ar.instrument + ~ar.subinstrument.map(Subinstrument.suffix),
        ~ar.notes
    )}

    val stats = Sheet(name="Stats", rows = statsRows)
    val details = Sheet(name="Details", rows = detailRows)
    val workbook = Workbook(stats, details).convertAsXlsx()
    workbook.write(os)
  }
}
