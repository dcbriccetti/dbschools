package com.dbschools.mgb
package snippet

import scalaz._
import Scalaz._
import net.liftweb.http.js.JE.JsRaw
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmds._
import org.scala_tools.time.Imports._
import org.joda.time.Days
import org.squeryl.PrimitiveTypeMode._
import model.{GroupAssignment, Cache, Terms}
import model.Terms._
import schema.{Piece, AppSchema}

trait ChartFeatures {
  val bookNames = Array("Red", "Blue", "Green")
  def findBookIndex(piece: Piece) = bookNames.indexWhere(bn => piece.name.get startsWith bn)
  def toA(vals: Iterable[Int]) = vals.mkString("[", ",", "]")
}

object PassChart extends ChartFeatures {
  val PassGraphWidth = 100
  val PassGraphHeight = 50

  def create(groupAssignments: Iterable[GroupAssignment], onlyCurrentTerm: Boolean) = {
    val mids = groupAssignments.map(_.musician.id).toSet
    Script(if (mids.isEmpty) Noop
    else {
      val curTermStart = onlyCurrentTerm ? Cache.currentMester | Terms.termStart(Terms.currentTerm)
      val q = AppSchema.assessments.where(a => a.musician_id in mids and a.assessment_time > toTs(curTermStart))
      val ga = q.groupBy(_.musician_id)
      val now = DateTime.now
      val maxDays = Days.daysBetween(curTermStart, now).getDays
      val mostPasses = ga.values.foldLeft(0)((n, tests) => math.max(tests.count(_.pass), n))
      val xScale = PassGraphWidth / maxDays.toFloat
      val yScale = PassGraphHeight / mostPasses.toFloat
      case class ChartData(x: Int, y: Int, attrib: Int, axisAttrib: Int)
      val piecesById = Cache.pieces.map(p => p.id -> p).toMap

      JsRaw(ga.map {
        case (mid, tests) =>
          val streakTimes = (Cache.testingStatsByMusician(mid).map(_.longestPassingStreakTimes) getOrElse Seq()).toSet
          val stests = tests.filter(_.pass).toSeq.sortBy(_.assessment_time.getTime)
          var np = 0
          val points = stests.map(test => {
            val d = new DateTime(test.assessment_time)
            val nd = Days.daysBetween(curTermStart, d).getDays
            np += 1
            val bookIndex =
            for {
              piece <- piecesById.get(test.pieceId)
              i = findBookIndex(piece)
              if i >= 0
            } yield i
            ChartData(nd, np, if (streakTimes contains new DateTime(test.assessment_time.getTime)) 1 else 0,
              bookIndex.map(_ + 1) getOrElse 0)
          })
          val xs = toA(points.map(n => (n.x * xScale).toInt))
          val ys = toA(points.map(n => (n.y * yScale).toInt))
          val attribs = toA(points.map(_.attrib))
          val axisAttribs = toA(points.map(_.axisAttrib))
          JsRaw(s"drawChart($mid, $xs, $ys, $attribs, $axisAttribs);"): JsCmd
      }.fold(Noop)(_ & _))
    })
  }
}
