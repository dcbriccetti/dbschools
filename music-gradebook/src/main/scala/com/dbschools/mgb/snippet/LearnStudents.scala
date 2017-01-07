package com.dbschools.mgb
package snippet

import java.sql.Timestamp
import scala.xml.Text
import scala.collection.mutable.{Queue=>MQueue}
import scalaz._, Scalaz._
import org.squeryl.PrimitiveTypeMode._
import org.scala_tools.time.Imports._
import net.liftweb.http.js.JsCmds._
import net.liftweb._
import http._
import net.liftweb.util.{ClearNodes, Helpers}
import Helpers._
import schema.{LearnState, AppSchema}
import model.{SchoolYears, GroupAssignment}

class LearnStudents extends Photos {
  private val userId = Authenticator.opLoggedInUser.get.id
  val NumNew = 10

  case class State(groupAssignments: Iterable[GroupAssignment]) {
    private val queue = new MQueue[GroupAssignment]
    queue ++= groupAssignments
    var opCurrent = dequeue()
    var front = true
    def next(): Unit = opCurrent = dequeue()
    def requeueCurrent(): Unit = opCurrent.foreach(ga => queue.enqueue(ga))

    def saveDue(due: Timestamp): Unit = opCurrent.foreach(ga => AppSchema.learnStates.where(ls =>
      ls.user_id === userId and ls.musician_id === ga.musician.id).headOption match {
        case Some(ls) => AppSchema.learnStates.update(ls.copy(due = due))
        case _        => AppSchema.learnStates.insert(LearnState(0, userId, ga.musician.id, due))
      }
    )
    
    private def dequeue() = if (queue.isEmpty) None else Some(queue.dequeue())
  }

  private val queueItems = {
    val groupAssignments = model.GroupAssignments.assignments.filter(ga =>
      pictureFilename(ga.musician.permStudentId.get).nonEmpty).toVector
    val musicianIds = groupAssignments.map(_.musician.id)
    val learnStates = if (musicianIds.isEmpty) Vector[LearnState]() else
      AppSchema.learnStates.where(ls =>
      ls.user_id === userId and (ls.musician_id in musicianIds)).toVector
    val dueMusIds = learnStates.withFilter(_.due.getTime <= DateTime.now.getMillis).map(_.musician_id).toSet
    val mids = learnStates.map(_.musician_id).toSet
    val (currentGas, newGas) = groupAssignments.partition(ga => mids contains ga.musician.id)
    val dueCurrentGas = currentGas.filter(ga => dueMusIds contains ga.musician.id)
    dueCurrentGas ++ newGas.take(NumNew)
  }
  private val state = State(queueItems)

  case class GradeButton(title: String, score: Int)

  val buttons = {
    val min = 60
    val day = min * 60 * 24
    val b = GradeButton.apply _
    Seq(
      b("0 Mins",   0        ),
      b("10 Mins",  min * 10 ),
      b("1 Day",    day      ),
      b("3 Days",   day * 3  ),
      b("1 Week",   day * 7  ),
      b("30 Days",  day * 30 ),
      b("1 Year",   day * 365)
    )
  }

  def schedule(delaySeconds: Int) = {
    if (delaySeconds == 0) state.requeueCurrent()
    state.saveDue(SchoolYears.toTs(DateTime.now.plusSeconds(delaySeconds)))
    state.next()
    state.opCurrent.map(groupAssignment => {
      state.front = true
      SetHtml("name", Text(info(groupAssignment))) &
      SetHtml("picture", img(groupAssignment.musician.permStudentId.get)) &
      JsHideId("back") & JsShowId("showBackButton")
    }) | JsHideId("running") & JsShowId("finished")
  }

  def showBack = {
    state.front = false
    JsShowId("back") & JsHideId("showBackButton")
  }

  val buttonClass = "class" -> "btn btn-primary"
  val buttonStyle = "style" -> "margin-right: .5em;"

  val ajaxButtons = buttons.zipWithIndex.map(b => {
    SHtml.ajaxButton(b._1.title, () => schedule(b._1.score), buttonClass, buttonStyle,
      "id" -> s"score${b._2 + 1}", "style" -> "width: 5em; margin-right: .5em")
  })

  private def info(ga: GroupAssignment) = {
    val m = ga.musician
    s"${m.nameNickLast}, Grade ${SchoolYears.graduationYearAsGrade(m.graduation_year.get)}, ${ga.instrument.name.get}"
  }

  def render = {
    state.opCurrent.map(groupAssignment => {
      "#name *"         #> info(groupAssignment) &
      "#picture *"      #> img(groupAssignment.musician.permStudentId.get) &
      "#buttons"        #> ajaxButtons &
      "#showBackButton" #> SHtml.ajaxButton("Show Name", () => showBack, buttonClass, buttonStyle)
    }) getOrElse ClearNodes
  }
}
