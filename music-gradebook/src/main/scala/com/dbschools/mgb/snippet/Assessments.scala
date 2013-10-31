package com.dbschools.mgb
package snippet

import scalaz._
import Scalaz._
import org.scala_tools.time.Imports._
import net.liftweb._
import util._
import Helpers._
import model.AssessmentRows
import schema.Subinstrument

class Assessments extends MusicianFromReq {
  def render =
    opMusician.map(m => AssessmentRows.forMusician(m.id)).toList.flatten match {
      case Nil  => ClearNodes
      case rows =>
        ".assessmentRow" #> {
          val dtf = DateTimeFormat.forStyle("S-")
          val tmf = DateTimeFormat.forStyle("-M")

          rows.map(ar =>
            ".date       *" #> <span title={tmf.print(ar.date)}>
              {dtf.print(ar.date)}
            </span> &
              ".tester     *" #> ar.tester &
              ".piece [class]" #> (if (ar.pass) "pass" else "fail") &
              ".piece      *" #> ar.piece &
              ".instrument *" #> (ar.instrument + ~ar.subinstrument.map(Subinstrument.suffix)) &
              ".comments   *" #> ar.notes
          )
        }
    }
}
