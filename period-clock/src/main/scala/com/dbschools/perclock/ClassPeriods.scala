package com.dbschools.perclock

import scala.scalajs.js
import org.scalajs.dom
import dom.document
import model.{Period, Periods}

object ClassPeriods extends js.JSApp {

  def main(): Unit = {
    def gbid(id: String) = document.getElementById(id)
    val timeRemaining = gbid("timeRemaining")
    val period        = gbid("period")
    val periodNumber  = gbid("periodNumber")
    val periodRange   = gbid("periodRange")
    val periodLength  = gbid("periodLength")
    val progressMins  = gbid("progressMins")

    def update(): Unit = {
      Periods.periodWithin match {
        case p: Period =>
          period.setAttribute("class", "")
          val secs = Math.floor((p.endMs - Periods.nowMs) / 1000)
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

    dom.setInterval(update _, 1000)

    val drawing = document.getElementById("drawing")
    val XPad = 15 // todo Find how to get main’s padding
    val Height = 350
    val TopMargin = 12
    val ColMargin = 6

    val firstStartMs = Periods.week.map(_.map(_.startMs).min).min
    val lastEndMs    = Periods.week.map(_.map(_.endMs  ).max).max
    val totalMs = lastEndMs - firstStartMs
    def yFromMs(ms: Double) = (TopMargin + ((ms - firstStartMs) / totalMs) * (Height - TopMargin)).toInt
    val fills = Seq("red",   "orange", "yellow", "green", "blue",  "indigo", "violet")
    val texts = Seq("white", "black",  "black",  "white", "white", "white",  "black")
    val days = Seq("Mon", "Tue", "Wed", "Thu", "Fri")
    val colWidth = (drawing.clientWidth - ColMargin * (5 - 1)) / 5
    var labeledStartTime = Set[Double]()
    var labeledEndTime   = Set[Double]()

    Periods.week.zipWithIndex.foreach {
      case (periods, i) =>
        val x = i * colWidth + i * ColMargin
        val dowY = TopMargin - 3

        def createTextDiv(text: String, x: Int, y: Int, color: String = "black") = {
          val td = document.createElement("div")
          td.setAttribute("class", "period")
          td.setAttribute("style", s"top: ${y}px; left: ${x}px; font-size: .5em; border: none; color: $color")
          td.appendChild(document.createTextNode(text))
          td
        }

        drawing.appendChild(createTextDiv(days(i), x + XPad, dowY - 8))

        periods.foreach(p => {
          val yStart = yFromMs(p.startMs)
          val yEnd = yFromMs(p.endMs)
          val periodDiv = document.createElement("div")
          periodDiv.setAttribute("class", "period")
          periodDiv.setAttribute("style", s"top: ${yStart}px; height: ${yEnd - yStart}px; left: ${x + XPad}px; width: ${colWidth}px; background-color: ${fills(p.num - 1)}")
          drawing.appendChild(periodDiv)

          val labelX = x + XPad + 2
          val textColor = texts(p.num - 1)
          if (! labeledStartTime.contains(p.startMs)) {
            drawing.appendChild(createTextDiv(p.formattedStart, labelX, yStart, color = textColor))
            labeledStartTime += p.startMs
          }
          if (! labeledEndTime.contains(p.endMs)) {
            drawing.appendChild(createTextDiv(p.formattedEnd, labelX, yEnd - 13, color = textColor))
            labeledEndTime += p.endMs
          }
        })
    }
  }
}
