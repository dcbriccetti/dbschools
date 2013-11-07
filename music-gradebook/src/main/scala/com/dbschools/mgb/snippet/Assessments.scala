package com.dbschools.mgb
package snippet

import scala.xml.NodeSeq
import collection.mutable.{Set => MSet}
import org.apache.log4j.Logger
import org.joda.time.DateTimeComparator
import scalaz._
import Scalaz._
import org.scala_tools.time.Imports._
import net.liftweb._
import util._
import Helpers._
import net.liftweb.http.{Templates, RequestVar, SHtml}
import net.liftweb.http.js.JsCmds._
import com.dbschools.mgb.model.{AssessmentRow, AssessmentRows}
import schema.Subinstrument
import model.BoxOpener._

object rvSelectedAsses extends RequestVar[MSet[Int]](MSet[Int]())

class Assessments extends MusicianFromReq {
  private val log = Logger.getLogger(getClass)

  def render = {
    Assessments.filterNodes(opMusician.isEmpty) andThen (
      AssessmentRows(opMusician.map(_.id)).toList match {
      case Nil  => ClearNodes
      case rows => Assessments.rowCssSel(rows)
    }
  )}

  def delete = "#deleteAss" #> SHtml.ajaxButton("Delete", () => {
    val selAsses = rvSelectedAsses.is.toIterable
    if (selAsses.nonEmpty) {
      Confirm(
        s"Are you sure you want to remove the ${selAsses.size} selected assessments? This can not be undone.",
        SHtml.ajaxInvoke(() => {
          model.Assessments.delete(selAsses)
          log.info("Deleted assessment(s): " + selAsses)
          rvSelectedAsses(rvSelectedAsses.is.empty)
          Reload
        }))
    } else Noop
  })
}

object Assessments {
  def removeNodes(selectors: String*) = selectors.map(_ #> none[String]).reduce(_ & _)

  def filterNodes(keep: Boolean): (NodeSeq) => NodeSeq = {
    if (keep) PassThru else Assessments.removeNodes(".musician", "#studentHeading")
  }

  def rowCssSel(rows: Iterable[AssessmentRow]): CssSel = {
    ".assessmentRow" #> {
      val dtf = DateTimeFormat.forStyle("SS")
      val tmf = DateTimeFormat.forStyle("-S")

      def selectionCheckbox(row: AssessmentRow) =
        SHtml.ajaxCheckbox(false, checked => {
          val selectedAsses = rvSelectedAsses.is
          if (checked) selectedAsses += row.assId
          else selectedAsses -= row.assId
          if (selectedAsses.isEmpty) JsHideId("deleteAss") else JsShowId("deleteAss")
        })

      val c = DateTimeComparator.getDateOnlyInstance
      rows.map(ar =>
        ".sel        *" #> selectionCheckbox(ar) &
        ".date       *" #> (if (c.compare(null, ar.date) == 0) tmf else dtf).print(ar.date) &
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

  private val rowNodeSeq = {
    val cssSel = ".assessmentRow ^^" #> ""
    val table = Templates(List("_assessments")).open
    cssSel(table)
  }
}
