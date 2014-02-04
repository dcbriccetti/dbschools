package com.dbschools.mgb.model

import scala.concurrent.duration._
import scala.language.postfixOps
import akka.actor.{PoisonPill, Props, ActorSystem}
import TestingManagerMessages.Tick

object Actors {
  val system = ActorSystem()
  val testingManager = system.actorOf(Props[TestingManager], "testingManager")
  testingManager ! Tick // Why doesn’t this get received right away?
  import Actors.system.dispatcher // for execution context
  system.scheduler.schedule(0 milliseconds, 1 seconds, testingManager, Tick)

  private val all = Seq(testingManager)

  def stop(): Unit = {
    all.foreach(_ ! PoisonPill)
  }
}
