package com.dbschools.mgb
package snippet

import scalaz._
import Scalaz._
import net.liftweb.common.{Empty, Loggable, Full}
import net.liftweb.http.SHtml
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmds.{Noop, ReplaceOptions}
import model.{Cache, Terms}
import schema.MusicianGroup

class Selectors(callback: => Option[() => JsCmd] = None, onlyTestingGroups: Boolean = false) extends Loggable {
  var opSelectedTerm: Option[Int] = Some(Terms.currentTerm) // None means no specific term, therefore all
  var opSelectedGroupId = none[Int]                         // None means no specific group, therefore all
  var opSelectedInstId  = none[Int]                         // None means no specific instrument, therefore all

  var opCallback = callback

  private val All = "All"
  private val allItem = (All, All)

  def yearSelector = selector("yearSelector", allItem :: Terms.allTermsFormatted, opSelectedTerm, updateTerm)

  private def updateTerm(opTerm: Option[Int]) = {
    opSelectedTerm = opTerm
    // Show only the groups with assessments in the term
    ReplaceOptions(groupSelectorId, groupSelectValues, Empty): JsCmd
  }

  val groupSelectorId: String = "groupSelector"

  def groupSelector =
    selector(groupSelectorId, groupSelectValues, opSelectedGroupId, opSelectedGroupId = _)

  private def groupSelectValues: List[(String, String)] =
    allItem :: Cache.groups.toList.sortBy(_.name).map(g => g.id.toString -> g.name)

  def instrumentSelector = selector("instrumentSelector",
    allItem :: Cache.instruments.toList.sortBy(_.sequence.get).map(i => i.id.toString -> i.name.get),
    opSelectedInstId, opSelectedInstId = _)

  private def selector(id: String, items: List[(String, String)], opId: Option[Int], fn: (Option[Int]) => JsCmd) = {
    SHtml.ajaxSelect(items, Full(opId.map(_.toString) | All), sel => {
      fn(if (sel == All) None else Some(sel.toInt)) & (opCallback.map(_()) | Noop)
    }, "id" -> id)
  }

  def musicianGroups = MusicianGroup.selectedMusicians(opSelectedTerm, opSelectedGroupId, opSelectedInstId)
}
