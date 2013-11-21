package com.dbschools.mgb.model

object SortStudentsBy extends Enumeration {
  type SortBy = Value
  val Name = Value("Name")
  val LastAssessment = Value("Last Test")
  val LastPiece = Value("Last Piece Passed")
}
