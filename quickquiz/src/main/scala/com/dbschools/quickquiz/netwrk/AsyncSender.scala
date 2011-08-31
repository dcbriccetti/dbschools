package com.dbschools.quickquiz.netwrk

import java.io.Serializable
import java.util.concurrent.{LinkedBlockingQueue}
import org.jgroups.{JChannel, Address}

class QueueEntry(val channel: JChannel, val address: Address, val message: Serializable)

class AsyncSender {
  val queue = new LinkedBlockingQueue[QueueEntry]
  new Thread(new Runnable() {
    def run {
      var keepRunning = true
      while(keepRunning) {
        val qe = queue.take
        if (qe.message == null) {
          keepRunning = false
        } else {
          qe.channel.send(qe.address, null, qe.message)
        }
      }
    }
  }, "AsynchSender").start

  def enqueue(channel: JChannel, address: Address, message: Serializable) {
    queue.put(new QueueEntry(channel, address, message))        
  }
}