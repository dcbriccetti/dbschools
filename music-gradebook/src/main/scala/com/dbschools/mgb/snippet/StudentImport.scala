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

    def n(s: String) = asInt(s).toOption | 0

    def process(): Unit = {
      val musicians = AppSchema.musicians.map(m => m.permStudentId.get -> m).toMap
      val musicianGroupsByMusicianId = AppSchema.musicianGroups.where(mg => mg.school_year === Terms.currentTerm).
        groupBy(_.musician_id)
      val insts = AppSchema.instruments.map(i => i.id -> i).toMap
      val uainst = insts.values.find(_.name.get == "Unassigned").get // OK to fail
      val groupIdsByPeriod = join(AppSchema.groups, AppSchema.groupTerms)((g, gt) =>
        where(gt.term === Terms.currentTerm)
        select(gt.period -> g.id)
        on(g.id === gt.groupId)
      ).toMap

      data.split("\n").foreach(row => {
        val cols = row.split("\t")
        val last    = cols(0)
        val first   = cols(1)
        val id      = n(cols(2))
        val grade   = n(cols(3))
        val period  = n(cols(4))
        val groupId = groupIdsByPeriod(period) // OK to fail

        val (musician, isNew) = musicians.get(id).map((_, false)) | {
          val newM = Musician.createRecord.permStudentId(id).last_name(last).first_name(first).
            graduation_year(Terms.gradeAsGraduationYear(grade))
          AppSchema.musicians.insert(newM)
          (newM, true)
        }
        val maybeGroups = musicianGroupsByMusicianId.get(musician.musician_id.get)
        val instrument = (
          for {
            musicianGroups <- maybeGroups
            distinctInstrumentIds = musicianGroups.map(_.instrument_id).toSeq.distinct
            if distinctInstrumentIds.size == 1
          } yield insts(distinctInstrumentIds.head)
        ) | uainst


        val alreadyAssigned = maybeGroups.map(_.exists(_.group_id == groupId)) | false
        if (! alreadyAssigned) {
          AppSchema.musicianGroups.insert(MusicianGroup(0, musician.id,
            groupId, instrument.id, Terms.currentTerm))
        }

        val state = (isNew ? "new " | "") + (alreadyAssigned ? "group already assigned " | "")
        log.info(s"$id $last $first $grade $period $state${instrument.name}")
      })
    }

    "#data"     #> SHtml.textarea("", data = _, "class" -> "form-control") &
    "#submit"   #> SHtml.submitButton(process)
  }
}
