package com.dbschools.mgb
package snippet

import com.dbschools.mgb.model.Periods.Period
import net.liftweb.http.js.JsCmds.Script
import net.liftweb.http.js.JE.JsRaw
import net.liftweb.http.js.JsCmds._
import net.liftweb.util.Props

class Periods {
  def js = Script(Periods.js)
}

object Periods {
  def js =
    model.Periods.periodWithin() match {
      case p: Period =>
        val warnBellMins = Props.getInt("warnBellMins") getOrElse 0
        JsRaw(s"Periods.warnBellMins = $warnBellMins") &
        JsRaw(s"Periods.startTime = ${p.startMs}") &
        JsRaw(s"Periods.endTime = ${p.endMs}").cmd
      case _ =>
        Noop
    }
}
