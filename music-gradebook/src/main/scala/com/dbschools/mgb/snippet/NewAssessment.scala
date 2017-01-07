package com.dbschools.mgb
package snippet

import java.sql.Timestamp

import scala.xml.{NodeSeq, Text}
import scalaz._
import Scalaz._
import org.scala_tools.time.Imports._
import net.liftweb.util.Helpers._
import net.liftweb.util.CssSel
import net.liftweb.http
import http.SHtml
import http.js.JsCmd
import http.js.JsCmds.{Noop, ReplaceOptions, Script, SetHtml, SetValById}
import http.js.jquery.JqJsCmds
import http.js.JE.JsRaw
import net.liftweb.common.{Empty, Full}
import JqJsCmds.{FadeOut, PrependHtml}
import schema._
import model._
import model.TestingManagerMessages.IncrementMusicianAssessmentCount
import comet.ActivityCometDispatcher
import comet.ActivityCometActorMessages._
import LiftExtensions._

class NewAssessment extends SelectedMusician {
  val lastPassFinder = new LastPassFinder()

  def render: CssSel = {
    val s = new AssessmentState(lastPassFinder)

    def jsTempo = JsRaw(s"tempoBpm = ${s.tempoFromPiece}").cmd

    def jsMetroSoundNum = JsRaw(s"metroSoundNum = ${Authenticator.metronome};").cmd

    def tempoControl = SHtml.number(s.tempoFromPiece, (t: Int) => {}, min = 30, max = 180)

    def sendTempo: JsCmd = {
      val tempo = s.tempoFromPiece
      JsJqVal("#tempo", tempo) & JsRaw(s"tempoBpm = $tempo;").cmd
    }

    def selPiece = {
      val initialSel = s.opSelPieceId.map(p => Full(p.toString)) getOrElse Empty
      SHtml.ajaxSelect(Cache.pieces.map(p => p.id.toString -> p.name.get),
        initialSel, (p) => {
          s.opSelPieceId = Some(p.toInt)
          sendTempo
        })
    }

    def selMetroSound = {
      val sortedSounds = MetroSounds.values.toSeq.sortBy(_.id)
      SHtml.ajaxSelect(sortedSounds.map(s => s.id.toString -> s.toString.replace("_", " ")),
        Full(Authenticator.metronome.toString), (p) => {
          Authenticator.metronome(p.toInt)
          jsMetroSoundNum
        })
    }

    def commentText = SHtml.textarea("", (n) => {
      s.notes = n
      Noop
    })

    def checkboxes(part: Int) = {
      val grouped = Cache.tags.grouped(math.ceil(Cache.tags.size / 2f).toInt).toSeq
      <div>{grouped(part).map(tag =>
        <div class="checkbox">
          <label>
            {SHtml.checkbox(false, s.commentTagSelections(tag.id) = _)}{tag.commentText}
          </label>
        </div>)}
      </div>
    }

    def selectedCommentIds =
      for {
        (commentId, selected) <- s.commentTagSelections
        if selected
      } yield commentId

    val pieceNames = Cache.pieces.map(p => p.id -> p.name.get).toMap

    def updatePageForNextAssessment(row: AssessmentRow, curAsmt: Assessment) =
      PrependHtml("assessmentsBody", Assessments.createRow(row, keepStudent = false)) &
      SetHtml("lastPiece", Text(StudentDetails.lastPiece(lastPassFinder, curAsmt.musician_id))) &
      (s.opSelPieceId.map(id => JsJqVal("#piece", id)) getOrElse Noop) &
      sendTempo &
      SetValById("commentText", "") &
      SetHtml("checkbox1", checkboxes(0)) &
      SetHtml("checkbox2", checkboxes(1)) &
      {
        val id = "passFailConfirmation"
        val msg = if (curAsmt.pass) "Passed " else "Failed "
        SetHtml(id, Text(msg + pieceNames(curAsmt.pieceId))) & JqJsCmds.Show(id) & FadeOut(id)
      }

    def createAssessmentRow(asmt: Assessment, asmtTime: DateTime, musician: Musician, user: User): AssessmentRow = {
      val inst = s.opSelInstId.flatMap(id => Cache.instruments.find(_.id == id)).map(_.name.get)
      val subinst = s.opSelSubinstId.flatMap(id => Cache.subinstruments.find(_.id == id)).map(_.name.get)
      def o(s: String) = if (s.isEmpty) None else Some(s)
      val expandedNotes = {
        val selIds = selectedCommentIds.toSet
        val joinedSelectedTags = Cache.tags.filter(selIds contains _.id).map(_.commentText).mkString(", ")
        Seq(o(joinedSelectedTags), o(s.notes)).flatten.mkString("; ")
      }

      AssessmentRow(asmt.id, asmtTime, musician, user.last_name,
        ~s.opSelPieceId.map(id => pieceNames(id)),
        ~inst, subinst, asmt.pass, o(expandedNotes))
    }

    def recordAss(pass: Boolean): JsCmd = {
      (for {
        musician  <- opMusician
        iid       <- s.opSelInstId
        pid       <- s.opSelPieceId
        user      <- Authenticator.opLoggedInUser
      } yield {
        val asmtTime = DateTime.now
        val asmt = Assessment(
          id                = 0,
          assessment_time   = new Timestamp(asmtTime.getMillis),
          musician_id       = musician.id,
          instrument_id     = iid,
          subinstrument_id  = s.opSelSubinstId,
          user_id           = user.id,
          pieceId           = pid,
          pass              = pass,
          notes             = s.notes
        )
        AppSchema.assessments.insert(asmt)
        AppSchema.assessmentTags.insert(selectedCommentIds.map(id => AssessmentTag(asmt.id, id)))

        model.Assessments.notifyListeners(TestSavedEvent(musician.id, asmtTime))
        
        val row = createAssessmentRow(asmt, asmtTime, musician, user)
        ActivityCometDispatcher ! ActivityStatusUpdate(row)
        Actors.testingManager ! IncrementMusicianAssessmentCount(user, musician)
        s.next(pass = pass)

        updatePageForNextAssessment(row, asmt)
      }) | Noop
    }

    val subinstId = "subinstrument"
    val initialInstrumentSel = s.opSelInstId.map(i => Full(i.toString)) getOrElse Empty

    def subinstSels(instId: Int): List[(String, String)] =
      Cache.subsByInstrument.get(instId).toList.flatten.map(si => si.id.toString -> si.name.get)

    def selInst = SHtml.ajaxSelect(Cache.instruments.map(i => i.id.toString -> i.name.get), initialInstrumentSel, (p) => {
      val instId = p.toInt
      s.opSelInstId = Some(instId)
      val sels = subinstSels(instId)
      sels.headOption.foreach(sel => s.opSelSubinstId = Some(sel._1.toInt))
      ReplaceOptions(subinstId, sels, Empty) & JsShowIdIf(subinstId, sels.nonEmpty)
    })

    def selSubinst = {
      val opts = s.opSelInstId.map(subinstSels) getOrElse Seq[(String, String)]()
      opts.headOption.foreach(sel => s.opSelSubinstId = Some(sel._1.toInt))
      SHtml.ajaxUntrustedSelect(opts, Empty, (idString) => {
        s.opSelSubinstId = Some(idString.toInt)
        Noop
      }, displayNoneIf(opts.isEmpty))
    }

    "#instrument"     #> selInst &
    s"#$subinstId"    #> selSubinst &
    "#piece"          #> selPiece &
    "#metroSound"     #> selMetroSound &
    "#tempo"          #> tempoControl &
    "#setTempo"       #> Script(jsTempo) &
    "#setMetroSoundNum" #> Script(jsMetroSoundNum) &
    "#checkbox1 *"    #> checkboxes(0) &
    "#checkbox2 *"    #> checkboxes(1) &
    "#commentText"    #> commentText &
    "#passButton"     #> SHtml.ajaxSubmit("Pass", () => { recordAss(pass = true ) }) &
    "#failButton"     #> SHtml.ajaxSubmit("Fail", () => { recordAss(pass = false) })
  }

  def audioControls: NodeSeq = {
    MetroSounds.values.map(s => {
      val filename = s.toString + ".wav"
      <audio id={s"audioControl${s.id}"} src={s"assets/audio/$filename"} preload="auto">
        Please use a standards-compliant browser.
      </audio>
    }).foldLeft(NodeSeq.Empty)(_ ++ _)
  }
}
