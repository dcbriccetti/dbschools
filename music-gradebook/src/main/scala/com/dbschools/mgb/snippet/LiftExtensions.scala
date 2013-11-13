package com.dbschools.mgb.snippet

import net.liftweb.http.js.JsCmds._

object LiftExtensions {
  def JsShowIdIf(what: String, condition: Boolean) = if (condition) JsShowId(what) else JsHideId(what)
}
