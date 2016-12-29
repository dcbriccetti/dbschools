package com.dbschools.mgb.model

import scala.math._

object Stats {

  def stdev(list: Iterable[Double], average: Double): Double = {
    def squaredDiff(v1: Double, v2: Double) = pow(v1 - v2, 2.0)

    if (list.isEmpty) 0.0 else {
      val sumSqDiffs = list.foldLeft(0.0)(_ + squaredDiff(_, average))
      sqrt(sumSqDiffs / list.size)
    }
  }

  case class Point(x: Int, y: Double)

  def regressionSlope(points: Iterable[Point]): Double = {
    val xs = points.map(_.x)
    val ys = points.map(_.y)
    val xys = points.map(c => c.x * c.y)
    val xmean = xs.sum / xs.size
    val ymean = ys.sum / ys.size
    val xymean = xys.sum / xys.size
    val xssq = xs.map(x => x * x)
    val xssqmean = xssq.sum / xssq.size
    val num = xmean * ymean - xymean
    val denom = xmean * xmean - xssqmean
    num / denom
  }
}
