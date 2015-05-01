package com.dbschools.mgb
package snippet

import scalaz._
import Scalaz._
import net.liftweb.common.{Loggable, Full}
import net.liftweb.http.SHtml
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmds.{Noop, ReplaceOptions}
import model.{Cache, Terms}
import schema.MusicianGroup

class Selectors(callback: => Option[() => JsCmd] = None, onlyTestingGroups: Boolean = false) extends Loggable {
  import Selectors._
  var selectedTerm   : Selection = Right(Terms.currentTerm)
  var selectedGroupId: Selection = Left(true)
  var selectedInstId : Selection = Left(true)

  var opCallback = callback

  private val All = "All"
  private val allItem = (All, All)
  private val NoneOption = "None"
  private val noneItem = (NoneOption, NoneOption)

  def yearSelector = selector("yearSelector", allItem :: Terms.allTermsFormatted, selectedTerm, updateTerm)

  private def updateTerm(opTerm: Selection) = {
    selectedTerm = opTerm
    // Show only the groups with assessments in the term
    selectedGroupId = Left(true)
    ReplaceOptions(groupSelectorId, groupSelectValues, Full(All)): JsCmd
  }

  val groupSelectorId: String = "groupSelector"

  def groupSelector =
    selector(groupSelectorId, groupSelectValues, selectedGroupId, selectedGroupId = _)

  private def groupSelectValues =
    allItem :: Cache.filteredGroups(rto(selectedTerm)).map(gp => gp.group.id.toString -> gp.group.name).toList

  def instrumentSelector(includeNone: Boolean = false, exclude: Seq[String] = Seq()) = {
    val nones = if (includeNone) Seq(noneItem) else Seq[(String, String)]()
    val instruments = Cache.instruments.filterNot(i => exclude.contains(i.name.get)).
      sortBy(_.sequence.get).map(i => i.id.toString -> i.name.get)
    selector("instrumentSelector", (nones :+ allItem) ++ instruments,
      selectedInstId, selectedInstId = _)
  }

  private def selector(id: String, items: Seq[(String, String)], opId: Selection, fn: (Selection) => JsCmd) = {
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
      fn(sel) & (opCallback.map(_()) | Noop)
    }, "id" -> id)
  }

  def musicianGroups = MusicianGroup.selectedMusicians(rto(selectedTerm), rto(selectedGroupId), rto(selectedInstId))
}

object Selectors {
  /** Left: false = None, true = All; Right(id) */
  type Selection = Either[Boolean, Int]
  /** Convert Selections that don’t include None to Option[Int] where None[Int] means All */
  def rto(s: Selection) = s.right.toOption orElse None
}
