package com.dbschools.mgb.snippet

import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.JE.JsRaw
import net.liftweb.http.js.JsCmd

object LiftExtensions {
  def JsShowIdIf(what: String, condition: Boolean) = if (condition) JsShowId(what) else JsHideId(what)
  def JsJqFn      [A](selector: String, value: A, fn: String): JsCmd = JsRaw(s"jQuery('$selector').$fn('${value.toString}')").cmd
  def JsJqHtml    [A](selector: String, value: A): JsCmd = JsJqFn(selector, value, "html")
  def JsJqVal     [A](selector: String, value: A): JsCmd = JsJqFn(selector, value, "val")
  def JsJqPrepend [A](selector: String, value: A): JsCmd = JsJqFn(selector, value, "prepend")
  def JsJqHilite(selector: String, ms: Int = 3000): JsCmd = JsRaw(s"jQuery('$selector').effect('highlight', {}, $ms);").cmd
}
