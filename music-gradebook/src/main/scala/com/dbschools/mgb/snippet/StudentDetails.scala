package com.dbschools.mgb
package snippet

import scala.xml.Text
import net.liftweb._
import util._
import http._
import js.JsCmds._
import Helpers._
import net.liftweb.http.js.JsCmds.SetHtml
import model.{LastPassFinder, SelectedMusician, Terms}
import schema.{AppSchema, Musician}

class StudentDetails extends Collapsible with SelectedMusician {
  private object svCollapsibleShowing extends SessionVar[Array[Boolean]](Array(false, false, false))
  private val collapsibleShowing = svCollapsibleShowing.is

  def render = {

    def changeName(musician: Musician, field: record.field.StringField[Musician], id: String)(t: String) = {
      field(t)
      if (musician.validate.isEmpty) {
        AppSchema.musicians.update(musician)
        SetHtml(id, Text(t))
      } else Noop
    }

    def changeStuId(musician: Musician)(t: String) =
      Helpers.asInt(t).toOption.map(newId => {
        musician.student_id(newId)
        AppSchema.musicians.update(musician)
        SetHtml("stuId", Text(newId.toString))
      }) getOrElse Noop

    val lastPassFinder = new LastPassFinder
    opMusician.map(m => {
      val collapseSels = (0 to 2).map(n => s"#collapse$n [class+]" #> (if (collapsibleShowing(n)) "in" else ""))

      collapseSels.reduce(_ & _) &
      ".lastName *"       #> SHtml.swappable(<span id="lastName">{m.last_name.get}</span>,
                                SHtml.ajaxText(m.last_name.get,
                                changeName(m, m.last_name, "lastName"))) &
      ".firstName *"      #> SHtml.swappable(<span id="firstName">{m.first_name.get}</span>,
                                SHtml.ajaxText(m.first_name.get,
                                changeName(m, m.first_name, "firstName"))) &
      ".grade"            #> Terms.graduationYearAsGrade(m.graduation_year.get) &
      ".stuId"            #> SHtml.swappable(<span id="stuId">{m.student_id.toString()}</span>,
                                SHtml.ajaxText(m.student_id.toString(), changeStuId(m))) &
      "#lastPiece *"      #> StudentDetails.lastPiece(lastPassFinder, m.id)
    }) getOrElse PassThru
  }

  def js = collapseMonitorJs(collapsibleShowing)
}

object StudentDetails {
  def lastPiece(lastPassFinder: LastPassFinder, musicianId: Int): String = {
    lastPassFinder.lastPassed(Some(musicianId)).mkString(", ")
  }
}
