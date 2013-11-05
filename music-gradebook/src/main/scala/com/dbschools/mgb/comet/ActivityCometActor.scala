package com.dbschools.mgb
package comet

import org.apache.log4j.Logger
import net.liftweb.actor.LiftActor
import net.liftweb.http.{ListenerManager, CometListener, CometActor}
import net.liftweb.util.PassThru
import net.liftweb.http.js.jquery.JqJsCmds.PrependHtml
import snippet.Assessments
import model.AssessmentRow

case class ActivityStatusUpdate(assessmentRow: AssessmentRow)

class ActivityCometActor extends CometActor with CometListener {
  private val log = Logger.getLogger(getClass)
  def registerWith = ActivityCometDispatcher


  override def lowPriority = {
    case ActivityStatusUpdate(assessmentRow) =>
      log.info("Got an update!")
      val nodeSeq = Assessments.createRow(assessmentRow)
      partialUpdate(PrependHtml("assessmentsBody", nodeSeq))
    case _ =>
  }

  def render = PassThru
}

object ActivityCometDispatcher extends LiftActor with ListenerManager {
  def createUpdate = "Not used"

  override def lowPriority = {
    case msg => updateListeners(msg)
  }
}
