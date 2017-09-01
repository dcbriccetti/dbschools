package com.dbschools.mgb
package snippet

import scala.xml.{NodeSeq, Text}
import collection.mutable.{Set => MSet}
import scalaz._
import Scalaz._
import net.liftweb._
import util._
import Helpers._
import net.liftweb.http.{RequestVar, SHtml}
import net.liftweb.http.js.JsCmds._
import model.{TagCounts, SelectedMusician, ActiveTestingWeeks, Cache, AssessmentRow, AssessmentRows, UserLoggable}
import schema.{Musician, Subinstrument}
import LiftExtensions._

object rvSelectedAsmts extends RequestVar[MSet[Int]](MSet[Int]())

class Assessments extends SelectedMusician with TagCounts with UserLoggable {
  def render: (NodeSeq) => NodeSeq = renderAllOrOne(opMusician)

  def renderWithStudents: (NodeSeq) => NodeSeq = renderAllOrOne(none[Musician])

  private def renderAllOrOne(opMusician: Option[Musician]) =
    Assessments.filterStudentColumn(opMusician.isEmpty) andThen
      Assessments.rowCssSel(AssessmentRows(opMusician.map(_.id), limit = 500).toList)

  def assessmentsSummary: NodeSeq = {
    Text(~rvMusicianDetails.is.map(md => {
      val (pass, fail) = md.assessments.partition(_.pass)
      val tagCountsStr = tagCounts(md.musician.id) match {
        case Nil => ""
        case n => n.map(tc => s"${tc.tag}: ${tc.count}").mkString(", comments: ", ", ", "")
      }
      s"Passes: ${pass.size}, failures: ${fail.size}$tagCountsStr"
    }))
  }

  def delete: CssBindFunc = "#deleteAss" #> SHtml.ajaxButton("Delete", () => {
    val selAsses = rvSelectedAsmts.is
    if (selAsses.nonEmpty) {
      Confirm(
        s"Are you sure you want to remove the ${selAsses.size} selected assessments? This can not be undone.",
        SHtml.ajaxInvoke(() => {
          model.Assessments.delete(selAsses)
          info("Deleted assessment(s): " + selAsses)
          rvSelectedAsmts(rvSelectedAsmts.is.empty)
          Reload
        }))
    } else Noop
  })
}

object Assessments {
  def removeNodes(selectors: String*): CssBindFunc = selectors.map(_ #> none[String]).reduce(_ & _)

  def filterStudentColumn(keepStudent: Boolean): (NodeSeq) => NodeSeq =
    if (keepStudent) PassThru else removeNodes(".student", "#studentHeading")

  def rowCssSel(rows: Iterable[AssessmentRow]): CssSel = {
    ".assessmentRow" #> {

      def selectionCheckbox(row: AssessmentRow) =
        SHtml.ajaxCheckbox(false, checked => {
          val selectedAsses = rvSelectedAsmts.is
          if (checked) selectedAsses += row.assId
          else selectedAsses -= row.assId
          if (selectedAsses.isEmpty) JsHideId("deleteAss") else JsShowId("deleteAss")
        })

      def formatWeekNum(ar: AssessmentRow): String = {
        val wn = ActiveTestingWeeks.weekNum(Cache.terms.yearStart, ar.date)
        (wn > 0) ? wn.toString | ""
      }

      rows.map(ar =>
        ".sel         *"  #> selectionCheckbox(ar) &
        ".weekNum     *"  #> formatWeekNum(ar) &
        ".date        *"  #> AbbrevDate(ar.date) &
        ".extra       *"  #> (if (ar.outsideClass) "✔︎" else "") &
        ".tester      *"  #> ar.tester &
        ".student     *"  #> ar.musician.nameNickLast &
        ".piece [class]"  #> (if (ar.pass) "pass" else "fail") &
        ".piece       *"  #> ar.piece &
        ".instrument  *"  #> (ar.instrument + ~ar.subinstrument.map(Subinstrument.suffix)) &
        ".comments    *"  #> ar.notes
      )
    }
  }

  def createRow(assessmentRow: AssessmentRow, keepStudent: Boolean = true): NodeSeq = {
    val sel = filterStudentColumn(keepStudent) andThen rowCssSel(Seq(assessmentRow))
    sel(elemFromTemplate("_assessments", ".assessmentRow"))
  }
}
