package com.dbschools.perclock

import com.dbschools.perclock.model.{Period, Periods}
import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.Date
import scala.scalajs.js.timers._

object ClassPeriods extends js.JSApp {

  def main(): Unit = {
    def gbid(id: String) = dom.document.getElementById(id)
    val timeRemaining   = gbid("timeRemaining")
    val period          = gbid("period")
    val periodNumber    = gbid("periodNumber")
    val periodRange     = gbid("periodRange")
    val periodLength    = gbid("periodLength")
    val progressMins    = gbid("progressMins")

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

    setInterval(1000) {
      update()
    }
  }
}
