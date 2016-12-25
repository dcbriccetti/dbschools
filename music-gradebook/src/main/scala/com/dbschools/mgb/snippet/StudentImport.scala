package com.dbschools.mgb
package snippet

import scalaz._
import Scalaz._
import org.squeryl.PrimitiveTypeMode._
import org.apache.log4j.Logger
import net.liftweb.util.Helpers._
import net.liftweb.http.SHtml
import schema.{Musician, MusicianGroup, AppSchema}
import model.{Cache, Terms}

/**
 * Imports tab-separated data in this form:
 * Student Name→Permanent Student ID→Grade
 * Jones, Sarah→600001234→6
 */
class StudentImport {
  private val log = Logger.getLogger(getClass)
  private val importTerm = Terms.currentTerm

  def render = {
    var data = ""

    def process(): Unit = {
      val musiciansByPermId = AppSchema.musicians.map(m => m.permStudentId.get -> m).toMap
      val testingGroupIds = Cache.groups.withFilter(_.doesTesting).map(_.id).toSet
      def m(term: Int) = AppSchema.musicianGroups.where(_.school_year === term).
        filter(mg => testingGroupIds.contains(mg.group_id)).groupBy(_.musician_id)
      val musicianGroupsByMusicianIdImportTerm = m(importTerm)
      val musicianGroupsByMusicianIdCurrentTerm = m(Terms.currentTerm)
      val insts = AppSchema.instruments.map(i => i.id -> i).toMap
      val UnassignedInstrumentId = insts.values.find(_.name.get == "Unassigned").get // OK to fail
      val InsertToGroup = 15

      val students = data.split("\n").map(_.trim.split("\t"))

      students.foreach(cols => {
        val nameParts = cols(0).trim.split(", ")
        val last = nameParts(0)
        val first = nameParts(1)
        val id = cols(1).toInt
        val grade = cols(2).toInt

        val (musician, isNew) = musiciansByPermId.get(id).map((_, false)) | {
          val newM = Musician.createRecord.permStudentId(id).last_name(last).first_name(first).
            graduation_year(Terms.gradeAsGraduationYear(grade))
          AppSchema.musicians.insert(newM)
          (newM, true)
        }

        val musicianId = musician.musician_id.get

        val instrumentId = (
          for {
            musicianGroups <- musicianGroupsByMusicianIdCurrentTerm.get(musicianId)
            distinctInstrumentIds = musicianGroups.map(_.instrument_id).toSeq.distinct
            if distinctInstrumentIds.size == 1
          } yield insts(distinctInstrumentIds.head)
          ) | UnassignedInstrumentId

        val newGroupId = (
          for {
            musicianGroups <- musicianGroupsByMusicianIdCurrentTerm.get(musicianId)
            groupIds = musicianGroups.map(_.group_id).toSeq.distinct
            if groupIds.size == 1
          } yield groupIds.head
          ) | InsertToGroup

        val groups = musicianGroupsByMusicianIdImportTerm.get(musicianId).toSeq.flatten
        val alreadyAssigned = groups.exists(_.group_id == newGroupId)
        if (!alreadyAssigned) {
          AppSchema.musicianGroups.insert(MusicianGroup(0, musician.id, newGroupId, instrumentId.id, importTerm))
        }

        val state = (isNew ? "new " | "") + (alreadyAssigned ? "group already assigned " | "")
        log.info(s"$id $last $first $grade $state${instrumentId.name}")
      })
    }

    "#data"     #> SHtml.textarea("", data = _, "class" -> "form-control", "rows" -> "20") &
    "#submit"   #> SHtml.submitButton(process)
  }
}
