package com.dbschools.mgb.schema

import org.squeryl.annotations.Column
import org.squeryl.PrimitiveTypeMode._

case class Group(
                          group_id:     Int,
                          name:         String,
  @Column("does_testing") doesTesting:  Boolean
)

object Group {
  /** TermGroupAssessments for all groups */
  lazy val groupsWithAssessments = from(AppSchema.assessments, AppSchema.musicianGroups)((a, mg) =>
    where(a.musician_id === mg.musician_id)
      groupBy(mg.school_year, mg.group_id)
      compute (count(a.assessment_id))
  ).map(g => TermGroupAssessments(g.key._1, g.key._2, g.measures.toInt))

  /** A map from terms to TermGroupAssessments */
  lazy val groupsWithAssessmentsByTerm: Map[Int, Seq[Int]] =
    groupsWithAssessments.groupBy(_.term).mapValues(_.map(_.groupId).toSeq)
}

case class TermGroupAssessments(term: Int, groupId: Int, numAssessments: Int)
