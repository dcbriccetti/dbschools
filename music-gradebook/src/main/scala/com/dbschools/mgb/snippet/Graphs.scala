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

  private def replacePie(template: String, elemId: String) =
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
    val sortedInstruments = MusicianGroup.selectedInstruments(
      selectors.opSelectedTerm, selectors.opSelectedGroupId).filter(_._1.name.is != "Unassigned")
    val names = sortedInstruments.map(_._1.name.is.toString)
    val portions = sortedInstruments.map(_._2.toInt)
    Flot.renderPie("instruments_graph", Pie(portions, Some(names)))
  }

  def progress = {
    Flot.render("progress_graph", createFlotSeries :: Nil, new FlotOptions {}, Noop)
  }

  private def createFlotSeries: FlotSerie = {
    val lpf = new LastPassFinder()
    val musicianIds = selectedMusicians.map(_.musician_id.is).toSet
    val lastPassedByPieceId = lpf.lastPassed().filter(lp => musicianIds.contains(lp.musicianId)).groupBy(_.pieceId)
    val d1 = lastPassedByPieceId.map {case (pieceId, value) => (pieceId.toDouble, value.size.toDouble)}.toList

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
