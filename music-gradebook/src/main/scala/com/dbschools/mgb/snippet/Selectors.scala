package com.dbschools.mgb
package snippet

import scalaz._
import Scalaz._
import net.liftweb.common.{Loggable, Full}
import net.liftweb.http._
import js.JsCmd
import js.JsCmds.{Noop, ReplaceOptions}
import model.{Cache, Terms}
import schema.MusicianGroup

class Selectors(callback: => Option[() => JsCmd] = None, onlyTestingGroups: Boolean = false) extends Loggable {
  import Selectors._
  var selectedTerm   : Selection = Right(Terms.currentTerm)
  var selectedGroupId: Selection = Left(true)
  var selectedInstId : Selection = Left(true)

  var opCallback = callback

  def yearSelector = selector("yearSelector", allItem :: Terms.allTermsFormatted, selectedTerm, updateTerm, opCallback)

  private def updateTerm(opTerm: Selection) = {
    selectedTerm = opTerm
    // Show only the groups with assessments in the term
    selectedGroupId = Left(true)
    ReplaceOptions(groupSelectorId, groupSelectValues, Full(All)): JsCmd
  }

  val groupSelectorId: String = "groupSelector"

  def groupSelector =
    selector(groupSelectorId, groupSelectValues, selectedGroupId, selectedGroupId = _, opCallback)

  private def groupSelectValues =
    allItem :: Cache.filteredGroups(rto(selectedTerm)).map(gp => gp.group.id.toString -> gp.group.name).toList

  def instrumentSelector = {
    val instruments = Cache.instruments.sortBy(_.sequence.get).map(i => i.id.toString -> i.name.get)
    selector("instrumentSelector", allItem +: instruments, selectedInstId, selectedInstId = _, opCallback)
  }

  def musicianGroups = MusicianGroup.selectedMusicians(rto(selectedTerm), rto(selectedGroupId), rto(selectedInstId))
}

object Selectors {
  val All = "All"
  val allItem = (All, All)
  val NoneOption = "None"
  val noneItem = (NoneOption, NoneOption)

  /** Left: false = None, true = All; Right(id) */
  type Selection = Either[Boolean, Int]
  /** Convert Selections that don’t include None to Option[Int] where None[Int] means All */
  def rto(s: Selection) = s.right.toOption orElse None

  def selector(id: String, items: Seq[(String, String)], opId: Selection, fn: (Selection) => JsCmd,
    callback: => Option[() => JsCmd]) = {
    SHtml.ajaxUntrustedSelect(items, Full(opId match {
      case Left(false)  => NoneOption
      case Left(true)   => All
      case Right(num)   => num.toString
    }), selString => {
      val sel = selString match {
        case NoneOption => Left(false)
        case All        => Left(true)
        case n          => Right(n.toInt)
      }
      fn(sel) & (callback.map(_()) | Noop)
    }, "id" -> id)
  }
}
