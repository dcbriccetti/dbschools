package com.dbschools.mgb
package snippet

import scalaz._
import Scalaz._
import net.liftweb.http.SHtml
import net.liftweb.util.Helpers
import net.liftweb.http.js.JsCmds.Replace
import org.squeryl.PrimitiveTypeMode._
import model.Cache
import schema.{Tempo, AppSchema}
import snippet.LiftExtensions._

class Tempos {
  private val selectors = svSelectors.is
  private val allInstruments = Cache.instruments.filter(_.name.get != "Unassigned").sortBy(_.sequence.get)
  selectors.opCallback = Some(() => replaceContents)

  def instrumentSelector = selectors.instrumentSelector

  def headings = {
    val colHeadings = Seq("Piece", "Default") ++ selectedInstruments.map(_.name.get)
    <tr>{colHeadings.map(ch => <th>{ch}</th>)}</tr>
  }

  def rows = Cache.pieces.map(p => {
    val temposForPiece = Cache.tempos.filter(_.pieceId == p.id).groupBy(_.instrumentId)
    val instrumentIds = None +: selectedInstruments.map(i => Some(i.id))

    def getTempo(i: Option[Int]) = ~temposForPiece.get(i).map(_.head.tempo.toString)

    val tempoCols = instrumentIds.map(iid =>
      <td> {
        val currentTempoStr = getTempo(iid)

        SHtml.ajaxText(currentTempoStr, t => {
          val newTempoStr = t.trim
          if (newTempoStr != currentTempoStr) {
            if (newTempoStr.isEmpty) {
              if (iid.nonEmpty) { // Mustn’t remove default tempo
                AppSchema.tempos.deleteWhere(t => t.pieceId === p.id and t.instrumentId === iid)
                Cache.invalidateTempos()
              }
            } else {
              Helpers.asInt(newTempoStr).foreach(newTempo => {
                if (currentTempoStr.isEmpty) {
                  AppSchema.tempos.insert(Tempo(0, p.id, iid, newTempo))
                } else {
                  AppSchema.tempos.update(t => where(t.pieceId === p.id and t.instrumentId === iid)
                    set (t.tempo := newTempo))
                }
              })
              Cache.invalidateTempos()
            }
          }
        }, "size" -> "3")
      } </td>)

    <tr><td>{p.name.get}</td>{tempoCols}</tr>
  })

  private def replaceContents = {
    val elemId = "dynamicSection"
    Replace(elemId, elemFromTemplate("tempos", s"#$elemId"))
  }

  private def selectedInstruments =
    selectors.selectedInstId.rto.map(id => allInstruments.filter(_.id == id)) | allInstruments
}
