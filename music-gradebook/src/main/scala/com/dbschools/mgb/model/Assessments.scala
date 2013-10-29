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
  import schema.AppSchema.{assessments, pieces, instruments, subinstruments, users}

  case class RowAndId(id: Int, row: AssessmentRow)
  
  def opStr(s: String) = if (s.trim.isEmpty) None else Some(s)

  def forMusician(id: Int): Iterable[AssessmentRow] = {
    val rows = join(assessments, pieces, instruments, users, subinstruments.leftOuter)((a, p, i, u, s) =>
      where(a.musician_id === id)
      select RowAndId(a.id, AssessmentRow(
        new DateTime(a.assessment_time.getTime), u.last_name, p.name.is, i.name.is,
          s.map(_.name.is), a.pass, opStr(a.notes)))
      orderBy(a.assessment_time desc)
      on(a.pieceId === p.id, a.instrument_id === i.id, a.user_id === u.id, a.subinstrument_id === s.map(_.id))
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