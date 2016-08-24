package com.dbschools.perclock

import scala.scalajs.js
import scala.scalajs.js.URIUtils
import org.scalajs.dom
import org.scalajs.jquery.jQuery
import dom.document
import scalatags.JsDom.all._
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
    
    if (periodNamesString == DefaultPeriodNames) {
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
    val Height = 425
    val TopMargin = 26
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

        periods.foreach(per => {
          val yStart = yFromMs(per.startMs)
          val yEnd   = yFromMs(per.endMs)
          val divHeight = yEnd - yStart
          val textColor = texts(per.num - 1)
          val divStyle = s"top: ${yStart}px; height: ${divHeight}px; left: ${colX + XPad}px; width: ${colWidth}px; " +
            s"color: $textColor; background-color: ${fills(per.num - 1)};"
          val divHtml = div(id := s"cell-$iDay-${per.num}", cls := "period", style := divStyle)(
            p(periodNames(per.num - 1))
          ).render
          jQuery("#drawing").append(divHtml)

          val labelX = colX + XPad + 2

          if (! labeledStartTimes.contains(per.startMs)) {
            drawing.appendChild(createTextDiv(per.formattedStart, labelX, yStart, "perTime", color = textColor))
            labeledStartTimes += per.startMs
          }
          if (! labeledEndTimes.contains(per.endMs)) {
            drawing.appendChild(createTextDiv(per.formattedEnd, labelX, yEnd - 15, "perTime", color = textColor))
            labeledEndTimes += per.endMs
          }
        })
    }
    update()
    dom.setInterval(update _, 1000) // todo try to return close to the 0 ms after the next second (wait < 1000 ms)
  }

  def parseParams = {
    val search = dom.window.location.search.replace("+", "%20")
    println(search)
    if (search.length > 1 && search(0) == '?') {
      val pairs = search.substring(1).split('&')
      (for {
        param <- pairs
        keyVal = param.split('=') if keyVal.length == 2
      } yield keyVal(0) -> URIUtils.decodeURIComponent(keyVal(1))).toMap
    } else Map[String, String]()
  }
}
