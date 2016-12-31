package com.dbschools.mgb
package snippet

import scala.xml.Node
import scalaz._
import Scalaz._
import net.liftweb.http.js.JE.JsRaw
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmds._
import org.scala_tools.time.Imports._
import org.joda.time.Days
import org.squeryl.PrimitiveTypeMode._
import model.{Cache, Terms}
import model.Terms._
import schema.{AppSchema, Piece}

trait ChartFeatures {
  val bookNames = Array("Red", "Blue", "Green")
  def findBookIndex(piece: Piece): Int = bookNames.indexWhere(bn => piece.name.get startsWith bn)
  def toA(vals: Iterable[Int]): String = vals.mkString("[", ",", "]")
}

object PassChart extends ChartFeatures {
  val PassGraphWidthSmall = 100
  val PassGraphHeightSmall = 50

  def create(mids: Iterable[Int], onlyCurrentTerm: Boolean,
             width: Int = PassGraphWidthSmall, height: Int = PassGraphHeightSmall): Node = {
    Script(if (mids.isEmpty) Noop
    else {
      val curTermStart = onlyCurrentTerm ? Cache.currentMester | Terms.termStart(Terms.currentTerm)
      val q = AppSchema.assessments.where(a => a.musician_id in mids and a.assessment_time > toTs(curTermStart))
      val ga = q.groupBy(_.musician_id)
      val now = DateTime.now
      val maxDays = Days.daysBetween(curTermStart, now).getDays
      val mostPasses = ga.values.foldLeft(0)((n, tests) => math.max(tests.count(_.pass), n))
      val xScale = width / maxDays.toFloat
      val piecesById = Cache.pieces.map(p => p.id -> p).toMap
      val yScale = height / mostPasses.toFloat

      case class ChartData(x: Int, y: Int, attrib: Int, axisAttrib: Int)

      JsRaw(ga.map {
        case (musicianId, tests) =>
          val streakTimes =
            (Cache.selectedTestingStatsByMusician(musicianId).map(_.longestPassingStreakTimes) getOrElse Seq()).toSet
          val sortedTests = tests.filter(_.pass).toSeq.sortBy(_.assessment_time.getTime)
          val chartDatas = sortedTests.zipWithIndex.map {
            case (test, y) =>
              val termDayOffset = Days.daysBetween(curTermStart, new DateTime(test.assessment_time)).getDays
              val opBookNumber =
                for {
                  piece <- piecesById.get(test.pieceId)
                  i = findBookIndex(piece)
                  if i >= 0
                } yield i + 1
              val attrib = if (streakTimes contains new DateTime(test.assessment_time.getTime)) 1 else 0
              val axisAttrib = ~opBookNumber
              ChartData(termDayOffset, y, attrib, axisAttrib)
          }

          val xs = toA(chartDatas.map(cd => (cd.x * xScale).toInt))
          val ys = toA(chartDatas.map(cd => (cd.y * yScale).toInt))
          val attribs = toA(chartDatas.map(_.attrib))
          val axisAttribs = toA(chartDatas.map(_.axisAttrib))
          JsRaw(s"drawChart($musicianId, $xs, $ys, $attribs, $axisAttribs);"): JsCmd
      }.fold(Noop)(_ & _))
    })
  }
}
