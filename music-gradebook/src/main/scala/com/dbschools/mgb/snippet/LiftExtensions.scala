package com.dbschools.mgb.snippet

import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.JE.JsRaw
import net.liftweb.http.js.JsCmd

object LiftExtensions {
  def JsShowIdIf(what: String, condition: Boolean) = if (condition) JsShowId(what) else JsHideId(what)
  def JsJqVal(selector: String, value: String): JsCmd = JsRaw(s"""jQuery("$selector").val('$value')""").cmd
  def JsJqVal(selector: String, value: Int): JsCmd = JsJqVal(selector, value.toString)
}
