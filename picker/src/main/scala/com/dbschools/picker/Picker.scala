package com.dbschools.picker

import java.awt.event.{ActionEvent, ActionListener}
import javax.swing.Timer
import scala.util.Random

/**
 * Contains the logic for doing the picking.
 * @author Dave Briccetti
 */
class Picker {
  private var names: java.util.List[String] = null
  private var fanfare: Boolean = false
  private var pickerListeners: List[Listener] = Nil
  private val random = new Random

  def notifyListeners(result: Int, isFinal: Boolean) {
    pickerListeners.foreach(l => l.itemSelected(result, isFinal))
  }
  
  def pick {
    if (names == null) throw new IllegalStateException("must call setNames")
        
    if (!fanfare || names.size == 1) {
        notifyListeners(0, true)
        return
    }

    // More than one to choose and fanfare requested

    var hatIndex = 0
    var delayMillis = random.nextInt(100)
    var timer: Timer = null

    timer = new Timer(delayMillis, new ActionListener() {
      def actionPerformed(event: ActionEvent) {
        if (delayMillis < 100) {
          notifyListeners(hatIndex, false)
          hatIndex = (hatIndex + 1) % names.size
          delayMillis += 5
          timer.setDelay(delayMillis)
        } else {
          timer.stop
          notifyListeners(hatIndex, true)
        }
      }})
    timer.start
  }
  
  def setFanfare(fanfare: Boolean) {
    this.fanfare = fanfare
  }

  def addListener(listener: Listener) {
    pickerListeners = listener :: pickerListeners
  }

  def removeListener(listener: Listener) {
    pickerListeners -= listener
  }

  def setNames(names: java.util.List[String]) {
    this.names = names
  }

}
