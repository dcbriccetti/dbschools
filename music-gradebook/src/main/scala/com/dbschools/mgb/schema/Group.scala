package com.dbschools.mgb.schema

import org.squeryl.annotations.Column
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.KeyedEntity

case class Group(
                          id:           Int,
                          name:         String,
                          shortName:    Option[String],
  @Column("does_testing") doesTesting:  Boolean
) extends KeyedEntity[Int] {
  def this() = this(0, "", Some(""), false)
}

object Group {
  /** TermGroupAssessments for all groups */
  def groupsWithAssessments = from(AppSchema.assessments, AppSchema.musicianGroups)((a, mg) =>
    where(a.musician_id === mg.musician_id)
      groupBy(mg.school_year, mg.group_id)
      compute count(a.id)
  ).map(g => TermGroupAssessments(g.key._1, g.key._2, g.measures.toInt))
}

case class TermGroupAssessments(term: Int, groupId: Int, numAssessments: Int)
