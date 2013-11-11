package com.dbschools.mgb
package snippet

import java.sql.Timestamp
import scala.xml.Text
import org.apache.log4j.Logger
import scalaz._
import Scalaz._
import org.squeryl.PrimitiveTypeMode._
import org.scala_tools.time.Imports._
import net.liftweb.util.Helpers._
import net.liftweb.http
import http.SHtml
import net.liftweb.http.js.JsCmds._
import net.liftweb.common.{Empty, Full}
import net.liftweb.http.js.jquery.JqJsCmds
import JqJsCmds.{FadeOut, PrependHtml}
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmds.ReplaceOptions
import net.liftweb.http.js.JE.JsRaw
import model.{Cache, GroupAssignments, LastPassFinder, Terms}
import schema.{Assessment, AssessmentTag, AppSchema, Piece}
import comet.ActivityCometDispatcher
import comet.ActivityStatusUpdate
import model.AssessmentRow

class NewAssessment extends SelectedMusician {
  private val log = Logger.getLogger(getClass)
  val lastPassFinder = new LastPassFinder()

  class State {
    case class Pi(piece: Piece, instId: Int, opSubInstId: Option[Int] = None)

    val opNextPi = {
      val currentGroups = GroupAssignments(opMusician.map(_.id), opSelectedTerm = Some(Terms.currentTerm)).toSeq
      val currentInstIds = currentGroups.map(_.instrument.id)

      val pi = for {
        musician      <- opMusician
        allLastPassed =  lastPassFinder.lastPassed(Some(musician.id))
        lastPass      <- allLastPassed.find(lp => currentInstIds contains lp.instrumentId)
        piece         <- Cache.pieces.find(_.id == lastPass.pieceId)
        nextPiece     =  Cache.nextPiece(piece)
      } yield Pi(nextPiece, lastPass.instrumentId, lastPass.opSubinstrumentId)

      pi orElse currentGroups.headOption.map(ga => Pi(Cache.pieces.head, ga.instrument.id))
    }

    val commentTagSelections = scala.collection.mutable.Map(Cache.tags.map(_.id -> false): _*)
    var opSelInstId = opNextPi.map(_.instId)
    var opSelSubinstId = opNextPi.flatMap(_.opSubInstId)
    var opSelPieceId = opNextPi.map(_.piece.id)
    var notes = ""

    def tempoFromPiece = opSelPieceId.flatMap(selPieceId =>
      // First look for a tempo for the specific instrument
      Cache.tempos.find(t => t.instrumentId == opSelInstId && t.pieceId == selPieceId) orElse
      Cache.tempos.find(_.pieceId == selPieceId)).map(_.tempo) | 0

    var tempo = tempoFromPiece
  }
  
