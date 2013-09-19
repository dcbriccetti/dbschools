package com.dbschools.mgb
package snippet

import scalaz._
import Scalaz._
import org.squeryl.PrimitiveTypeMode._
import net.liftweb.common.{Empty, Loggable, Full}
import net.liftweb.http.SHtml
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmds.ReplaceOptions
import net.liftweb.http
import com.dbschools.mgb.model.Terms
import com.dbschools.mgb.schema.{MusicianGroup, AppSchema}

class Selectors(changed: () => JsCmd, onlyTestingGroups: Boolean = false) extends Loggable {
  var opSelectedTerm: Option[Int] = Some(Terms.currentTerm) // None means no specific term, therefore all
  var opSelectedGroupId = none[Int]                         // None means no specific group, therefore all
  var opSelectedInstId  = none[Int]                         // None means no specific instrument, therefore all

  val yearSelector = selector("yearSelector", Terms.allTermsFormatted, opSelectedTerm, updateTerm)

  private def updateTerm(opTerm: Option[Int]) = {
    opSelectedTerm = opTerm
    // Show only the groups with assessments in the term
    ReplaceOptions(groupSelectorId, groupSelectValues, Empty): JsCmd
  }

  val groupSelectorId: String = "groupSelector"

  val groupSelector =
    selector(groupSelectorId, groupSelectValues, opSelectedGroupId, opSelectedGroupId = _)

  private def groupSelectValues: List[(String, String)] = {
    val groups = AppSchema.groups.toList
    val opGroupIds = opSelectedTerm.flatMap(schema.Group.groupsWithAssessmentsByTerm.get)
    val filtered = opGroupIds.map(ids => groups.filter(g => ids.contains(g.id))) | groups
    filtered.sortBy(_.name).map(g => g.id.toString -> g.name)
  }

  val instrumentSelector = selector("instrumentSelector",
    AppSchema.instruments.toList.map(i => i.id.toString -> i.name.is),
    opSelectedInstId, opSelectedInstId = _)

  private def selector(id: String, items: List[(String, String)], opId: Option[Int], fn: (Option[Int]) => JsCmd) = {
    val All = "All"
    SHtml.ajaxSelect((All, All) :: items, Full(opId.map(_.toString) | All), sel => {
      fn(if (sel == All) None else Some(sel.toInt)) &
      changed()
    }, "id" -> id)
  }

  def musicianGroups = MusicianGroup.selectedMusicians(opSelectedTerm, opSelectedGroupId, opSelectedInstId)
}
