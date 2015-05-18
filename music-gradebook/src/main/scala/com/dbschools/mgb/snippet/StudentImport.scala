package com.dbschools.mgb
package snippet

import scalaz._
import Scalaz._
import org.squeryl.PrimitiveTypeMode._
import org.apache.log4j.Logger
import net.liftweb.util.Helpers._
import net.liftweb.http.SHtml
import schema.{Musician, MusicianGroup, AppSchema}
import model.Terms

class StudentImport {
  private val log = Logger.getLogger(getClass)
  private val importTerm = Terms.nextTerm

  def render = {
    var data = ""

    def process(): Unit = {
      val musicians = AppSchema.musicians.map(m => m.permStudentId.get -> m).toMap
      val musicianGroupsByMusicianId = AppSchema.musicianGroups.where(mg => mg.school_year === importTerm).
        groupBy(_.musician_id)
      val insts = AppSchema.instruments.map(i => i.id -> i).toMap
      val uainst = insts.values.find(_.name.get == "Unassigned").get // OK to fail
      val newGroup = 3 // Cadet

      val allLines = data.split("\n").map(_.trim.split("\t")) // Pairs of stu -> perm, then student data
      val (idPairs, students) = allLines.partition(_.length == 2)
      val stuToPerm = idPairs.map(ids => ids(0).toInt -> ids(1).toInt).toMap

      students.foreach(cols => {
        val stuId = cols(0).toInt
        val last = cols(1)
        val first = cols(2)
        val nextGrade = cols(3).toInt

        stuToPerm.get(stuId) match {
          case None =>
            log.error(s"No perm ID provided for $stuId, $first $last $nextGrade")

          case Some(id) =>
            val (musician, isNew) = musicians.get(id).map((_, false)) | {
              val newM = Musician.createRecord.permStudentId(id).last_name(last).first_name(first).
                graduation_year(Terms.gradeAsGraduationYear(nextGrade - 1))
              AppSchema.musicians.insert(newM)
              (newM, true)
            }

            val groupsForMusician = musicianGroupsByMusicianId.get(musician.musician_id.get)
            val instrument = (
              for {
                musicianGroups <- groupsForMusician
                distinctInstrumentIds = musicianGroups.map(_.instrument_id).toSeq.distinct
                if distinctInstrumentIds.size == 1
              } yield insts(distinctInstrumentIds.head)
              ) | uainst

            val alreadyAssigned = groupsForMusician.map(_.exists(_.group_id == newGroup)) | false
            if (! alreadyAssigned) {
              AppSchema.musicianGroups.insert(MusicianGroup(0, musician.id,
                newGroup, instrument.id, Terms.nextTerm))
            }

            val state = (isNew ? "new " | "") + (alreadyAssigned ? "group already assigned " | "")
            log.info(s"$id $last $first $nextGrade $state${instrument.name}")
        }
      })
    }

    "#data"     #> SHtml.textarea("", data = _, "class" -> "form-control", "rows" -> "20") &
    "#submit"   #> SHtml.submitButton(process)
  }
}
