package com.dbschools.mgb.snippet

import com.dbschools.mgb.model.{LastPass, GroupAssignment, Cache}
import net.liftweb.http.js.JE.JsRaw
import net.liftweb.http.js.JsCmds.Script
import net.liftweb.http.js.JsCmds._

trait LocationsGraph extends ChartFeatures {
  val LocationsGraphWidth = 3 * Cache.pieces.size
  val LocationsGraphHeight = 3 * 5

  def makeLocationsChart(selector: String, groupAssignments: Seq[GroupAssignment],
    lastPassesByMusician: Map[Int, Iterable[LastPass]]) = {

    def makeLocationCounts = {
      val counts = Array.fill(Cache.pieces.size)(0)
      val musicianIds = groupAssignments.map(_.musician.id).toSet
      for {
        (mid, passes) <- lastPassesByMusician
        if musicianIds contains mid
        pass <- passes
      } counts(pass.position) += 1
      counts
    }

    val pieceColors = Cache.pieces.map(p => findBookIndex(p) + 1)
    val locCounts = toA(makeLocationCounts)
    Script(JsRaw(s"drawLocationsChart('$selector', $locCounts, ${toA(pieceColors)})"))
  }

}
