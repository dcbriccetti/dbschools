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
import net.liftweb.http.js.JsCmds.{SetHtml, Noop, Reload}
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

    def selInst = {
      val initialSel = opSelInstId.map(i => Full(i.toString)) getOrElse Empty
      SHtml.ajaxSelect(Cache.instruments.map(p => p.id.toString -> p.name.get),
        initialSel, (p) => {
          opSelInstId = Some(p.toInt)
          Noop
        })
    }

    def selSubinst = {
      SHtml.ajaxSelect(Seq[(String, String)](), Empty, (p) => {
        opSelSubinstId = Some(p.toInt)
        Noop
      })
    }

    def selPiece = {
      val initialSel = opSelPieceId.map(p => Full(p.toString)) getOrElse Empty
      SHtml.ajaxSelect(Cache.pieces.map(p => p.id.toString -> p.name.get),
        initialSel, (p) => {
          opSelPieceId = Some(p.toInt)
          SetHtml("tempo", Text(~findTempo.map(_.tempo.toString)))
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
    }, "id" -> "commentText", "rows" -> "3")

    "#instrument"     #> selInst &
    "#subinstrument"  #> selSubinst &
    "#piece"          #> selPiece &
    "#tempo *"        #> ~findTempo.map(_.tempo.toString) &
    "#checkbox *"     #> checkboxes &
    "#commentText"    #> commentText &
    "#passButton"     #> SHtml.ajaxSubmit("Pass", () => { recordAss(pass = true ); Reload }) &
    "#failButton"     #> SHtml.ajaxSubmit("Fail", () => { recordAss(pass = false); Reload })
  }
}
