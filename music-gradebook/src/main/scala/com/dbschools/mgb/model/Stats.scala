package com.dbschools.mgb.model

import scala.math._

object Stats {

  def stdev(list: Iterable[Double], average: Double) = {
    def squaredDiff(v1: Double, v2: Double) = pow(v1 - v2, 2.0)

    if (list.isEmpty) 0.0 else {
      val sumSqDiffs = list.foldLeft(0.0)(_ + squaredDiff(_, average))
      sqrt(sumSqDiffs / list.size)
    }
  }
}
