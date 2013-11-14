package com.dbschools.mgb.comet

import net.liftweb.actor.LiftActor
import net.liftweb.http.ListenerManager

trait CommonCometDispatcher extends LiftActor with ListenerManager {
  def createUpdate = CommonCometActorMessages.Start

  override def lowPriority = {
    case msg => updateListeners(msg)
  }
}

object CommonCometActorMessages {
  case object Start
}
