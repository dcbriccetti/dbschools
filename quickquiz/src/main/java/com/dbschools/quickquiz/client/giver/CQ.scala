package com.dbschools.quickquiz.client.giver
import scala.swing._
import scala.swing.event._

object CQ extends SimpleGUIApplication {
  def top = new MainFrame {
    title = "Hi there"
    val button = new Button {
      text = "Hi"
    }
    val label = new Label {
      text = "No registered"
    }
    contents = new BoxPanel(Orientation.Vertical) {
      contents += button
      contents += label
      border = Swing.EmptyBorder(30, 30, 10, 30)
    }
    listenTo(button)
    var nClicks = 0
    reactions += {
      case ButtonClicked(b) =>
        nClicks += 1
        label.text = "Num: " + nClicks
    }
  }
  override def main(args: Array[String]) {
    super.main(args)
  }
}
