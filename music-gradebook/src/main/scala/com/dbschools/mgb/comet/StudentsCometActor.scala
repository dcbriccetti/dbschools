package com.dbschools.mgb
package comet

import scala.xml.Text
import org.apache.log4j.Logger
import net.liftweb.actor.LiftActor
import net.liftweb.http.{ListenerManager, CometListener, CometActor}
import net.liftweb.http.js.JsCmds.SetHtml
import com.dbschools.mgb.snippet.Students

case class TestingUpdate(queue: Iterable[ScheduledMusician])

class StudentsCometActor extends CometActor with CometListener {
  private val log = Logger.getLogger(getClass)
  def registerWith = StudentsCometDispatcher

  override def lowPriority = {
    case TestingUpdate(queue) =>
      partialUpdate(SetHtml("count", Text(queue.size.toString)) & Students.showClearSchedule)
    case _ =>
  }

  def render = "#count *" #> comet.testing.scheduledMusicians.size.toString
}

object StudentsCometDispatcher extends LiftActor with ListenerManager {
  def createUpdate = "Not used"

  override def lowPriority = {
    case msg => updateListeners(msg)
  }
}
