package com.dbschools.mgb
package snippet

import scala.xml.Text
import net.liftweb._
import util._
import Helpers._
import net.liftweb.http.SHtml
import com.dbschools.mgb.comet.RemoveMusician
import bootstrap.liftweb.Actors

class TestSched {
  def render =
    "#student *" #> comet.testing.scheduledMusicians.toSeq.sortBy(_.sortOrder).map(sm => {
      val m = sm.musician
      <tr id={m.id.toString}>
        <td class="h3">{
          SHtml.link(Students.urlToDetails(m), () => Actors.testScheduler ! RemoveMusician(m),
            Text(m.first_name.get + " " + m.last_name))
        } </td>
        <td class="h3">
          {sm.nextPieceName}
        </td>
      </tr>
    })
}
