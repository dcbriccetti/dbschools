package com.dbschools.mgb.snippet

import net.liftweb._
import http._
import record.field.{StringField, IntField}
import record.{MetaRecord, Record}
import squerylrecord.KeyedRecord
import org.squeryl.annotations._

/**
 * Declare the fields on the screen
 */
object NewStudent extends LiftScreen {
  object NS extends ScreenVar(NewStu.createRecord)
  addFields(() => NS.is)

  def finish() {
  }
}

/** Experimental Record implementation of Musician */
case class NewStu private() extends Record[NewStu] with KeyedRecord[Int]{
  override def meta = NewStu

  @Column("musician_id")
  override val idField = new IntField(this) {
    override def shouldDisplay_? = false
  }

  @Column("student_id")
  val studentId = new IntField(this) {
    override def displayName = "Student ID"
  }

  @Column("first_name")
  val firstName = new StringField(this, "")

  @Column("last_name")
  val lastName = new StringField(this, "")

  @Column("graduation_year")
  val graduationYear = new IntField(this)

  val sex = new StringField(this, "")

}

object NewStu extends NewStu with MetaRecord[NewStu]
