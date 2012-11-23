package com.dbschools.mgb.snippet

import net.liftmodules.widgets.flot.{FlotBarsOptions, FlotOptions, FlotSerie, Pie, Flot}
import net.liftweb.http
import http.js.JsCmds.Noop
import com.dbschools.mgb.model.{LastPassFinder, Terms}
import com.dbschools.mgb.schema.MusicianGroup
import net.liftweb.common.Full

class Graphs {

  def grades = {
    val grouped = MusicianGroup.musiciansInCurrentTerm.groupBy(_.graduation_year.is)
    val sortedYears = grouped.keys.toSeq.sorted.reverse
    val labels = sortedYears.map(y => Terms.graduationYearAsGrade(y).toString)
    val portions = sortedYears.map(grouped).map(_.size).toSeq
    Flot.renderPie("grades_graph", Pie(portions, Some(labels.toSeq)))
  }

  def instruments = {
    val grouped = MusicianGroup.instrumentsInCurrentTerm.groupBy(_.name.is)
    val sortedInstrumentNames = grouped.keys.toSeq.sorted.filter(_ != "Unassigned")
    val portions = sortedInstrumentNames.map(grouped).map(_.size).toSeq
    Flot.renderPie("instruments_graph", Pie(portions, Some(sortedInstrumentNames.toSeq)))
  }

  def progress = {
    val lpf = new LastPassFinder()
    val termMusicianIds = MusicianGroup.musiciansInCurrentTerm.map(_.musician_id.is).toSet
    val lps = lpf.lastPassed().filter(lp => termMusicianIds.contains(lp.musicianId))
    val buckets = Array.fill[Int](lpf.numPieces)(0)
    lps.foreach(lp => {
      buckets(lpf.pieceIdToPosition(lp.pieceId)) += 1
    })
    val d1 = buckets.toList.zipWithIndex.map {case (numAtPos, pos) => (pos.toDouble, numAtPos.toDouble) }

    val s1 = new FlotSerie() {
      override val data = d1
      override val bars = Full (new FlotBarsOptions () {
        override val show = Full(true)
      })
    }

    Flot.render("progress_graph", s1 :: Nil, new FlotOptions {}, Noop)
  }
}
