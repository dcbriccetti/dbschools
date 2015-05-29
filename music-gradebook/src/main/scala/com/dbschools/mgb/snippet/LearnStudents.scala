package com.dbschools.mgb
package snippet

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
import model.{Terms, GroupAssignment}

class LearnStudents extends Photos {
  private val userId = Authenticator.opLoggedInUser.get.id
  val NumNew = 10
  val InitialEase = 2.5f

  case class State(gas: Iterable[GroupAssignment]) {
    val queue = new MQueue[GroupAssignment]
    queue ++= gas
    var opThisGa = dequeue()
    var front = true
    private def dequeue() = if (queue.isEmpty) None else Some(queue.dequeue())
    def next(): Unit = opThisGa = dequeue()
    def requeueCurrent(): Unit = opThisGa.foreach(ga => queue.enqueue(ga))
  }

  private val queueItems = {
    val groupAssignments = model.GroupAssignments.assignments.toVector
    val musicianIds = groupAssignments.map(_.musician.id)
    val learnStates = AppSchema.learnStates.where(ls =>
      ls.user_id === userId and (ls.musician_id in musicianIds)).toVector
    val dueMusIds = learnStates.withFilter(_.due.getTime <= DateTime.now.getMillis).map(_.musician_id).toSet
    val mids = learnStates.map(_.musician_id).toSet
    val (currentGas, newGas) = groupAssignments.partition(ga => mids contains ga.musician.id)
    val dueCurrentGas = currentGas.filter(ga => dueMusIds contains ga.musician.id)
    dueCurrentGas ++ newGas.take(NumNew)
  }
  private val state = State(queueItems)
  val RequeueScores = Set(1)

  case class GradeButton(title: String, score: Int)

  val buttons = Seq(
    GradeButton("Missed", 1),
    GradeButton("Hard",   2),
    GradeButton("Good",   3),
    GradeButton("Easy",   4)
  )

  def push(score: Int) = {
    if (RequeueScores contains score) state.requeueCurrent()
    def newEase(ease: Float, score: Int) = ease
    def newDue(ease: Float) = Terms.toTs(DateTime.now.plusMinutes(10))
    state.opThisGa.map(ga => {
      val opLs = AppSchema.learnStates.where(ls =>
        ls.user_id === userId and ls.musician_id === ga.musician.id).headOption
      opLs.map(ls => {
        val ne = newEase(ls.ease, score)
        AppSchema.learnStates.update(ls.copy(ease = ne, due = newDue(ne)))
        ls
      }) getOrElse {
        val ne = InitialEase
        AppSchema.learnStates.insert(LearnState(0, userId, ga.musician.id, ne, newDue(ne)))
      }
    })
    state.next()
    state.opThisGa.map(ga => {
      state.front = true
      SetHtml("name", Text(info(ga))) &
      SetHtml("picture", img(ga.musician.permStudentId.get)) &
      JsHideId("back") & JsShowId("showBackButton")
    }) | JsHideId("running") & JsShowId("finished")
  }

  def showBack = {
    state.front = false
    JsShowId("back") & JsHideId("showBackButton")
  }

  val buttonClass = "class" -> "btn btn-primary"
  val buttonStyle = "style" -> "margin-right: .5em;"

  val ajaxButtons = buttons.map(b => {
    SHtml.ajaxButton(b.title, () => push(b.score), buttonClass, buttonStyle, "id" -> s"score${b.score}")
  })

  private def info(ga: GroupAssignment) = {
    val m = ga.musician
    s"${m.nameNickLast}, Grade ${Terms.graduationYearAsGrade(m.graduation_year.get)}, ${ga.instrument.name.get}"
  }

  def render = {
    state.opThisGa.map(thisGa => {
      "#name *" #> info(thisGa) &
      "#picture *" #> img(thisGa.musician.permStudentId.get) &
      "#buttons" #> ajaxButtons &
      "#showBackButton" #> SHtml.ajaxButton("Show Name", () => showBack, buttonClass, buttonStyle)
    }) getOrElse ClearNodes
  }
}
