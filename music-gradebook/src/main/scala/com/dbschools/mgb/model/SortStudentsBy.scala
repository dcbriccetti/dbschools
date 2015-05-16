package com.dbschools.mgb.model

object SortStudentsBy extends Enumeration {
  type SortBy = Value
  val Name = Value("Name")
  val LastAssessment = Value("Last Test")
  val LastPassed = Value("Last Passed")
  val NumPassed = Value("# Passed")
  val PctPassed = Value("% Passed")
  val Streak = Value("Streak")
}
