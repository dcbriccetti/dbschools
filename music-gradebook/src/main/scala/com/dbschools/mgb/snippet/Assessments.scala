package com.dbschools.mgb
package snippet

import scalaz._
import Scalaz._
import org.scala_tools.time.Imports._
import net.liftweb._
import util._
import Helpers._
import com.dbschools.mgb.model.{AssessmentRow, AssessmentRows}
import schema.Subinstrument
import scala.xml.NodeSeq
import net.liftweb.http.SHtml

class Assessments extends MusicianFromReq {
  def render = {
    Assessments.filterNodes(opMusician.isEmpty) andThen (
      AssessmentRows(opMusician.map(_.id)).toList match {
      case Nil  => ClearNodes
      case rows => Assessments.rowCssSel(rows)
    }
  )}
  def delete = "#deleteAss" #> SHtml.button("Delete", () => {})
}

object Assessments {
  def removeNodes(selectors: String*) = selectors.map(_ #> none[String]).reduce(_ & _)

  def filterNodes(keep: Boolean): (NodeSeq) => NodeSeq = {
    if (keep) PassThru else Assessments.removeNodes(".musician", "#studentHeading")
  }

  def rowCssSel(rows: Iterable[AssessmentRow]): CssSel = {
    ".assessmentRow" #> {
      val dtf = DateTimeFormat.forStyle("S-")
      val tmf = DateTimeFormat.forStyle("-M")

      rows.map(ar =>
        ".sel        *" #> SHtml.ajaxCheckbox(false, (c) => {}) &
        ".date       *" #> <span title={tmf.print(ar.date)}>{dtf.print(ar.date)}</span> &
        ".tester     *" #> ar.tester &
        ".musician   *" #> ar.musician.name &
        ".piece [class]" #> (if (ar.pass) "pass" else "fail") &
        ".piece      *" #> ar.piece &
        ".instrument *" #> (ar.instrument + ~ar.subinstrument.map(Subinstrument.suffix)) &
        ".comments   *" #> ar.notes
      )
    }
  }

  def createRow(assessmentRow: AssessmentRow, keepStudent: Boolean = true) = {
    val sel = filterNodes(keepStudent) andThen rowCssSel(Seq(assessmentRow))
    sel(Assessments.rowNodeSeq)
  }

  private val rowNodeSeq = // TODO Why does Templates give <html><body></body></html>? Templates(List("_assessmentRow")).open
    <tr class="assessmentRow">
      <td class="sel"></td>
      <td class="date"></td>
      <td class="tester"></td>
      <td class="musician"></td>
      <td class="piece"></td>
      <td class="instrument"></td>
      <td class="comments"></td>
    </tr>
}
