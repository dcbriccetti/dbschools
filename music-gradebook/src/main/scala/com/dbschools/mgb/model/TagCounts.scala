package com.dbschools.mgb.model

import org.squeryl.PrimitiveTypeMode._
import com.dbschools.mgb.schema.AppSchema

trait TagCounts {
  private lazy val predefCommentsById = Cache.tags.map(pc => pc.id -> pc.commentText).toMap

  case class TagCount(tag: String, count: Long)

  def tagCounts(musicianId: Int) = {
    val q = from(AppSchema.assessments)(a => where(a.musician_id === musicianId) select a.id)
    from(AppSchema.assessmentTags)(t =>
      where(t.assessmentId in q)
      groupBy t.commentId
      compute count(t.commentId)
    ).map(gm => TagCount(predefCommentsById(gm.key), gm.measures)).toSeq.sortBy(_.count).reverse
  }
}