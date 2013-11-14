package com.dbschools.mgb
package model

import scalaz._
import Scalaz._
import org.scala_tools.time.Imports._
import org.squeryl.PrimitiveTypeMode._
import schema.{Musician, AssessmentTag, AppSchema}

case class AssessmentRow(assId: Int, date: DateTime, musician: Musician, tester: String, piece: String,
  instrument: String, subinstrument: Option[String], pass: Boolean, notes: Option[String])

object AssessmentRows {
  import AppSchema.{musicians, assessments, pieces, instruments, subinstruments, users}

  def apply(opMusicianId: Option[Int], limit: Int = 500): Iterable[AssessmentRow] = {
    val rows =
      join(assessments, musicians, pieces, instruments, users, subinstruments.leftOuter)((a, m, p, i, u, s) =>
        where(a.musician_id === opMusicianId.?)
        select AssessmentRow(a.id,
        new DateTime(a.assessment_time.getTime), m, u.last_name, p.name.get, i.name.get,
        s.map(_.name.get), a.pass, opStr(a.notes))
        orderBy (a.assessment_time desc)
        on(
          a.musician_id       === m.id,
          a.pieceId           === p.id,
          a.instrument_id     === i.id,
          a.user_id           === u.id,
          a.subinstrument_id  === s.map(_.id)
        )
      ).page(0, limit).toSeq
    val predefCommentsMap = AssessmentTag.expandedPredefinedCommentsForAssessments(rows.map(_.assId))
    rows.map(addPredefinedComments(predefCommentsMap))
  }

  private def addPredefinedComments(predefCommentsMap: Map[Int, String])(row: AssessmentRow): AssessmentRow = {
    predefCommentsMap.get(row.assId).map(predefComments => {
      val newNotes = row.notes.toSeq.filter(_.nonEmpty).map(predefComments + "; " + _).headOption | predefComments
      row.copy(notes = Some(newNotes))
    }) | row
  }

  private def opStr(s: String) = if (s.trim.isEmpty) None else Some(s)
}

object Assessments {
  def delete(ids: Iterable[Int]): Unit = {
    import AppSchema.{assessments, assessmentTags}
    assessmentTags.deleteWhere(at => at.assessmentId in ids)
    assessments.deleteWhere(a => a.id in ids)
  }
}
