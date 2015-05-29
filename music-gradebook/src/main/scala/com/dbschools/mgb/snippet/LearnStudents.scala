package com.dbschools.mgb
package snippet

import scala.xml.Text
import scalaz._, Scalaz._
import net.liftweb.http.js.JsCmds._
import net.liftweb._
import http._
import net.liftweb.util.{ClearNodes, Helpers}
import Helpers._
import model.{Terms, GroupAssignment}

class LearnStudents extends Photos {
  val queue = new scala.collection.mutable.Queue[GroupAssignment]
  queue ++= model.GroupAssignments.assignments
  def dequeue() = if (queue.isEmpty) None else Some(queue.dequeue())
  var opThisGa = dequeue()
  var front = true
  val RequeueScores = Set(1)

  case class GradeButton(title: String, score: Int)

  val buttons = Seq(
    GradeButton("Missed", 1),
    GradeButton("Hard",   2),
    GradeButton("Good",   3),
    GradeButton("Easy",   4)
  )

  def push(score: Int) = {
    if (RequeueScores contains score) opThisGa.foreach(ga => queue.enqueue(ga))
    opThisGa = dequeue()
    opThisGa.map(thisGa => {
      front = true
      SetHtml("name", Text(info(thisGa))) &
      SetHtml("picture", img(thisGa.musician.permStudentId.get)) &
      JsHideId("back") & JsShowId("showBackButton")
    }) | JsHideId("running") & JsShowId("finished")
  }

  def showBack = {
    front = false
    JsShowId("back") & JsHideId("showBackButton")
  }

  val buttonClass = "class" -> "btn btn-primary btn-lg"
  val buttonStyle = "style" -> "margin-right: .5em;"

  val ajaxButtons = buttons.map(b => {
    SHtml.ajaxButton(b.title, () => push(b.score), buttonClass, buttonStyle, "id" -> s"score${b.score}")
  })

  private def info(ga: GroupAssignment) = {
    val m = ga.musician
    s"${m.nameNickLast}, Grade ${Terms.graduationYearAsGrade(m.graduation_year.get)}, ${ga.instrument.name.get}"
  }

  def render = {
    opThisGa.map(thisGa => {
      "#name *" #> info(thisGa) &
      "#picture *" #> img(thisGa.musician.permStudentId.get) &
      "#buttons" #> ajaxButtons &
      "#showBackButton" #> SHtml.ajaxButton("Show Name", () => showBack, buttonClass, buttonStyle)
    }) getOrElse ClearNodes
  }
}
