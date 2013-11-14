package com.dbschools.mgb
package snippet

import net.liftweb._
import util._
import Helpers._
import schema.TermGroupAssessments
import model.Cache

class History {

  def assessmentsByYearAndGroup = {
    case class WithGroupName(tg: TermGroupAssessments, name: String)
    val sortedWithNames = {
      val groups = Cache.groups.map(g => g.id -> g).toMap
      val withNames = schema.Group.groupsWithAssessments.map(t => WithGroupName(t, groups(t.groupId).name))
      withNames.toSeq.sortBy(_.name).sortWith(_.tg.term > _.tg.term)
    }
    val nf = java.text.NumberFormat.getInstance

    "#row" #> sortedWithNames.map(wn =>
      ".schYear     *" #> wn.tg.term &
      ".group       *" #> wn.name &
      ".assessments *" #> nf.format(wn.tg.numAssessments)
    )
  }
}
