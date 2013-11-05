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

class Assessments extends MusicianFromReq {
  def render = {
    def removeNodes(selectors: String*) = selectors.map(_ #> none[String]).reduce(_ & _)
    
    (if (opMusician.isEmpty) PassThru else removeNodes(".musician", "#studentHeading")) andThen (
      AssessmentRows(opMusician.map(_.id)).toList match {
      case Nil  => ClearNodes
      case rows => Assessments.rowCssSel(rows)
    }
  )}
}

object Assessments {
  def rowCssSel(rows: Iterable[AssessmentRow]): CssSel = {
    ".assessmentRow" #> {
      val dtf = DateTimeFormat.forStyle("S-")
      val tmf = DateTimeFormat.forStyle("-M")

      rows.map(ar =>
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
}
