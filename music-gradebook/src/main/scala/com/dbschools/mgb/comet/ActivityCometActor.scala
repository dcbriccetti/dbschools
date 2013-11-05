package com.dbschools.mgb
package comet

import org.apache.log4j.Logger
import net.liftweb.actor.LiftActor
import net.liftweb.http.{ListenerManager, CometListener, CometActor}
import net.liftweb.util.PassThru
import net.liftweb.http.js.jquery.JqJsCmds.PrependHtml
import model.AssessmentRow
import model.BoxOpener._
import snippet.Assessments

case class ActivityStatusUpdate(assessmentRow: AssessmentRow)

class ActivityCometActor extends CometActor with CometListener {
  private val log = Logger.getLogger(getClass)
  def registerWith = ActivityCometDispatcher

  private val rowNodeSeq = // TODO Why does Templates give <html><body></body></html>? Templates(List("_assessmentRow")).open
    <tr class="assessmentRow">
      <td class="date"></td>
      <td class="tester"></td>
      <td class="musician"></td>
      <td class="piece"></td>
      <td class="instrument"></td>
      <td class="comments"></td>
    </tr>


  override def lowPriority = {
    case ActivityStatusUpdate(assessmentRow) =>
      log.info("Got an update!")
      val rowCssSel = Assessments.rowCssSel(Seq(assessmentRow))
      val nodeSeq = rowCssSel(rowNodeSeq)
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
