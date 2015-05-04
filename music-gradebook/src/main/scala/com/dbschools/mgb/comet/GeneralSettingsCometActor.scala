package com.dbschools.mgb
package comet

import com.dbschools.mgb.snippet.LiftExtensions._
import net.liftweb.http.js.JsCmds.Replace
import net.liftweb.http.{CometActor, CometListener}
import net.liftweb.util.PassThru
import org.apache.log4j.Logger

import scala.language.postfixOps

class GeneralSettingsCometActor extends CometActor with CometListener {
  val log = Logger.getLogger(getClass)
  import CommonCometActorMessages._
  import GeneralSettingsCometActorMessages._

  def registerWith = GeneralSettingsCometDispatcher

  override def lowPriority = {

    case ChangeServicingQueueSelection =>
      replaceDefaultPageSection("queueService")

    case ChangePeriodElements =>
      replaceDefaultPageSection("periodSpan")

    case ChangeSpecialSchedule =>
      replaceDefaultPageSection("specialScheduleOuter")

    case Start =>
  }

  private def replaceDefaultPageSection(elemId: String) {
    val elem = elemFromTemplate("templates-hidden/default", s"#$elemId")
    partialUpdate(Replace(elemId, elem))
  }

  def render = PassThru
}

object GeneralSettingsCometDispatcher extends CommonCometDispatcher

object GeneralSettingsCometActorMessages {
  case object ChangeServicingQueueSelection
  case object ChangePeriodElements
  case object ChangeSpecialSchedule
}
