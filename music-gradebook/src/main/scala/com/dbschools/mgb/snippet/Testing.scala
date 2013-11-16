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
import net.liftweb.http.js.JsCmds.{Focus, FocusOnLoad, Noop, JsShowId, JsHideId}
import LiftExtensions._
import bootstrap.liftweb.ApplicationPaths
import schema.AppSchema
import com.dbschools.mgb.model._
import model.testingState._
import model.TestingManagerMessages._

class Testing extends SelectedMusician {
  def render = {
    var selectedScheduledIds = Set[Int]()
    val opUser = AppSchema.users.where(_.login === Authenticator.userName.get).headOption

    def queueRow(sm: EnqueuedMusician): CssSel = {
      val userName = ~opUser.map(_.last_name)
      val m = sm.musician
      val mgs = AppSchema.musicianGroups.where(mg => mg.musician_id === m.id and mg.school_year === Terms.currentTerm)
      val instrumentNames =
        for {
          instrumentId  <- mgs.map(_.instrument_id)
          instrument    <- Cache.instruments.find(_.id == instrumentId)
        } yield instrument.name.get

      def studentNameTestLink = {
        SHtml.link(ApplicationPaths.studentDetails.href, () => {
          svSelectedMusician(Some(m))
          Actors.testingManager ! TestMusician(TestingMusician(m, userName, DateTime.now))
        }, Text(m.first_name.get + " " + m.last_name))
      }

      "tr [id]"     #> Testing.queueRowId(m.id) &
      "#qrsel *"    #> SHtml.ajaxCheckbox(false, (b) => {
        if (b)
          selectedScheduledIds += sm.musician.id
        else
          selectedScheduledIds -= sm.musician.id
        JsShowIdIf("queueDelete", selectedScheduledIds.nonEmpty)
      }) &
      "#qrstu *"    #> studentNameTestLink &
      "#qrinst *"   #> instrumentNames.toSet /* no dups */ .toSeq.sorted.mkString(", ") &
      "#qrpiece *"  #> sm.nextPieceName
    }

    "#queueDelete" #> SHtml.ajaxButton("Remove Selected", () => {
      Actors.testingManager ! DequeueMusicians(selectedScheduledIds)
      Noop
    }) &
    ".queueRow"   #> enqueuedMusicians.toSeq.sortBy(_.sortOrder).map(queueRow) &
    ".sessionRow" #> testingMusicians.toSeq.sortBy(-_.time.millis).map(Testing.sessionRow(show = true)) &
    "#message"    #> FocusOnLoad(SHtml.ajaxText("",
      _.trim match {
        case "" => Noop
        case msg =>
          Actors.testingManager ! Chat(ChatMessage(DateTime.now, opUser.get, msg))
          JsJqVal("#message", "")
      }, "id" -> "message"
    )) &
    ".messageRow" #> chatMessages.map(Testing.messageRow) &
    "#clearMessages" #> SHtml.ajaxButton("Clear", () => {
      Actors.testingManager ! ClearChat
      Noop
    }, "style" -> (if (chatMessages.isEmpty) "display: none;" else ""))
  }
}

object Testing {

  private val tmf = DateTimeFormat.forStyle("-M")

  def sessionRow(show: Boolean)(tm: TestingMusician): CssSel = {
    val m = tm.musician
    "tr [id]"     #> Testing.sessionRowId(m.id) &
    "tr [style+]" #> (if (show) "" else "display: none;") &
    "#srstu *"    #> (m.first_name.get + " " + m.last_name) &
    "#srtester *" #> tm.testerName &
    "#srtime *"   #> tmf.print(tm.time) &
    ".srasmts *"  #> tm.numAsmts
  }

  def messageRow(chatMessage: ChatMessage) =
    "#mrtime *"   #> tmf.print(chatMessage.time) &
    "#mrtester *" #> chatMessage.user.last_name &
    "#mrmsg *"    #> chatMessage.msg

  def addMessage(chatMessage: ChatMessage) = JsJqPrepend("#messagesTable tbody",
    messageRow(chatMessage)(elemFromTemplate("testing", ".messageRow")).toString().encJs) &
    JsShowId("clearMessages") & Focus("message")

  def clearMessages = JsJqRemove("#messagesTable tbody *") & JsHideId("clearMessages")

  def queueRowId(musicianId: Int) = "qr" + musicianId 
  
  def sessionRowId(musicianId: Int) = "sr" + musicianId
}
