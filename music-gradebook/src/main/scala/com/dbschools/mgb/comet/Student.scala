package com.dbschools.mgb
package comet

import scala.xml.NodeSeq
import net.liftweb.http.{CometListener, CometActor}
import net.liftweb.http.js.JsCmds.SetHtml
import net.liftweb.util.PassThru
import snippet.Testing.studentNameLink
import model.{TestingManager, EnqueuedMusician}

class Student extends CometActor with CometListener {
  import StudentMessages._
  import CommonCometActorMessages._

  def registerWith = StudentCometDispatcher

  override protected def dontCacheRendering = true

  override def lowPriority = {

    case Next(opEnqueuedMusician) =>
      partialUpdate(
        SetHtml("nextTesting", opEnqueuedMusician.map(em => studentNameLink(em.musician, test = true)) getOrElse
          NodeSeq.Empty))

    case Start =>
  }

  def render = {
    TestingManager.opNext match {
      case Some(em) => "#nextTesting *" #> studentNameLink(em.musician, test = true)
      case _ => PassThru
    }
  }
}

object StudentMessages {
  case class Next(opEnqueuedMusician: Option[EnqueuedMusician])
}

object StudentCometDispatcher extends CommonCometDispatcher
