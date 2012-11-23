package com.dbschools.mgb.snippet

import net.liftmodules.widgets.flot.{FlotBarsOptions, FlotOptions, FlotSerie, Pie, Flot}
import net.liftweb.http
import http.js.JsCmds.{Replace, Noop}
import http.Templates
import net.liftweb.common.{Loggable, Full}
import com.dbschools.mgb.model.{LastPassFinder, Terms}
import com.dbschools.mgb.schema.MusicianGroup

class Graphs extends Loggable {

  def yearSelector = selectors.yearSelector
  def groupSelector = selectors.groupSelector
  def instrumentSelector = selectors.instrumentSelector

  private val selectors = new Selectors(() =>
    Flot.renderJs("progress_graph", createFlotSeries :: Nil, new FlotOptions {}, Noop) &
    replacePie("_gradesPie", "grades_graph_wrapper") &
    replacePie("_instrumentsPie", "instruments_graph_wrapper"),
    onlyTestingGroups = true)

  def replacePie(template: String, elemId: String) =
    Templates(List(template)).map(Replace(elemId, _)) openOr {
      logger.error("Error loading template " + template)
      Noop
    }

  def grades = {
    val grouped = selectedMusicians.groupBy(_.graduation_year.is)
    val sortedYears = grouped.keys.toSeq.sorted.reverse
    val labels = sortedYears.map(y => Terms.graduationYearAsGrade(y).toString)
    val portions = sortedYears.map(grouped).map(_.size).toSeq
    Flot.renderPie("grades_graph", Pie(portions, Some(labels.toSeq)))
  }

  def instruments = {
    val grouped = MusicianGroup.selectedInstruments(
      selectors.opSelectedTerm, selectors.opSelectedGroupId).groupBy(_.name.is)
    val sortedInstrumentNames = grouped.keys.toSeq.sorted.filter(_ != "Unassigned")
    val portions = sortedInstrumentNames.map(grouped).map(_.size).toSeq
    Flot.renderPie("instruments_graph", Pie(portions, Some(sortedInstrumentNames.toSeq)))
  }

  def progress = {
    Flot.render("progress_graph", createFlotSeries :: Nil, new FlotOptions {}, Noop)
  }

  private def createFlotSeries: FlotSerie = {
    val lpf = new LastPassFinder()
    val termMusicianIds = selectedMusicians.map(_.musician_id.is).toSet
    val lps = lpf.lastPassed().filter(lp => termMusicianIds.contains(lp.musicianId))
    val buckets = Array.fill[Int](lpf.numPieces)(0)
    lps.foreach(lp => {
      buckets(lpf.pieceIdToPosition(lp.pieceId)) += 1
    })
    val d1 = buckets.toList.zipWithIndex.map {
      case (numAtPos, pos) => (pos.toDouble, numAtPos.toDouble)
    }

    new FlotSerie() {
      override val data = d1
      override val bars = Full(new FlotBarsOptions() {
        override val show = Full(true)
      })
    }
  }

  private def selectedMusicians =
    MusicianGroup.selectedMusicians(selectors.opSelectedTerm,
      selectors.opSelectedGroupId, selectors.opSelectedInstId)
}
