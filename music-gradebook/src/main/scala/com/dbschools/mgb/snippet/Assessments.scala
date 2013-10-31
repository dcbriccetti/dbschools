package com.dbschools.mgb
package snippet

import scalaz._
import Scalaz._
import org.scala_tools.time.Imports._
import net.liftweb._
import util._
import Helpers._
import model.{AssessmentRow, AssessmentRows}
import schema.Subinstrument

class Assessments extends MusicianFromReq {
  def render = ".assessmentRow" #> {
    val dtf = DateTimeFormat.forStyle("S-")
    val tmf = DateTimeFormat.forStyle("-M")

    val rows = opMusician.map(_.id).map(AssessmentRows.forMusician) | Seq[AssessmentRow]()
    rows.map(ar =>
      ".date       *"   #> <span title={tmf.print(ar.date)}>{dtf.print(ar.date)}</span> &
      ".tester     *"   #> ar.tester &
      ".piece [class]"  #> (if (ar.pass) "pass" else "fail") &
      ".piece      *"   #> ar.piece &
      ".instrument *"   #> (ar.instrument + ~ar.subinstrument.map(Subinstrument.suffix)) &
      ".comments   *"   #> ar.notes
    )
  }
}
