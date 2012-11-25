package com.dbschools.mgb
package snippet

import org.squeryl.PrimitiveTypeMode._
import net.liftweb._
import util._
import schema.{AppSchema, TermGroupAssessments}

class History {

  def assessmentsByYearAndGroup = {
    case class WithGroupName(tg: TermGroupAssessments, name: String)
    val groups = AppSchema.groups.map(g => g.group_id -> g).toMap
    val withNames = schema.Group.groupsWithAssessments.map(t => WithGroupName(t, groups(t.groupId).name))
    val sortedWithNames = withNames.toSeq.sortBy(_.name).sortWith(_.tg.term > _.tg.term)
    val nf = java.text.NumberFormat.getInstance

    "#row" #> sortedWithNames.map(wn =>
      ".schYear     *" #> wn.tg.term &
      ".group       *" #> wn.name &
      ".assessments *" #> nf.format(wn.tg.numAssessments)
    )
  }
}
