package com.dbschools.mgb.snippet

import scalaz._
import Scalaz._
import org.squeryl.PrimitiveTypeMode._
import org.apache.log4j.Logger
import net.liftweb.util.Helpers._
import net.liftweb.http.SHtml
import com.dbschools.mgb.schema.{MusicianGroup, Musician, AppSchema}
import com.dbschools.mgb.model.Terms

class StudentImport {
  private val log = Logger.getLogger(getClass)

  def render = {
    var data = ""
    val c = "class" -> "form-control"
    def n(s: String) = asInt(s).toOption | 0
    def process(): Unit = {
      val musicians = AppSchema.musicians.map(m => m.student_id.get -> m).toMap
      val mas = AppSchema.musicianGroups.where(mg => mg.school_year === Terms.currentTerm).
        groupBy(_.musician_id)
      val insts = AppSchema.instruments.map(i => i.id -> i).toMap
      val uainst = insts.values.find(_.name.get == "Unassigned").get // OK to fail
      val incomingGroupId = AppSchema.groups.where(_.name === "Unassigned").head.id // OK to fail

      data.split("\n").foreach(row => {
        val cols = row.split("\t")
        val id = n(cols(0))
        val last = cols(1)
        val first = cols(2)
        val grade = n(cols(3))

        val musician = musicians.get(id) | {
          val newM = Musician.createRecord.student_id(id).last_name(last).first_name(first).
            graduation_year(Terms.gradeAsGraduationYear(grade - 1))
          AppSchema.musicians.insertOrUpdate(newM)
          newM
        }
        val opExistingMusician = musicians.get(id)

        val instrument = (for {
          musicianGroups <- mas.get(musician.musician_id.get)
          distinctInstrumentIds = musicianGroups.map(_.instrument_id).toSeq.distinct
          if distinctInstrumentIds.size == 1
        } yield insts(distinctInstrumentIds.head)) | uainst

        val state = opExistingMusician.nonEmpty ? "" | "new "
        log.info(s"$id $last $first $grade $state${instrument.name}")

        AppSchema.musicianGroups.insert(MusicianGroup(0, musician.id, incomingGroupId, instrument.id,
          Terms.currentTerm + 1))
      })
    }

    "#data"     #> SHtml.textarea("", data = _, c) &
    "#submit"   #> SHtml.submitButton(process)
  }
}
