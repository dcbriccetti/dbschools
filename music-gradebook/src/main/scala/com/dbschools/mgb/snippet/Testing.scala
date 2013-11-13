package com.dbschools.mgb
package snippet

import scala.xml.Text
import scalaz._
import Scalaz._
import org.squeryl.PrimitiveTypeMode._
import org.scala_tools.time.Imports._
import net.liftweb._
import util._
import Helpers._
import net.liftweb.http.SHtml
import net.liftweb.http.js.JsCmds.Noop
import LiftExtensions._
import bootstrap.liftweb.{ApplicationPaths, Actors}
import com.dbschools.mgb.comet.{UnscheduleMusicians, ScheduledMusician, TestingMusician, TestMusician}
import com.dbschools.mgb.schema.AppSchema
import com.dbschools.mgb.model.{Cache, Terms}

class Testing extends SelectedMusician {
  def render = {
    var selectedScheduledIds = Set[Int]()

    def queueRow(sm: ScheduledMusician): CssSel = {
      val userName = ~AppSchema.users.where(_.login === Authenticator.userName.get).headOption.map(_.last_name)
      val m = sm.musician
      val mgs = AppSchema.musicianGroups.where(mg => mg.musician_id === m.id and mg.school_year === Terms.currentTerm)
      val instrumentNames =
        for {
          instrumentId  <- mgs.map(_.instrument_id)
          instrument    <- Cache.instruments.find(_.id == instrumentId)
        } yield instrument.name.get

      def testLink = {
        SHtml.link(ApplicationPaths.studentDetails.href, () => {
          svSelectedMusician(Some(m))
          Actors.testScheduler ! TestMusician(TestingMusician(m, userName, DateTime.now))
        }, Text(m.first_name.get + " " + m.last_name))
      }

      "tr [id]"     #> ("qr" + m.id.toString) &
      "#qrsel *"    #> SHtml.ajaxCheckbox(false, (b) => {
        if (b)
          selectedScheduledIds += sm.musician.id
        else
          selectedScheduledIds -= sm.musician.id
        JsShowIdIf("queueDelete", selectedScheduledIds.nonEmpty)
      }) &
      "#qrstu *"    #> testLink &
      "#qrinst *"   #> instrumentNames.toSet /* no dups */ .toSeq.sorted.mkString(", ") &
      "#qrpiece *"  #> sm.nextPieceName
    }

    "#queueDelete" #> SHtml.ajaxButton("Remove Selected", () => {
      Actors.testScheduler ! UnscheduleMusicians(selectedScheduledIds)
      Noop
    }) &
    ".queueRow"   #> comet.testing.scheduledMusicians.toSeq.sortBy(_.sortOrder).map(queueRow) &
    ".sessionRow" #> comet.testing.testingMusicians.toSeq.sortBy(-_.time.millis).map(Testing.sessionRow(show = true))
  }
}

object Testing {

  def sessionRow(show: Boolean)(tm: TestingMusician): CssSel = {
    val m = tm.musician
    val tmf = DateTimeFormat.forStyle("-M")
    "tr [id]"     #> ("sr" + m.id.toString) &
    "tr [style+]" #> (if (show) "" else "display: none;") &
    "#srstu *"    #> Text(m.first_name.get + " " + m.last_name) &
    "#srtester *" #> Text(tm.testerName) &
    "#srtime *"   #> Text(tmf.print(tm.time))
  }
}
