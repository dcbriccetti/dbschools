package com.dbschools.mgb
package snippet

import scalaz._
import Scalaz._
import net.liftweb.common.{Loggable, Full}
import net.liftweb.http._
import js.JsCmd
import js.JsCmds.{Noop, ReplaceOptions}
import model.{Cache, SchoolYears}
import schema.MusicianGroup

class Selectors(callback: => Option[() => JsCmd] = None, onlyTestingGroups: Boolean = false) extends Loggable {
  import Selectors._
  import Selection.AllItems
  var selectedSchoolYear  = Selection(SchoolYears.current)
  var selectedGroupId     = AllItems
  var selectedInstId      = AllItems

  var opCallback = callback

  def yearSelector = selector("yearSelector", allItem :: SchoolYears.allFormatted, selectedSchoolYear, updateTerm, opCallback)

  private def updateTerm(opTerm: Selection) = {
    selectedSchoolYear = opTerm
    // Show only the groups with assessments in the term
    selectedGroupId = AllItems
    ReplaceOptions(groupSelectorId, groupSelectValues, Full(All)): JsCmd
  }

  val groupSelectorId: String = "groupSelector"

  def groupSelector =
    selector(groupSelectorId, groupSelectValues, selectedGroupId, selectedGroupId = _, opCallback)

  private def groupSelectValues = allItem :: Selectors.groupsWithoutAll(selectedSchoolYear)

  def instrumentSelector = {
    val instruments = Cache.instruments.sortBy(_.sequence.get).map(i => i.id.toString -> i.name.get)
    selector("instrumentSelector", allItem +: instruments, selectedInstId, selectedInstId = _, opCallback)
  }

  def musicianGroups = MusicianGroup.selectedMusicians(selectedSchoolYear.rto, selectedGroupId.rto, selectedInstId.rto)
}

object Selectors {
  val All = "All"
  val allItem = (All, All)
  val NoneOption = "None"
  val noneItem = (NoneOption, NoneOption)

  def selector(
    id:       String,
    items:    Seq[(String, String)],
    selection:Selection,
    fn:       (Selection) => JsCmd,
    callback: => Option[() => JsCmd],
    attrs:    (String, String)*
  ) = {
    val allAttrs = attrs :+ "id" -> id
    SHtml.ajaxUntrustedSelect(items, Full(selection.value match {
      case Left(false)  => NoneOption
      case Left(true)   => All
      case Right(num)   => num.toString
    }), selString => {
      val sel = selString match {
        case NoneOption => Selection.NoItems
        case All        => Selection.AllItems
        case n          => Selection(n.toInt)
      }
      fn(sel) & (callback.map(_()) | Noop)
    }, allAttrs: _*)
  }
  
  def groupsWithoutAll(selectedTerm: Selection) =
    Cache.filteredGroups(selectedTerm.rto).map(gp => gp.group.id.toString -> gp.group.shortOrLongName).toList
}

/** Left: false = None, true = All; Right(id) */
case class Selection(value: Either[Boolean, Int]) {
  /** Convert Selections that don’t include None to Option[Int] where None[Int] means All */
  def rto = value.right.toOption orElse None

  def matches(id: Int) = value match {
    case Right(thisId) => id == thisId
    case Left(b) => b
  }

  def isAll = value match {
    case Left(true) => true
    case _ => false
  }
}

object Selection {
  def apply(all: Boolean): Selection = Selection(Left(all))
  def apply(id: Int)     : Selection = Selection(Right(id))
  val AllItems = Selection(all = true)
  val NoItems  = Selection(all = false)
}
