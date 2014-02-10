package com.dbschools.mgb.schema

import scalaz._
import Scalaz._
import org.squeryl.annotations._
import net.liftweb.record.{MetaRecord, Record}
import net.liftweb.record.field.{OptionalStringField, StringField, IntField}
import net.liftweb.squerylrecord.KeyedRecord
import net.liftweb.util.FieldError
import com.dbschools.mgb.model.Terms

case class Musician private() extends Record[Musician] with KeyedRecord[Int]{
  override def meta = Musician

  @Column("id")
  override val idField = new IntField(this) {
    override def shouldDisplay_? = false
  }

  def musician_id = idField

  @Column("student_id")
  val student_id = new IntField(this) {
    override def displayName = "Student ID"
    override def validations = nonZero _ :: super.validations

    def nonZero(value: Int) =
      if (value < 1)
        List(FieldError(this, "Invalid value"))
      else
        Nil
  }

  @Column("perm_student_id")
  val permStudentId = new IntField(this) {
    override def displayName = "Permanent Student ID"
    override def shouldDisplay_? = true
  }

  @Column("first_name")
  val first_name = new StringField(this, "") {
    override def displayName = "First Name"
    override def validations = valMinLen(1, "May not be blank") _ :: super.validations
  }

  @Column("last_name")
  val last_name = new StringField(this, "") {
    override def displayName = "Last Name"
    override def validations = valMinLen(1, "May not be blank") _ :: super.validations
  }

  val nickname = new OptionalStringField(this, None) {
    override def displayName = "Nickname"
  }

  @Column("graduation_year")
  val graduation_year = new IntField(this, Terms.currentTerm) {
    override def displayName = "Graduation Year"
  }

  def name = last_name.get + ", " + first_name.get + nickOrBlank

  def nameFirstLast = first_name.get + nickOrBlank + " " + last_name.get

  private def nickOrBlank = ~nickname.get.map(n => s" ($n)")
}

object Musician extends Musician with MetaRecord[Musician] {
  override def fieldOrder = List(student_id, first_name, nickname, last_name, graduation_year)
}
