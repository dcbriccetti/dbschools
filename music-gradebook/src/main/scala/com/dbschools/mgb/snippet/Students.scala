package com.dbschools.mgb
package snippet

import scala.xml.{NodeSeq, Text}
import org.squeryl.PrimitiveTypeMode._
import net.liftweb._
import util._
import Helpers._
import schema.AppSchema

class Students {

  def render = {
    <table>
      <thead>
        <tr>
          <th>Graduation Year</th>
          <th>Name</th>
          <th>School Year</th>
          <th>Group</th>
        </tr>
      </thead>
      <tbody>
        {
        import AppSchema._
        from (musicians, groups, musicianGroups)((m, g, mg) =>
          where(m.musician_id === mg.musician_id and mg.group_id === g.group_id)
          select((m, g, mg))
          orderBy(mg.school_year, m.last_name, m.first_name, g.name)
        ).map(r =>
        <tr>
          <td>
            {r._1.graduation_year}
          </td>
          <td>
            {r._1.last_name + ", " + r._1.first_name}
          </td>
          <td>
            {r._3.school_year}
          </td>
          <td>
            {r._2.name}
          </td>
        </tr>
      )}
      </tbody>
    </table>
  }
}
