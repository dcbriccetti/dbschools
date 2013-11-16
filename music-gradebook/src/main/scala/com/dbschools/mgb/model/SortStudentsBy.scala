package com.dbschools.mgb.model

object SortStudentsBy extends Enumeration {
  type SortBy = Value
  val Name = Value("Name")
  val LastAssessment = Value("Last Assessment")
  val LastPiece = Value("Last Piece")
}
