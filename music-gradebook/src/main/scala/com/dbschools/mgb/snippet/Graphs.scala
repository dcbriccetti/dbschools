package com.dbschools.mgb.snippet

import net.liftmodules.widgets.flot.{Pie, Flot}
import com.dbschools.mgb.model.Terms
import com.dbschools.mgb.schema.MusicianGroup

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
}
