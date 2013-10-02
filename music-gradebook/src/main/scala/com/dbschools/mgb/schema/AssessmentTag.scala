package com.dbschools.mgb.schema

import org.squeryl.annotations.Column
import org.squeryl.PrimitiveTypeMode._

case class AssessmentTag(
  @Column("assessment_id")                  assessmentId: Int,
  @Column("predefinedcomments_comment_id")  commentId:    Int
)

object AssessmentTag {
  /**
   * Returns a map of assessment ID to a string of expanded, sorted, predefined comments.
   */
  def expandedPredefinedCommentsForAssessments(assIds: Iterable[Int]): Map[Int, String] = {
    val tagsWithCom = from(AppSchema.assessmentTags, AppSchema.predefinedComments)((t, c) =>
      where(t.commentId === c.id and (t.assessmentId in assIds))
      select((t.assessmentId, c.commentText))
      orderBy c.commentText
    )
    (for {
      as <- tagsWithCom.groupBy(_._1).values
      cs = as.map(_._2).mkString(", ")
    } yield (as.head._1, cs)).toMap
  }
}

