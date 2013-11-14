package com.dbschools.mgb.model

import akka.actor.{PoisonPill, Props, ActorSystem}
import com.dbschools.mgb.comet.TestSchedulerActor

object Actors {
  val system = ActorSystem()
  val testScheduler = system.actorOf(Props[TestSchedulerActor], "testScheduler")
  private val all = Seq(testScheduler)

  def stop(): Unit = {
    all.foreach(_ ! PoisonPill)
  }
}
