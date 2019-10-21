package com.dbschools.perclock

import scala.scalajs.js
import scala.scalajs.js.{RegExp, URIUtils}
import org.scalajs.dom
import org.scalajs.jquery.jQuery
import dom.document
import scalatags.JsDom.all._
import model.{DefaultPeriods, NotInPeriod, Period, Periods, TeacherSettings, TimeClass}
import org.scalajs.dom.raw.Element

object ClassPeriods extends js.JSApp {

  def byId(id: String): Element = document.getElementById(id)

  def main(): Unit = {
    import TeacherSettings._
    val params = parseParams
    DefaultPeriods.dump()

    val (school, periods, scheduleDefPeriodNames) = params.get("scheduleDef") match {
      case Some(scheduleDef) =>
        val lines = scheduleDef.split('\n').toVector
        val schoolAndNames = lines(0).split('\t')
        val school = schoolAndNames(0)
        val periodNames = schoolAndNames(1).split('|').toSeq

        val customWeek = lines.drop(1).map {day =>
          val pattern = new RegExp("(\\S+) (\\d+):(\\d+)-(\\d+):(\\d+)")
          val pts = day.split("\t").toVector
          for {
            pt <- pts
            x = pattern.exec(pt)
            if x != null
            code = x(1).get.charAt(0).toInt - 'a'.toInt + 1
          } yield Period(code, x(2).get, x(3).get, x(4).get, x(5).get)
        }
        println(customWeek)
        (school, new Periods() {
          override val week: Vector[Vector[Period]] = customWeek
        }, periodNames)
      case None =>
        ("Stanley Middle School", DefaultPeriods, 1 to 7 map(_.toString))
    }

    val NumPeriods = periods.week.map(_.size).max

    val maybeParamsPeriodNames: Option[Seq[String]] = params.get("periodNames").map(_.split('|'))
    val maybeTeacherSettings: Option[(Seq[String], Seq[String])] = for {
      teacher <- params.get("teacher")
      settings <- PredefinedSettings.get(teacher)
    } yield settings
    val fillColors: Seq[String] = maybeTeacherSettings.map(_._2).getOrElse(defaultFillColors(NumPeriods))
    byId("schoolName").innerHTML = school

    if (maybeParamsPeriodNames.isEmpty) {
      jQuery("#namesFormDiv").show()
      jQuery("#defineSchoolDiv").show()
    }

    val periodNames: Seq[String] = maybeParamsPeriodNames.getOrElse(
      maybeTeacherSettings.map(_._1).getOrElse(scheduleDefPeriodNames)).
      map(URIUtils.decodeURIComponent).padTo(NumPeriods, "").take(NumPeriods)

    val timeRemaining = byId("timeRemaining")
    val period        = byId("period")
    val periodName    = byId("periodName")
    val periodRange   = byId("periodRange")
    val periodLength  = byId("periodLength")
    val progressMins  = byId("progressMins")

    var lastPeriodWithin: TimeClass = NotInPeriod
    var lastDowToday = -1

    def update(): Unit = {
      val dowToday = periods.dowToday match {
        case d if d < 5 || true /* for testing */ => d
        case _          => 0 // Treat weekends as Monday
      }
      val periodWithin = periods.periodWithin
      if (dowToday != lastDowToday || periodWithin != lastPeriodWithin) {
        jQuery(".currentCell").removeClass("currentCell")
        periodWithin match {
          case p: Period =>
            jQuery(s"#cell-$dowToday-${p.number}").addClass("currentCell")
          case _ =>
        }
        lastDowToday = dowToday
        lastPeriodWithin = periodWithin
      }

      periodWithin match {
        case p: Period =>
          period.setAttribute("class", "")
          val secs = Math.floor((p.endMs - periods.nowMs) / 1000)
          timeRemaining.setAttribute("max", p.totalSecs.toString)
          timeRemaining.setAttribute("value", secs.toString)
          periodName.innerHTML = periodNames(p.number - 1)
          periodRange .innerHTML = p.formattedRange
          periodLength.innerHTML = (p.totalSecs.toInt / 60).toString
          val remaining = p.formattedTimeRemaining
          progressMins.innerHTML = remaining
          dom.document.title = remaining
        case _ =>
          period.setAttribute("class", "hide")
          timeRemaining.setAttribute("value", "0")
          periodName.innerHTML = ""
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

    val firstStartMs = periods.week.map(_.map(_.startMs).min).min
    val lastEndMs    = periods.week.map(_.map(_.endMs  ).max).max
    val totalMs = lastEndMs - firstStartMs
    def yFromMs(ms: Double): Int = (TopMargin + ((ms - firstStartMs) / totalMs) * (Height - TopMargin)).toInt
    def mkSeq(line: String): Seq[String] = line.split(" +").toSeq
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

    periods.week.zipWithIndex.foreach {
      case (periods, iDay) =>
        val colX = iDay * (colWidth + ColMargin)

        drawing.appendChild(createTextDiv(days(iDay), colX + XPad, 0, "dayTitle"))

        periods.zipWithIndex.foreach {
          case (per, i) => {
            val yStart = yFromMs(per.startMs)
            val yEnd   = yFromMs(per.endMs)
            val divHeight = yEnd - yStart
            val textColor = "black"
            val divStyle = s"top: ${yStart}px; height: ${divHeight}px; left: ${colX + XPad}px; width: ${colWidth}px; " +
              s"color: $textColor; background-color: ${fillColors(per.number - 1)};"
            val divHtml = div(id := s"cell-$iDay-${per.number}", cls := "period", style := divStyle)(
              p(periodNames(per.number - 1))
            ).render
            jQuery("#drawing").append(divHtml)

            val labelX = colX + XPad + 2

            if (! labeledStartTimes.contains(per.startMs)) {
              drawing.appendChild(createTextDiv(per.formattedStart, labelX, yStart, "perTime", color = textColor))
              labeledStartTimes += per.startMs
            }
            val abutsNext = i < periods.size - 1 && per.endMs == periods(i + 1).startMs
            if (! labeledEndTimes.contains(per.endMs) && ! abutsNext) {
              drawing.appendChild(createTextDiv(per.formattedEnd, labelX, yEnd - 15, "perTime", color = textColor))
              labeledEndTimes += per.endMs
            }
          }
        }
    }
    update()
    dom.setInterval(update _, 1000) // todo try to return close to the 0 ms after the next second (wait < 1000 ms)
  }

  private def parseParams: Map[String, String] = {
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
