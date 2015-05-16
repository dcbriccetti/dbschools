package com.dbschools.mgb
package snippet

import net.liftweb.http.js.JE.JsRaw
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmds._
import org.scala_tools.time.Imports._
import org.joda.time.Days
import org.squeryl.PrimitiveTypeMode._
import model.{GroupAssignment, Cache, Terms}
import model.Terms._
import schema.AppSchema

object PassChart {
  val PassGraphWidth = 100
  val PassGraphHeight = 50

  def create(groupAssignments: Iterable[GroupAssignment]) = {
    val mids = groupAssignments.map(_.musician.id).toSet
    Script(if (mids.isEmpty) Noop
    else {
      val curTermStart = Terms.termStart(Terms.currentTerm)
      val q = AppSchema.assessments.where(a => a.musician_id in mids and a.assessment_time > toTs(curTermStart))
      val ga = q.groupBy(_.musician_id)
      val now = DateTime.now
      val maxDays = Days.daysBetween(curTermStart, now).getDays
      val mostPasses = ga.values.foldLeft(0)((n, tests) => math.max(tests.count(_.pass), n))
      val xScale = PassGraphWidth / maxDays.toFloat
      val yScale = PassGraphHeight / mostPasses.toFloat
      def toA(vals: Iterable[Int]) = vals.mkString("[", ",", "]")
      JsRaw(ga.map {
        case (mid, tests) =>
          val streakTimes = (Cache.testingStatsByMusician.get(mid).map(_.longestPassingStreakTimes) getOrElse Seq()).toSet
          val stests = tests.filter(_.pass).toSeq.sortBy(_.assessment_time.getTime)
          var np = 0
          val points = stests.map(test => {
            val d = new DateTime(test.assessment_time)
            val nd = Days.daysBetween(curTermStart, d).getDays
            np += 1
            (nd, np, if (streakTimes contains test.assessment_time.getTime) 1 else 0)
          })
          val xs = toA(points.map(n => (n._1 * xScale).toInt))
          val ys = toA(points.map(n => (n._2 * yScale).toInt))
          val attribs = toA(points.map(_._3))
          JsRaw(s"drawChart($mid, $xs, $ys, $attribs);"): JsCmd
      }.fold(Noop)(_ & _))
    })
  }
}
