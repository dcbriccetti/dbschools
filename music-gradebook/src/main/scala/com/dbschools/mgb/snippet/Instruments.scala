package com.dbschools.mgb
package snippet

import xml.{NodeSeq, Text}

import org.squeryl.PrimitiveTypeMode._

import bootstrap.liftweb.ApplicationPaths._
import net.liftweb.common.Loggable
import net.liftweb.http.{RequestVar, S, SHtml}
import net.liftweb.http
import http.js.JsCmds.Noop
import net.liftweb.util.Helpers
import Helpers._

import com.dbschools.mgb.schema.{AppSchema, Instrument}
import com.dbschools.mgb.model.Cache

/** Snippet for Instrument CRUD operations. */
class Instruments extends Loggable {
  
  /** Used to pass Instrument between successive HTTP Requests.*/
  object requestData extends RequestVar[Instrument](Instrument.createRecord)
  
  def list = {
    val usedInstrumentIds = from(AppSchema.musicianGroups)(mg => select(mg.instrument_id)).distinct.toSet
    "#create [href]"    #> instrumentsCreate.href &
    ".row *"            #> from(AppSchema.instruments)(i => select(i) orderBy i.sequence.is).map(instrument => {
      ".instrumentSequence *"   #> instrument.sequence.is &
      ".instrumentName *"       #> SHtml.swappable(<span>{instrument.name.is}</span>, instrumentNameText(instrument)) &
      ".actions *"              #> actionsForInstrument(usedInstrumentIds, instrument)
    })
  }

  private def actionsForInstrument(usedInstrumentIds: Set[Int], instrument: Instrument) =
    if (!usedInstrumentIds.contains(instrument.id))
      Text(" ") ++ SHtml.link(instrumentsDelete.href, () => requestData(instrument), Text("Delete"))
    else
      NodeSeq.Empty

  private def instrumentNameText(instrument: Instrument) =
    SHtml.ajaxText(instrument.name.is,
      v => {
        if (v != instrument.name.is && v.trim.length > 0) { // TODO do this validation the right way?
          instrument.name(v)
          doSaveInstrument(doUpdateInstrument, instrument, notifyOk = false)
        }
        Noop
      }
    )

  def create = {
    val instrument = requestData.is
    "#hidden"               #> SHtml.hidden(() => requestData(instrument)) &
    "#instrumentSequence"   #> SHtml.number(requestData.is.sequence.is, (sequence: Int) => requestData.is.sequence(sequence), 0, 200) &
    "#instrumentName"       #> SHtml.text(requestData.is.name.is, name => requestData.is.name(name)) &
    "#submit"               #> SHtml.onSubmitUnit(() => doSaveInstrument(doInsertInstrument))
  }

  def delete = {
    if (!requestData.set_?) {
      logger.info("Delete Instrument page has not been reached from Instrument List page. Redirecting to List page.")
      S.redirectTo(instrumentsList.href)
    }
    
    val instrument = requestData.is

    "#instrumentName"   #> instrument.name.is &
    "#yes"              #> SHtml.link(instrumentsList.href, () => doDeleteInstrument(instrument), Text("Yes")) &
    "#no [href]"        #> instrumentsList.href
  }

  private def doSaveInstrument(predicate: (Instrument) => Unit, instrument: Instrument = requestData.is,
      notifyOk: Boolean = true): Unit = {
    instrument.validate match {
      case Nil =>
        predicate(instrument)
        Cache.invalidateInstruments()
        if (notifyOk) S.notice("Instrument has been saved")
        S.seeOther(instrumentsList.href)
      case errors =>
        S.error(errors)
    }
  }
  
  private def doInsertInstrument(instrument: Instrument): Unit = {
    AppSchema.instruments.insert(instrument)
  }

  private def doUpdateInstrument(instrument: Instrument): Unit = {
    AppSchema.instruments.update(instrument)
  }

  private def doDeleteInstrument(instrument: Instrument): Unit = {
    AppSchema.instruments.deleteWhere(ins => ins.idField.is === instrument.idField.is)
  }
}