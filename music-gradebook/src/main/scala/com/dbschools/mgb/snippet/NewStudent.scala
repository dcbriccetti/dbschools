package com.dbschools.mgb.snippet

import net.liftweb._
import common.Loggable
import http._
import com.dbschools.mgb.schema.{Musician, AppSchema}
import com.dbschools.mgb.schema.IdGenerator.genId

/**
 * Declare the fields on the screen
 */
object NewStudent extends LiftScreen with Loggable {
  val musician = Musician.createRecord
  addFields(() => musician)

  def finish() {
    val s = musician
    logger.warn(validate)
    logger.warn(s.student_id.is.toString)
    logger.warn(s.first_name.is)
    val musGenId = musician.musician_id(genId())
    AppSchema.musicians.insert(musGenId)
  }
}
