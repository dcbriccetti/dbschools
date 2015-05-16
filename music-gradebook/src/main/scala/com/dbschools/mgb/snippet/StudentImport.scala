package com.dbschools.mgb
package snippet

import scalaz._
import Scalaz._
import org.squeryl.PrimitiveTypeMode._
import org.apache.log4j.Logger
import net.liftweb.util.Helpers._
import net.liftweb.http.SHtml
import schema.{MusicianGroup, AppSchema}
import model.Terms

class StudentImport {
  private val log = Logger.getLogger(getClass)

  def render = {
    var data = ""

    def process(): Unit = {
      val musicians = AppSchema.musicians.toList
      val musicianGroupsByMusicianId = AppSchema.musicianGroups.where(mg => mg.school_year === Terms.currentTerm).
        groupBy(_.musician_id)
      val insts = AppSchema.instruments.map(i => i.id -> i).toMap
      val uainst = insts.values.find(_.name.get == "Unassigned").get // OK to fail
      val newGroup = 3 // Cadet

      data.split("\n").foreach(row => {
        val cols = row.trim.split("\t")
        val last    = cols(0)
        val first   = cols(1)

        val matchingMusicians = musicians.filter(m => m.last_name.get == last && m.first_name.get == first)
        matchingMusicians match {
          case Nil => log.warn(s"No match for $first $last")

          case musician :: Nil =>
            val groupsForMusician = musicianGroupsByMusicianId.get(musician.musician_id.get)
            val instrument = (
              for {
                musicianGroups <- groupsForMusician
                distinctInstrumentIds = musicianGroups.map(_.instrument_id).toSeq.distinct
                if distinctInstrumentIds.size == 1
              } yield insts(distinctInstrumentIds.head)
              ) | uainst

            AppSchema.musicianGroups.insert(MusicianGroup(0, musician.id,
              newGroup, instrument.id, Terms.nextTerm))

            log.info(s"$id $last $first ${instrument.name}")

          case m => log.warn(s"${m.size} matches for $id")
        }
      })
    }

    "#data"     #> SHtml.textarea("", data = _, "class" -> "form-control") &
    "#submit"   #> SHtml.submitButton(process)
  }
}
