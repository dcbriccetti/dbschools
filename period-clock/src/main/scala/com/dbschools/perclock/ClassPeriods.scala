package com.dbschools.perclock

import org.scalajs.dom.raw.HTMLCanvasElement

import scala.scalajs.js
import scala.scalajs.js.Date
import scala.scalajs.js.timers._
import org.scalajs.dom.ext._
import org.scalajs.dom
import model.{Period, Periods}

object ClassPeriods extends js.JSApp {

  def main(): Unit = {
    def gbid(id: String) = dom.document.getElementById(id)
    val timeRemaining = gbid("timeRemaining")
    val period = gbid("period")
    val periodNumber = gbid("periodNumber")
    val periodRange = gbid("periodRange")
    val periodLength = gbid("periodLength")
    val progressMins = gbid("progressMins")

    def update(): Unit = {
      Periods.periodWithin match {
        case p: Period =>
          period.setAttribute("class", "")
          val secs = Math.floor((p.endMs - new Date().getTime) / 1000)
          timeRemaining.setAttribute("max", p.totalSecs.toString)
          timeRemaining.setAttribute("value", secs.toString)
          periodNumber.innerHTML = p.num.toString
          periodRange.innerHTML = p.formattedRange
          periodLength.innerHTML = (p.totalSecs.toInt / 60).toString
          val remaining = p.formattedTimeRemaining
          progressMins.innerHTML = remaining
          dom.document.title = remaining
        case _ =>
          period.setAttribute("class", "hide")
          timeRemaining.setAttribute("value", "0")
          periodNumber.innerHTML = ""
          periodRange.innerHTML = ""
          periodLength.innerHTML = ""
          progressMins.innerHTML = ""
          dom.document.title = "Between Periods"
      }
    }

    update()

    setInterval(1000)(update())

    val canvas = dom.document.getElementById("canvas").cast[HTMLCanvasElement]
    val ctx = canvas.getContext("2d").cast[dom.CanvasRenderingContext2D]

    canvas.width = canvas.parentElement.clientWidth
    canvas.height = 350
    ctx.fillStyle = "#f8f8f8"
    ctx.fillRect(0, 0, canvas.width, canvas.height)

    val firstStartMs = Periods.week.map(_.map(_.startMs).min).min
    val lastEndMs    = Periods.week.map(_.map(_.endMs  ).max).max
    val totalMs = lastEndMs - firstStartMs
    def scale(ms: Double) = ((ms - firstStartMs) / totalMs) * canvas.height
    val fills = Seq("red",   "orange", "yellow", "green", "blue",  "indigo", "violet")
    val texts = Seq("white", "black",  "black",  "white", "white", "white",  "black")
    val colMargin = 6
    val colWidth = (canvas.width - colMargin * (5 - 1)) / 5
    var labeledStartTime = Set[Double]()
    var labeledEndTime   = Set[Double]()

    Periods.week.zipWithIndex.foreach {
      case (periods, i) =>
        periods.foreach(p => {
          ctx.fillStyle = fills(p.num - 1)
          val x = i * colWidth + i * colMargin
          val yStart = scale(p.startMs)
          val yEnd = scale(p.endMs)
          val w = colWidth
          val h = yEnd - yStart
          ctx.fillRect(x, yStart, w, h)
          val labelX = x + 2
          if (! labeledStartTime.contains(p.startMs)) {
            ctx.fillStyle = texts(p.num - 1)
            ctx.fillText(p.formattedStart, labelX, yStart + 10)
            labeledStartTime += p.startMs
          }
          if (! labeledEndTime.contains(p.endMs)) {
            ctx.fillStyle = texts(p.num - 1)
            ctx.fillText(p.formattedEnd, labelX, yEnd - 3)
            labeledEndTime += p.endMs
          }
        })
    }
  }
}
