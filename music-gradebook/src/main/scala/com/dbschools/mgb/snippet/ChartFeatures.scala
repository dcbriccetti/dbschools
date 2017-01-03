package com.dbschools.mgb
package snippet

import scalaz._
import Scalaz._
import schema.{Piece}

trait ChartFeatures {
  val bookNames = Array("Red", "Blue", "Green")
  def findBookIndex(piece: Piece): Int = bookNames.indexWhere(bn => piece.name.get startsWith bn)
  def toA(vals: Iterable[Int]): String = vals.mkString("[", ",", "]")
}