  def render = {
    var s = new State()

    def jsTempo = JsRaw(s"tempoBpm = ${s.tempo}").cmd

    def tempoControl = SHtml.ajaxText(s.tempo.toString, (t) => asInt(t).map(ti => {
      s.tempo = ti
      jsTempo
    }) getOrElse Noop, "id" -> "tempo", "size" -> "3")

    def sendTempo = SetHtml("tempo", tempoControl) & jsTempo

    def selPiece = {
      val initialSel = s.opSelPieceId.map(p => Full(p.toString)) getOrElse Empty
      SHtml.ajaxSelect(Cache.pieces.map(p => p.id.toString -> p.name.get),
        initialSel, (p) => {
          s.opSelPieceId = Some(p.toInt)
          s.tempo = s.tempoFromPiece
          sendTempo
        })
    }

    def commentText = SHtml.textarea("", (n) => {
      s.notes = n
      Noop
    }, "id" -> "commentText", "rows" -> "3", "style" -> "width: 30em;", "placeholder" -> "Additional comments")

    def checkboxes(part: Int) = {
      val grouped = Cache.tags.grouped(Cache.tags.size / 2).toSeq
      <div>{grouped(part).map(tag =>
        <div class="checkbox">
          <label>
            {SHtml.checkbox(false, (checked) => s.commentTagSelections(tag.id) = checked)}{tag.commentText}
          </label>
        </div>)}
      </div>
    }

    def recordAss(pass: Boolean): JsCmd = {
      (for {
        musician  <- opMusician
        iid       <- s.opSelInstId
        pid       <- s.opSelPieceId
        user      <- AppSchema.users.find(_.login == Authenticator.userName.get)
      } yield {
        val assTime = DateTime.now
        val newAss = Assessment(
          id                = 0,
          assessment_time   = new Timestamp(assTime.getMillis),
          musician_id       = musician.id,
          instrument_id     = iid,
          subinstrument_id  = s.opSelSubinstId,
          user_id           = user.id,
          pieceId           = pid,
          pass              = pass,
          notes             = s.notes
        )
        AppSchema.assessments.insert(newAss)
        val selectedCommentIds = (for {
          (commentId, selected) <- s.commentTagSelections
          if selected
        } yield commentId).toSet
        val tags = selectedCommentIds.map(id => AssessmentTag(newAss.id, id))
        AppSchema.assessmentTags.insert(tags)
        Cache.updateLastAssTime(newAss.musician_id, assTime)
        log.info(s"Assessment: $newAss, $tags")

        def pieceNameFromId(id: Int) = Cache.pieces.find(_.id == id).map(_.name.get)

        val row = {
          val inst = s.opSelInstId.flatMap(id => Cache.instruments.find(_.id == id)).map(_.name.get)
          val subinst = s.opSelSubinstId.flatMap(id => Cache.subinstruments.values.flatten.find(_.id == id)).map(_.name.get)
          val predef = Cache.tags.filter(t => selectedCommentIds.contains(t.id)).map(_.commentText).mkString(", ")
          val expandedNotes = (if (predef.isEmpty) "" else s"$predef; ") + s.notes
          AssessmentRow(newAss.id, assTime, musician, user.last_name,
            ~s.opSelPieceId.flatMap(id => pieceNameFromId(id)),
            ~inst, subinst, pass, if (expandedNotes.isEmpty) None else Some(expandedNotes))
        }
        ActivityCometDispatcher ! ActivityStatusUpdate(row)
        val nodeSeq = Assessments.createRow(row, keepStudent = false)
        s = new State
        PrependHtml("assessmentsBody", nodeSeq) &
          SetHtml("lastPiece", Text(StudentDetails.lastPiece(lastPassFinder, musician.id))) &
          SetHtml("piece", selPiece) & sendTempo &
          SetHtml("commentText", commentText) &
          SetHtml("checkbox1", checkboxes(0)) &
          SetHtml("checkbox2", checkboxes(1)) &
          (pieceNameFromId(newAss.pieceId).map(pieceName => {
            val id = "passFailConfirmation"
            val msg = if (newAss.pass) "Passed " else "Failed "
            SetHtml(id, Text(msg + pieceName)) & JqJsCmds.Show(id) & FadeOut(id)
          }) | Noop)
      }) | Noop
    }

    val subinstId = "subinstrument"
    val initialInstrumentSel = s.opSelInstId.map(i => Full(i.toString)) getOrElse Empty

    def subinstSels(instId: Int): List[(String, String)] =
      Cache.subinstruments.get(instId).toList.flatten.map(si => si.id.toString -> si.name.get)

    def selInst = SHtml.ajaxSelect(Cache.instruments.map(i => i.id.toString -> i.name.get), initialInstrumentSel, (p) => {
      val instId = p.toInt
      s.opSelInstId = Some(instId)
      val sels = subinstSels(instId)
      sels.headOption.foreach(sel => s.opSelSubinstId = Some(sel._1.toInt))
      ReplaceOptions(subinstId, sels, Empty)
    })

    def selSubinst = {
      val opts = s.opSelInstId.map(subinstSels) getOrElse Seq[(String, String)]()
      def setSubinstId(idString: String) { s.opSelSubinstId = Some(idString.toInt) }
      opts.headOption.foreach(sel => setSubinstId(sel._1))
      SHtml.select(opts, Empty, setSubinstId)
    }

    "#instrument"     #> selInst &
    s"#$subinstId"    #> selSubinst &
    "#piece *"        #> selPiece &
    "#tempo *"        #> tempoControl &
    "#setTempo"       #> Script(jsTempo) &
    "#checkbox1 *"    #> checkboxes(0) &
    "#checkbox2 *"    #> checkboxes(1) &
    "#commentText *"  #> commentText &
    "#passButton"     #> SHtml.ajaxSubmit("Pass", () => { recordAss(pass = true ) }) &
    "#failButton"     #> SHtml.ajaxSubmit("Fail", () => { recordAss(pass = false) })
  }
}
