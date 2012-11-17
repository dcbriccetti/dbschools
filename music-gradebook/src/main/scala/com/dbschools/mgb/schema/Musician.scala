package com.dbschools.mgb.schema

case class Musician(
  musician_id:     Int,
  student_id:      Int,
  first_name:      String,
  last_name:       String,
  graduation_year: Int,
  sex:             String
) {
  def name = last_name + ", " + first_name
}
