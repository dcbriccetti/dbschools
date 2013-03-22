package com.dbschools.picker

import java.awt.event.{ActionEvent, ActionListener}
import javax.swing.Timer
import scala.util.Random

/**
 * Contains the logic for doing the picking.
 * @author Dave Briccetti
 */
class Picker {
  private var names = Iterable[String]()
  private var fanfare = false
  private var pickerListeners = List[Listener]()
  private val random = new Random

  def notifyListeners(result: Int, isFinal: Boolean) {
    pickerListeners.foreach(_.itemSelected(result, isFinal))
  }
  
  def pick() {
    if (!fanfare || names.size == 1) {
        notifyListeners(0, isFinal = true)
        return
    }

    // More than one to choose and fanfare requested

    var hatIndex = 0
    var delayMillis = random.nextInt(100)
    val timer = new Timer(delayMillis, null)
    timer.addActionListener(new ActionListener() {
      def actionPerformed(event: ActionEvent) {
        if (delayMillis < 100) {
          notifyListeners(hatIndex, isFinal = false)
          hatIndex = (hatIndex + 1) % names.size
          delayMillis += 5
          timer.setDelay(delayMillis)
        } else {
          timer.stop()
          notifyListeners(hatIndex, isFinal = true)
        }
      }})
    timer.start()
  }
  
  def setFanfare(fanfare: Boolean) {
    this.fanfare = fanfare
  }

  def addListener(listener: Listener) {
    pickerListeners ::= listener
  }

  def removeListener(listener: Listener) {
    pickerListeners -= listener
  }

  def setNames(names: java.util.List[String]) {
    this.names = scala.collection.JavaConversions.asScalaIterable(names)
  }
}
