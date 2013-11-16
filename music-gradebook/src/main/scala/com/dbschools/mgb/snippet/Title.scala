package com.dbschools.mgb.snippet

import scala.xml.{Text, NodeSeq}
import net.liftweb.http.S
import net.liftweb.util.Helpers._

/** Adds the current location to the title in the element */
class Title {
  def render = {
    val opLocation =
      (for {
        request <- S.request
        loc     <- request.location
      } yield loc.title).toOption
    
    "* *+" #> (opLocation map(location => Text("—") ++ location) getOrElse NodeSeq.Empty)
  }
}
