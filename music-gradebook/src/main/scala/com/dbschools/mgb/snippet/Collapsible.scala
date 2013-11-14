package com.dbschools.mgb.snippet

import net.liftweb._
import http._
import http.js.JE.JsRaw
import js.JsCmds._

/** Hooks into collapse show and hide events on the Student Details Page */
trait Collapsible {
  def collapseMonitorJs(expanded: Array[Boolean]) = {
    def onEvent(n: Int, show: Boolean) = SHtml.onEvent(_ => expanded(n) = show).toString()

    Script(JsRaw((0 to 2).map(n => s"""
      $$(function() {
        $$("#collapse$n").on("show.bs.collapse", function() {
          ${onEvent(n, show = true)}
        });
        $$("#collapse$n").on("hide.bs.collapse", function() {
          ${onEvent(n, show = false)}
        });
      });
    """).reduce(_ + _)
    ))
  }
}
