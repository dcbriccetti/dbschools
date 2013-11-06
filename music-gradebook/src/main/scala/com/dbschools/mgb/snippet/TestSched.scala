package com.dbschools.mgb
package snippet

import net.liftweb._
import util._
import Helpers._
import net.liftweb.http.SHtml
import scala.xml.Text
import com.dbschools.mgb.comet.{RemoveMusician, TestCometDispatcher}

class TestSched {
  def render =
    "#student *" #> comet.testing.scheduledMusicians.toSeq.sortBy(_.sortOrder).map(sm => {
      val m = sm.musician
      SHtml.link(Students.urlToDetails(m), () => {
        TestCometDispatcher ! RemoveMusician(m)
      }, Text(m.first_name.get + " " + m.last_name + ", " + sm.nextPieceName), "id" -> m.id.toString)
    })
}

