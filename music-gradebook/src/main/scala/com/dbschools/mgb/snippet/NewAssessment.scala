package com.dbschools.mgb
package snippet

import java.sql.Timestamp
import scala.xml.{Elem, Text}
import scalaz._
import Scalaz._
import org.squeryl.PrimitiveTypeMode._
import org.scala_tools.time.Imports._
import net.liftweb.util.Helpers._
import net.liftweb.http.SHtml
import net.liftweb.common.{Empty, Full}
import net.liftweb.http.js.JsCmds.{SetHtml, Noop, Reload, ReplaceOptions, Script}
import net.liftweb.http.js.JE.JsRaw
import model.{Cache, GroupAssignments, LastPassFinder, Terms}
import schema.{Assessment, AssessmentTag, AppSchema, Piece}

class NewAssessment extends MusicianFromReq {
  def render = {
    val lastPassFinder = new LastPassFinder()

    case class Pi(piece: Piece, instId: Int, opSubInstId: Option[Int] = None)

    val opNextPi = {
      val pi = for {
        musician  <- opMusician
        lastPass  <- lastPassFinder.lastPassed(Some(musician.musician_id.get)).headOption
        piece     <- Cache.pieces.find(_.id == lastPass.pieceId)
        nextPiece =  lastPassFinder.next(piece)
      } yield Pi(nextPiece, lastPass.instrumentId, lastPass.opSubinstrumentId)

      pi orElse GroupAssignments(opMusician.map(_.id), opSelectedTerm = Some(Terms.currentTerm)).headOption.map(ga =>
        Pi(Cache.pieces.head, ga.instrument.id))
    }

    val commentTagSelections = scala.collection.mutable.Map(Cache.tags.map(_.id -> false): _*)
    var opSelInstId = opNextPi.map(_.instId)
    var opSelSubinstId = opNextPi.flatMap(_.opSubInstId)
    var opSelPieceId = opNextPi.map(_.piece.id)
    var notes = ""

    def findTempo = opSelPieceId.flatMap(selPieceId =>
      // First look for a tempo for the specific instrument
      Cache.tempos.find(t => t.instrumentId == opSelInstId && t.pieceId == selPieceId) orElse
      Cache.tempos.find(_.pieceId == selPieceId))

    def recordAss(pass: Boolean): Unit = {
      for {
        musician  <- opMusician
        iid       <- opSelInstId
        pid       <- opSelPieceId
        user      <- AppSchema.users.find(_.login == Authenticator.userName.get)
      } {
        val newAss = Assessment(
          id                = 0,
          assessment_time   = new Timestamp(DateTime.now.getMillis),
          musician_id       = musician.musician_id.get,
          instrument_id     = iid,
          subinstrument_id  = opSelSubinstId,
          user_id           = user.id,
          pieceId           = pid,
          pass              = pass,
          notes             = notes
        )
        AppSchema.assessments.insert(newAss)
        AppSchema.assessmentTags.insert(
          for {
            (commentId, selected) <- commentTagSelections
            if selected
          } yield AssessmentTag(newAss.id, commentId)
        )
      }
    }

    val subinstId = "subinstrument"
    val initialInstrumentSel = opSelInstId.map(i => Full(i.toString)) getOrElse Empty

    def subinstSels(instId: Int): List[(String, String)] =
      Cache.subinstruments.get(instId).toList.flatten.map(si => si.id.toString -> si.name.get)

    def selInst = SHtml.ajaxSelect(Cache.instruments.map(i => i.id.toString -> i.name.get), initialInstrumentSel, (p) => {
      val instId = p.toInt
      opSelInstId = Some(instId)
      val sels = subinstSels(instId)
      sels.headOption.foreach(sel => opSelSubinstId = Some(sel._1.toInt))
      ReplaceOptions(subinstId, sels, Empty)
    })

    def selSubinst = {
      val opts = opSelInstId.map(subinstSels) getOrElse Seq[(String, String)]()
      def setSubinstId(idString: String) { opSelSubinstId = Some(idString.toInt) }
      opts.headOption.foreach(sel => setSubinstId(sel._1))
      SHtml.select(opts, Empty, setSubinstId)
    }

    def setJsTempo(t: Option[Int]) = JsRaw(s"tempoBpm = ${~t}").cmd

    def selPiece = {
      val initialSel = opSelPieceId.map(p => Full(p.toString)) getOrElse Empty
      SHtml.ajaxSelect(Cache.pieces.map(p => p.id.toString -> p.name.get),
        initialSel, (p) => {
          opSelPieceId = Some(p.toInt)
          val t = findTempo.map(_.tempo)
          SetHtml("tempo", Text(~t.map(_.toString))) & setJsTempo(t)
        })
    }

    def checkboxes: Seq[Elem] =
      Cache.tags.map(tag =>
        <span>
          {SHtml.checkbox(false, (checked) => commentTagSelections(tag.id) = checked)}{tag.commentText}
        </span>)

    def commentText = SHtml.textarea("", (s) => {
      notes = s
      Noop
    }, "id" -> "commentText", "rows" -> "3", "style" -> "width: 30em;")

    "#instrument"     #> selInst &
    s"#$subinstId"  #> selSubinst &
    "#piece"          #> selPiece &
    "#tempo *"        #> ~findTempo.map(_.tempo.toString) &
    "#setTempo"       #> Script(setJsTempo(findTempo.map(_.tempo))) &
    "#checkbox *"     #> checkboxes &
    "#commentText"    #> commentText &
    "#passButton"     #> SHtml.ajaxSubmit("Pass", () => { recordAss(pass = true ); Reload }) &
    "#failButton"     #> SHtml.ajaxSubmit("Fail", () => { recordAss(pass = false); Reload })
  }
}
