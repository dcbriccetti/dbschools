package com.dbschools.mgb.model

import scala.language.implicitConversions
import net.liftweb.common.Box

object BoxOpener {
  /** Convert a Box into a BoxOpener so the simpler open can be used in place of
    * openOrThrowException.
    */
  implicit def boxToBoxOpener[A](box: Box[A]) = new BoxOpener(box)
}

class BoxOpener[A](box: Box[A]) {
  /** A shortcut for openOrThrowException */
  def open = box.openOrThrowException("Always exists")
}
