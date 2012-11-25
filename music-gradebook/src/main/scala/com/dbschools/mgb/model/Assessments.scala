package com.dbschools.mgb
package model

import org.scala_tools.time.Imports._
import org.squeryl.PrimitiveTypeMode._

case class AssessmentRow(date: DateTime, tester: String, piece: String,
  instrument: String, subinstrument: Option[String], pass: Boolean, notes: String)

object AssessmentRows {
  import schema.AppSchema.{assessments, pieces, instruments, subinstruments, users}

  def forMusician(id: Int): Iterable[AssessmentRow] =
    from(assessments, pieces, instruments, users)((a, p, i, u) =>
      where(a.musician_id === id and a.pieceId === p.id and a.instrument_id === i.id
        and a.user_id === u.id)
      select(AssessmentRow(new DateTime(a.assessment_time.getTime), "%s, %s".format(u.last_name, u.first_name),
        p.name.is, i.name.is, None, a.pass, a.notes))
      orderBy(a.assessment_time desc)
    )
}