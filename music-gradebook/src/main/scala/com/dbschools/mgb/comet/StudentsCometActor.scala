package com.dbschools.mgb
package comet

import scala.xml.Text
import net.liftweb.http.{CometListener, CometActor}
import net.liftweb.http.js.JsCmds.SetHtml
import snippet.Students

class StudentsCometActor extends CometActor with CometListener {
  import StudentsCometActorMessages._
  import CommonCometActorMessages._

  def registerWith = StudentsCometDispatcher

  override def lowPriority = {

    case QueueSize(queueSize) =>
      partialUpdate(SetHtml("count", Text(queueSize.toString)) &
        Students.adjustButtons)

    case Start =>
  }

  def render = "#count *" #> model.testingState.enqueuedMusicians.size.toString
}

object StudentsCometActorMessages {
  case class QueueSize(queueSize: Int)
}

object StudentsCometDispatcher extends CommonCometDispatcher
