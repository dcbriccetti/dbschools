package com.dbschools.mgb.snippet

import org.squeryl.PrimitiveTypeMode._
import net.liftmodules.widgets.flot.{FlotBarsOptions, FlotOptions, FlotSerie, Pie, Flot}
import net.liftweb.http
import http.js.JsCmds.{SetHtml, Noop}
import http.Templates
import net.liftweb._
import common.{Full, Loggable}
import util._
import Helpers._
import com.dbschools.mgb.model.{Stats, LastPassFinder, Terms}
import com.dbschools.mgb.schema.{AppSchema, MusicianGroup}

class Graphs extends Loggable {
  val pieces = AppSchema.pieces.toSeq

  def yearSelector = selectors.yearSelector
  def groupSelector = selectors.groupSelector
  def instrumentSelector = selectors.instrumentSelector

  private val selectors = new Selectors(updatePage, onlyTestingGroups = true)

  private def updatePage() =
    Flot.renderJs("progress_graph", createFlotSeries :: Nil, new FlotOptions {}, Noop) &
    updateElement("_progressNumbers", "progress_numbers_wrapper") &
    updateElement("_gradesPie", "grades_graph_wrapper") &
    updateElement("_instrumentsPie", "instruments_graph_wrapper")

  def grades = {
    val gradesToMgms = selectors.musicianGroups.groupBy(mgm =>
      Terms.graduationYearAsGrade(mgm.m.graduation_year.is, mgm.mg.school_year))
    val sortedYears = gradesToMgms.keys.toSeq.sorted
    val labels = sortedYears.map(_.toString)
    val portions = sortedYears.map(gradesToMgms).map(_.size).toSeq
    Flot.renderPie("grades_graph", Pie(portions, Some(labels.toSeq)))
  }

  def instruments = {
    val sortedInstruments = MusicianGroup.selectedInstruments(
      selectors.opSelectedTerm, selectors.opSelectedGroupId).filter(_._1.name.is != "Unassigned")
    val names = sortedInstruments.map(_._1.name.is.toString)
    val portions = sortedInstruments.map(_._2.toInt)
    Flot.renderPie("instruments_graph", Pie(portions, Some(names)))
  }

  def progress = {
    Flot.render("progress_graph", createFlotSeries :: Nil, new FlotOptions {}, Noop)
  }

  def progressNums = {
    val musicianPositions = lastPasses.map(_.position)
    val sumHighest = musicianPositions.sum
    val numMusicians = musicianPositions.size
    val meanHighest = if (numMusicians == 0) 0 else sumHighest.toDouble / numMusicians
    val stdevHighest = Stats.stdev(musicianPositions.map(_.toDouble), meanHighest)
    val fmt = java.text.NumberFormat.getInstance

    "#numStudents *" #> fmt.format(numMusicians) &
    "#numPieces   *" #> fmt.format(pieces.size) &
    "#meanLast    *" #> fmt.format(meanHighest) &
    "#stdevLast   *" #> fmt.format(stdevHighest)
  }

  private def createFlotSeries: FlotSerie = {
    val d1 = lastPasses.groupBy(_.pieceId).map {case (pieceId, value) => (pieceId.toDouble, value.size.toDouble)}.toList

    new FlotSerie() {
      override val data = d1
      override val bars = Full(new FlotBarsOptions() {
        override val show = Full(true)
      })
    }
  }

  private def lastPasses: Iterable[LastPassFinder#LastPass] = {
    val lpf = new LastPassFinder()
    val musicianIds = selectors.musicianGroups.map(_.m.musician_id.is).toSet
    val opTermEnd = selectors.opSelectedTerm.map(Terms.termEnd)
    val lastPasses = lpf.lastPassed(upTo = opTermEnd)
    lastPasses.filter(lp => musicianIds.contains(lp.musicianId))
  }

  private def updateElement(template: String, elemId: String) =
    Templates(List(template)).map(SetHtml(elemId, _)) openOr {
      logger.error("Error loading template " + template)
      Noop
    }
}
