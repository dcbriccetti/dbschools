package com.dbschools.mgb
package snippet

import scala.xml.Text
import scalaz._
import Scalaz._
import org.squeryl.PrimitiveTypeMode._
import org.scala_tools.time.Imports._
import net.liftweb._
import util._
import Helpers._
import net.liftweb.http.SHtml
import bootstrap.liftweb.Actors
import com.dbschools.mgb.comet.{TestingMusician, TestMusician}
import com.dbschools.mgb.schema.AppSchema

class TestSched {
  def render =
    "#student *" #> comet.testing.scheduledMusicians.toSeq.sortBy(_.sortOrder).map(sm => {
      val userName = ~AppSchema.users.where(_.login === Authenticator.userName.get).headOption.map(_.last_name)
      val m = sm.musician
      //todo move HTML to templates
      <tr id={m.id.toString}>
        <td class="h3">{
          SHtml.link(Students.urlToDetails(m), () => Actors.testScheduler ! TestMusician(TestingMusician(m, userName, DateTime.now)),
            Text(m.first_name.get + " " + m.last_name))
        } </td>
        <td class="h3">
          {sm.nextPieceName}
        </td>
      </tr>
    }) &
    "#test *" #> comet.testing.testingMusicians.toSeq.sortBy(_.time.millis).map(tm => {
      val m = tm.musician
      val tmf = DateTimeFormat.forStyle("-M")
      <tr id={"t" + m.id.toString}>
        <td class="h4">{
          Text(m.first_name.get + " " + m.last_name)
        } </td>
        <td class="h4">{
          Text(tm.testerName)
        } </td>
        <td class="h4">{
          Text(tmf.print(tm.time))
        } </td>
      </tr>
    })
}
