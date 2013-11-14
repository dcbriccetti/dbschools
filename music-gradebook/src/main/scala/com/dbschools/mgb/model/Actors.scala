package com.dbschools.mgb.model

import akka.actor.{PoisonPill, Props, ActorSystem}

object Actors {
  val system = ActorSystem()
  val testingManager = system.actorOf(Props[TestingManager], "testingManager")
  private val all = Seq(testingManager)

  def stop(): Unit = {
    all.foreach(_ ! PoisonPill)
  }
}
