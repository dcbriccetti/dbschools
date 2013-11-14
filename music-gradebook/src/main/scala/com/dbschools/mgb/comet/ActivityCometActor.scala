package com.dbschools.mgb
package comet

import net.liftweb.http.{CometListener, CometActor}
import net.liftweb.util.PassThru
import net.liftweb.http.js.jquery.JqJsCmds.PrependHtml
import snippet.Assessments
import model.AssessmentRow

/** Pushes new assessments to the Activity and Student Details pages */
class ActivityCometActor extends CometActor with CometListener {
  import ActivityCometActorMessages._
  import CommonCometActorMessages._

  def registerWith = ActivityCometDispatcher

  override def lowPriority = {

    case ActivityStatusUpdate(assessmentRow) =>
      val nodeSeq = Assessments.createRow(assessmentRow)
      partialUpdate(PrependHtml("assessmentsBody", nodeSeq))

    case Start =>
  }

  def render = PassThru
}

object ActivityCometActorMessages {
  case class ActivityStatusUpdate(assessmentRow: AssessmentRow)
}

object ActivityCometDispatcher extends CommonCometDispatcher
