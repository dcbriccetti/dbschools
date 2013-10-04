package com.dbschools.mgb.snippet

import scalaz._
import Scalaz._
import net.liftweb._
import common.Loggable
import util._
import http._
import js._
import js.JsCmds._
import Helpers._
import com.dbschools.mgb.model.Combiner

class Problems extends Loggable {
  def combine = {
    var opOldId = none[Int]
    var opCurrentId = none[Int]

    def process(): JsCmd = {
      opOldId tuple opCurrentId foreach {
        case (o, c) =>
          logger.warn(s"process $o and $c")
          Combiner.combine(o, c)
      }
      Noop
    }
    def intText(fn: Int => Unit) = SHtml.text("", asInt(_).map(id => fn(id)))

    "#oldId"      #> intText(id => opOldId = Some(id)) &
    "#currentId"  #> (intText(id => opCurrentId = Some(id)) ++ SHtml.hidden(process))
  }
}
