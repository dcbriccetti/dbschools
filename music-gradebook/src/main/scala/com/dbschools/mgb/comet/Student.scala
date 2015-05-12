package com.dbschools.mgb
package comet

import net.liftweb.http.{CometListener, CometActor}
import net.liftweb.http.js.JsCmds.SetHtml
import net.liftweb.util.PassThru
import snippet.Testing.studentNameLink
import model.TestingManager
import schema.Musician

class Student extends CometActor with CometListener {
  import StudentMessages._
  import CommonCometActorMessages._

  def registerWith = StudentCometDispatcher

  override protected def dontCacheRendering = true

  private def musicianSpans(musicians: Iterable[Musician]) =
    musicians.toSeq.map(m => <span style="margin-right: 1em">{studentNameLink(m, test = true)} </span>)

  private var lastNames: Iterable[String] = Seq()

  override def lowPriority = {

    case Next(musicians) =>
      val theseNames = musicians.map(_.nameFirstNickLast)
      if (theseNames != lastNames) {
        partialUpdate(SetHtml("nextTesting", musicianSpans(musicians)))
        lastNames = theseNames
      }

    case Start =>
  }

  def render = {
    TestingManager.called match {
      case musicians if musicians.isEmpty => PassThru
      case musicians                      => "#nextTesting *" #> musicianSpans(musicians)
    }
  }
}

object StudentMessages {
  case class Next(musicians: Iterable[Musician])
}

object StudentCometDispatcher extends CommonCometDispatcher
