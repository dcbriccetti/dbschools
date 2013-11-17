package com.dbschools.mgb.snippet

import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.JE.JsRaw
import net.liftweb.http.js.JsCmd
import net.liftweb.http.Templates
import com.dbschools.mgb.model
import model.BoxOpener._
import net.liftweb.util.Helpers
import Helpers._

object LiftExtensions {
  def JsShowIdIf(what: String, condition: Boolean) = if (condition) JsShowId(what) else JsHideId(what)
  def JsPropIf    (selector: String, prop: String, condition: Boolean) =  JsJqFn2(selector, "prop", prop, condition)
  def JsEnableIf  (selector: String, condition: Boolean) =  JsPropIf(selector, "disabled", ! condition)
  def JsCheckIf   (selector: String, condition: Boolean) =  JsPropIf(selector, "checked", condition)
  def JsJqHtml    [A](selector: String, value: A): JsCmd = JsJqFn1(selector, "html", value)
  def JsJqVal     [A](selector: String, value: A): JsCmd = JsJqFn1(selector, "val", value)
  def JsJqPrepend [A](selector: String, value: A): JsCmd = JsJqFn1(selector, "prepend", value)
  def JsJqRemove(selector: String): JsCmd = JsJqFn0(selector, "remove")
  def JsJqHilite(selector: String, ms: Int = 3000): JsCmd = JsRaw(s"jQuery('$selector').effect('highlight', {}, $ms);").cmd

  def elemFromTemplate(templateName: String, selector: String) = {
    val template = Templates(templateName :: Nil).open
    val cssGetTr = s"$selector ^^" #> ""
    cssGetTr(template)
  }

  def displayNoneIf(condition: Boolean): Helpers.TheStrBindParam =
    "style" -> (if (condition) "display: none;" else "")

  def disableIf(b: Boolean) = if (b) "disabled" -> "disabled" else "" -> ""

  private def JsJqFn0(selector: String, fn: String): JsCmd = JsRaw(s"jQuery('$selector').$fn()").cmd
  private def JsJqFn1[A](selector: String, fn: String, value: A): JsCmd = JsRaw(s"jQuery('$selector').$fn('${value.toString}')").cmd
  private def JsJqFn2[A, B](selector: String, fn: String, value1: A, value2: B): JsCmd =
    JsRaw(s"jQuery('$selector').$fn('${value1.toString}', ${value2.toString})").cmd
}
