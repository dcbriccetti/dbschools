package com.dbschools.mgb
package model

import java.text.NumberFormat
import scalaz._
import Scalaz._
import org.scala_tools.time.Imports._
import testingState._

case class SessionStats(rows: Seq[TestingMusician], num: Int, avgMins: Option[Double], avgMinsStr: String,
    σ: Option[Double], σStr: String)

object SessionStats {
  private val fnum = NumberFormat.getNumberInstance
  fnum.setMaximumFractionDigits(2)

  def apply(userId: Int): SessionStats = {
    val rows = testingMusicians.filter(_.tester.id == userId).toSeq.sortBy(-_.startingTime.millis)
    val n = rows.size
    val lengths = if (n < 2) List[Double]() else
      for {
        i <- 1 until n
      } yield (rows(i - 1).startingTime.getMillis - rows(i).startingTime.getMillis) / 1000.0 / 60
    val avgMins = if (n < 2) None else Some(lengths.sum / (n - 1))
    val opσ = avgMins.map(am => Stats.stdev(lengths, am))
    SessionStats(rows, n, avgMins, ~avgMins.map(fnum.format), opσ, ~opσ.map(fnum.format))
  }
}
