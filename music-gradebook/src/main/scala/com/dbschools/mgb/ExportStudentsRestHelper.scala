package com.dbschools.mgb

import java.io._
import java.text.NumberFormat

import scalaz._
import Scalaz._
import net.liftweb.http._
import net.liftweb.http.rest.RestHelper
import org.scala_tools.time.Imports.DateTimeFormat
import com.norbitltd.spoiwo.model._
import com.norbitltd.spoiwo.natures.xlsx.Model2XlsxConversions._
import com.norbitltd.spoiwo.model.enums.CellFill
import bootstrap.liftweb.ApplicationPaths.logIn
import model.{LastPass, LastPassFinder, TestingStats, Terms}
import model.Cache.{lastAssTimeByMusician, selectedTestingStatsByMusician}
import snippet.{Authenticator, svGroupAssignments}

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
    val nfmt = NumberFormat.getInstance
    nfmt.setMaximumFractionDigits(2)
    nfmt.setMinimumFractionDigits(2)
    val lastPassesByMusician = new LastPassFinder().lastPassed().groupBy(_.musicianId)
    val headerStyle =
      CellStyle(fillPattern = CellFill.Solid, fillForegroundColor = Color.AquaMarine, font = Font(bold = true))
    val hr = Row(style = headerStyle).withCellValues(
      "Group", "Name", "Gr", "Instrument", "Pass", "Fail", "%", "OP", "OF", "Days", "P/D", "Str", "Last Test", "Last Passed")

    val rows = List(hr) ++
        svGroupAssignments.is.sortBy(ga => (ga.group.name, ga.musician.nameLastFirstNick)).map { row =>
      val opStats = selectedTestingStatsByMusician(row.musician.id)
      def stat(fn: TestingStats => Int) = ~opStats.map(fn)
      val passed  = stat(_.totalPassed)
      val inClassDaysTested = stat(_.inClassDaysTested)
      val passes = lastPassesByMusician.getOrElse(row.musician.id, Seq[LastPass]())

      Row().withCellValues(
        row.group.name,
        row.musician.nameLastFirstNick,
        Terms.graduationYearAsGrade(row.musician.graduation_year.get),
        row.instrument.name.get,
        passed,
        stat(_.totalFailed),
        stat(_.percentPassed),
        stat(_.outsideClassPassed),
        stat(_.outsideClassFailed),
        inClassDaysTested,
        if (inClassDaysTested == 0) "0.0" else nfmt.format(passed.toFloat / inClassDaysTested),
        stat(_.longestPassingStreakTimes.size),
        ~lastAssTimeByMusician.get(row.musician.id).map(fmt.print),
        passes.map(lp => lp.formatted(passes.size > 1 || lp.instrumentId != row.instrument.id)).mkString(", ")
      )
    }

    val sheet = Sheet(rows: _*)
    val workbook = sheet.convertAsXlsx()
    workbook.write(os)
  }
}

