package com.dbschools.mgb.snippet

import scalaz._
import Scalaz._
import org.squeryl.PrimitiveTypeMode._
import net.liftweb.common.Loggable
import net.liftweb.http.SHtml
import net.liftweb.common.Full
import com.dbschools.mgb.model.Terms
import com.dbschools.mgb.schema.AppSchema
import net.liftweb.http.js.JsCmd

class Selectors(changed: () => JsCmd, onlyTestingGroups: Boolean = false) extends Loggable {
  var opSelectedTerm: Option[Int] = Some(Terms.currentTerm) // None means no specific term, therefore all
  var opSelectedGroupId = none[Int]                         // None means no specific group, therefore all
  var opSelectedInstId  = none[Int]                         // None means no specific instrument, therefore all

  def yearSelector = selector(Terms.allTermsFormatted, opSelectedTerm, opSelectedTerm = _)

  def groupSelector = selector(groups.map(g => g.group_id.toString -> g.name),
    opSelectedGroupId, opSelectedGroupId = _)

  private def groups = {
    val g = AppSchema.groups.toList
    if (onlyTestingGroups) g.filter(_.doesTesting) else g
  }

  def instrumentSelector = selector(AppSchema.instruments.toList.map(i => i.id.toString -> i.name.is),
    opSelectedInstId, opSelectedInstId = _)

  private def selector(items: List[(String, String)], opId: Option[Int], fn: (Option[Int]) => Unit) = {
    val All = "All"
    SHtml.ajaxSelect((All, All) :: items, Full(opId.map(_.toString) | All), sel => {
      fn(if (sel == All) None else Some(sel.toInt))
      changed()
    })
  }

}
