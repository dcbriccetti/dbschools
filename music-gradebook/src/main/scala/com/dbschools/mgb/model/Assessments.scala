package com.dbschools.mgb
package model

import scalaz._
import Scalaz._
import org.scala_tools.time.Imports._
import org.squeryl.PrimitiveTypeMode._
import schema.AssessmentTag

case class AssessmentRow(date: DateTime, tester: String, piece: String,
  instrument: String, subinstrument: Option[String], pass: Boolean, notes: Option[String])

object AssessmentRows {
  import schema.AppSchema.{assessments, pieces, instruments, users}

  case class RowAndId(id: Int, row: AssessmentRow)
  
  def opStr(s: String) = if (s.trim.isEmpty) None else Some(s)

  def forMusician(id: Int): Iterable[AssessmentRow] = {
    val rows = from(assessments, pieces, instruments, users)((a, p, i, u) =>
      where(a.musician_id === id and a.pieceId === p.id and a.instrument_id === i.id
        and a.user_id === u.id)
      select(RowAndId(a.assessment_id, AssessmentRow(new DateTime(a.assessment_time.getTime),
        s"${u.last_name}, ${u.first_name}",
        p.name.is, i.name.is, None, a.pass, opStr(a.notes))))
      orderBy(a.assessment_time desc)
    )
    val predefCommentsMap = AssessmentTag.expandedPredefinedCommentsForAssessments(rows.map(_.id))
    rows.map(addPredefinedComments(predefCommentsMap))
  }

  private def addPredefinedComments(predefCommentsMap: Map[Int, String])(ri: AssessmentRows.RowAndId): AssessmentRow = {
    predefCommentsMap.get(ri.id).map(predefComments => {
      val newNotes = ri.row.notes.map(predefComments + "; " + _) | predefComments
      ri.row.copy(notes = Some(newNotes))
    }) | ri.row
  }
}