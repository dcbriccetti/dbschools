package com.dbschools.mgb
package comet

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

  private def musicianSpans(musicians: Seq[EnqueuedMusician]) =
    musicians.map(em => <span style="margin-right: 1em">{studentNameLink(em.musician, test = true)} </span>)

  private var lastNames = Seq[String]()

  override def lowPriority = {

    case Next(enqueuedMusicians) =>
      val theseNames = enqueuedMusicians.map(_.musician.nameFirstLast)
      if (theseNames != lastNames) {
        partialUpdate(SetHtml("nextTesting", musicianSpans(enqueuedMusicians)))
        lastNames = theseNames
      }

    case Start =>
  }


  def render = {
    TestingManager.called match {
      case Nil => PassThru
      case ems => "#nextTesting *" #> musicianSpans(ems)
    }
  }
}

object StudentMessages {
  case class Next(enqueuedMusicians: Seq[EnqueuedMusician])
}

object StudentCometDispatcher extends CommonCometDispatcher
