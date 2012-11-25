package com.dbschools.mgb.schema

import org.squeryl.annotations.Column
import org.squeryl.PrimitiveTypeMode._

case class Group(
                          group_id:     Int,
                          name:         String,
  @Column("does_testing") doesTesting:  Boolean
)

object Group {
  /** Maps from terms to the IDs of groups that have assessments in that term */
  lazy val groupsWithAssessmentsByTerm: Map[Int, Seq[Int]] =
    from(AppSchema.assessments, AppSchema.musicianGroups)((a, mg) =>
      where(a.musician_id === mg.musician_id)
      groupBy(mg.school_year, mg.group_id)
      compute(count(a.assessment_id))
    ).map(g => TermGroup(g.key._1, g.key._2)).groupBy(_.term).mapValues(_.map(_.groupId).toSeq)
}

case class TermGroup(term: Int, groupId: Int)
