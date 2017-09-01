package com.dbschools.mgb
package model

import scalaz._
import Scalaz._
import org.squeryl.PrimitiveTypeMode._
import org.apache.log4j.Logger
import schema.{AppSchema, Musician, MusicianGroup}

private[mgb] class StudentImporter {
  private val log = Logger.getLogger(getClass)
  private val importYear = SchoolYears.current

  /**
   * Imports tab-separated data in this form:
   * Student Name→Permanent Student ID→Grade
   * Jones, Sarah→600001234→6
   */
  def importStudents(data: String, insertToGroup: Int): Unit = {
    val musiciansByPermId = AppSchema.musicians.map(m => m.permStudentId.get -> m).toMap
    val testingGroupIds = Cache.groups.withFilter(_.doesTesting).map(_.id).toSet
    def m(schoolYear: Int) = AppSchema.musicianGroups.where(_.school_year === schoolYear).
      filter(mg => testingGroupIds.contains(mg.group_id)).groupBy(_.musician_id)
    val musicianGroupsByMusicianIdImportTerm = m(importYear)
    val musicianGroupsByMusicianIdPrevTerm = m(importYear - 1)
    val insts = AppSchema.instruments.map(i => i.id -> i).toMap
    val UnassignedInstrumentId = insts.values.find(_.name.get == "Unassigned").get // OK to fail

    val students = data.split("\n").map(_.trim.split("\t")).filter(_.length == 3)

    students.foreach(cols => {
      val nameParts = cols(0).trim.split(", ")
      val last = nameParts(0)
      val first = nameParts(1)
      val id = cols(1).toInt
      val grade = cols(2).toInt

      val (musician, isNew) = musiciansByPermId.get(id).map((_, false)) | {
        val newM = Musician.createRecord.permStudentId(id).last_name(last).first_name(first).
          graduation_year(SchoolYears.gradeAsGraduationYear(grade))
        AppSchema.musicians.insert(newM)
        (newM, true)
      }

      val musicianId = musician.musician_id.get

      val instrumentId = (
        for {
          musicianGroups <- musicianGroupsByMusicianIdPrevTerm.get(musicianId)
          distinctInstrumentIds = musicianGroups.map(_.instrument_id).toSeq.distinct
          if distinctInstrumentIds.size == 1
        } yield insts(distinctInstrumentIds.head)
        ) | UnassignedInstrumentId

      val groups = musicianGroupsByMusicianIdImportTerm.get(musicianId).toSeq.flatten
      val alreadyAssigned = groups.exists(_.group_id == insertToGroup)
      if (!alreadyAssigned) {
        AppSchema.musicianGroups.insert(MusicianGroup(0, musician.id, insertToGroup, instrumentId.id, importYear))
      }

      val state = (isNew ? "new " | "") + (alreadyAssigned ? "group already assigned " | "")
      log.info(s"$id $last $first $grade $state${instrumentId.name}")
    })
  }
}
