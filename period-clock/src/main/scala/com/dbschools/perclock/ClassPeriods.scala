package com.dbschools.perclock

import scala.scalajs.js
import scala.scalajs.js.URIUtils
import org.scalajs.dom
import org.scalajs.jquery.jQuery
import dom.document
import model.{TimeClass, NotInPeriod, Period, Periods, TeacherSettings}

object ClassPeriods extends js.JSApp {

  def byId(id: String) = document.getElementById(id)

  def main(): Unit = {
    import TeacherSettings._
    val params = parseParams

    val (periodNamesString, fillColors, textColors) =
      params.get("periodNames") match {
        case Some(names) => (names, DefaultFillColors, DefaultTextColors)
        case None =>
          (for {
            teacher  <- params.get("teacher")
            settings <- PredefinedSettings.get(teacher)
          } yield settings) getOrElse DefaultSettings
      }
    
    if (periodNamesString == DefaultPeriodNames && false /* todo wait until URL decoding is working with + */) {
      jQuery("#namesFormDiv").show()
    }

    val NumPeriods = 7
    val periodNames = periodNamesString.split(" *\\| *").map(URIUtils.decodeURIComponent).toSeq.
      padTo(NumPeriods, "").take(NumPeriods)

    val timeRemaining = byId("timeRemaining")
    val period        = byId("period")
    val periodNumber  = byId("periodNumber")
    val periodRange   = byId("periodRange")
    val periodLength  = byId("periodLength")
    val progressMins  = byId("progressMins")

    var lastPeriodWithin: TimeClass = NotInPeriod
    var lastDowToday = -1

    def update(): Unit = {
      val dowToday = Periods.dowToday match {
        case d if d < 5 || true /* for testing */ => d
        case _          => 0 // Treat weekends as Monday
      }
      val periodWithin = Periods.periodWithin
      if (dowToday != lastDowToday || periodWithin != lastPeriodWithin) {
        jQuery(".currentCell").removeClass("currentCell")
        periodWithin match {
          case p: Period =>
            jQuery(s"#cell-$dowToday-${p.num}").addClass("currentCell")
          case _ =>
        }
        lastDowToday = dowToday
        lastPeriodWithin = periodWithin
      }

      periodWithin match {
        case p: Period =>
          period.setAttribute("class", "")
          val secs = Math.floor((p.endMs - Periods.nowMs) / 1000)
          timeRemaining.setAttribute("max", p.totalSecs.toString)
          timeRemaining.setAttribute("value", secs.toString)
          periodNumber.innerHTML = p.num.toString
          periodRange .innerHTML = p.formattedRange
          periodLength.innerHTML = (p.totalSecs.toInt / 60).toString
          val remaining = p.formattedTimeRemaining
          progressMins.innerHTML = remaining
          dom.document.title = remaining
        case _ =>
          period.setAttribute("class", "hide")
          timeRemaining.setAttribute("value", "0")
          periodNumber.innerHTML = ""
          periodRange .innerHTML = ""
          periodLength.innerHTML = ""
          progressMins.innerHTML = ""
          dom.document.title = "Between Periods"
      }
    }

    val drawing = byId("drawing")
    val XPad = 15 // todo Find how to get main’s padding
    val Height = 350
    val TopMargin = 20
    val ColMargin = 8

    val firstStartMs = Periods.week.map(_.map(_.startMs).min).min
    val lastEndMs    = Periods.week.map(_.map(_.endMs  ).max).max
    val totalMs = lastEndMs - firstStartMs
    def yFromMs(ms: Double) = (TopMargin + ((ms - firstStartMs) / totalMs) * (Height - TopMargin)).toInt
    def mkSeq(line: String) = line.split(" +").toSeq
    val fills = mkSeq(fillColors)
    val texts = mkSeq(textColors)
    val days  = mkSeq("Monday Tuesday Wednesday Thursday Friday")
    val colWidth = (drawing.clientWidth - ColMargin * (5 - 1)) / 5
    var labeledStartTimes = Set[Double]()
    var labeledEndTimes   = Set[Double]()

    def createTextDiv(text: String, x: Int, y: Int, className: String, color: String = "black") = {
      val td = document.createElement("div")
      td.setAttribute("class", className)
      td.setAttribute("style", s"top: ${y}px; left: ${x}px; color: $color")
      td.appendChild(document.createTextNode(text))
      td
    }

    Periods.week.zipWithIndex.foreach {
      case (periods, iDay) =>
        val colX = iDay * (colWidth + ColMargin)

        drawing.appendChild(createTextDiv(days(iDay), colX + XPad, 0, "dayTitle"))

        periods.foreach(p => {
          val yStart = yFromMs(p.startMs)
          val yEnd   = yFromMs(p.endMs)
          val divHeight = yEnd - yStart
          val textColor = texts(p.num - 1)
          val style = s"top: ${yStart}px; height: ${divHeight}px; left: ${colX + XPad}px; width: ${colWidth}px; " +
            s"color: $textColor; background-color: ${fills(p.num - 1)};"
          val divHtml =
            s"""
               |<div id="cell-$iDay-${p.num}" class="period" style="$style">
               |    <p>${periodNames(p.num - 1)}</p>
               |</div>
             """.stripMargin
          jQuery("#drawing").append(divHtml)

          val labelX = colX + XPad + 2

          if (! labeledStartTimes.contains(p.startMs)) {
            drawing.appendChild(createTextDiv(p.formattedStart, labelX, yStart, "perTime", color = textColor))
            labeledStartTimes += p.startMs
          }
          if (! labeledEndTimes.contains(p.endMs)) {
            drawing.appendChild(createTextDiv(p.formattedEnd, labelX, yEnd - 13, "perTime", color = textColor))
            labeledEndTimes += p.endMs
          }
        })
    }
    update()
    dom.setInterval(update _, 1000) // todo try to return close to the 0 ms after the next second (wait < 1000 ms)
  }

  def parseParams = {
    val s = dom.window.location.search
    if (s.length > 1 && s(0) == '?') {
      val pairs = s.substring(1).split('&')
      (for {
        param <- pairs
        keyVal = param.split('=') if keyVal.length == 2
      } yield keyVal(0) -> URIUtils.decodeURIComponent(keyVal(1))).toMap
    } else Map[String, String]()
  }
}
