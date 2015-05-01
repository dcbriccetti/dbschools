package com.dbschools.mgb
package snippet

import scala.xml.Text
import scalaz._
import Scalaz._
import org.squeryl.PrimitiveTypeMode._
import net.liftweb.util.Helpers._
import net.liftweb.http.SHtml
import net.liftweb.http.js.JsCmds.Replace
import bootstrap.liftweb.ApplicationPaths
import schema.{Instrument, AppSchema}
import model.{Cache, Terms}
import snippet.LiftExtensions._

class GroupsSummary {
  private val href = ApplicationPaths.students.href
  private val selectors = svSelectors.is

  private def replaceContents = {
    val elemId = "dynamicSection"
    Replace(elemId, elemFromTemplate("groups", s"#$elemId"))
  }

  selectors.opCallback = Some(() => replaceContents)
  def yearSelector = selectors.yearSelector

  def render = {
    import Selectors._
    case class Count(groupId: Int, instrumentId: Int, count: Long)
    val year = rto(selectors.selectedTerm) | Terms.currentTerm
    val q = from(AppSchema.musicianGroups)(mg =>
      where(mg.school_year === year)
      groupBy(mg.group_id, mg.instrument_id)
      compute count(mg.instrument_id)
    )
    val counts = q.map(g => Count(g.key._1, g.key._2, g.measures))
    val countsByGroup = counts.groupBy(_.groupId)
    val groupPeriods = Cache.filteredGroups(rto(selectors.selectedTerm))
    val usedInstruments = {
      val usedInstIds = counts.map(_.instrumentId).toSet
      Cache.instruments.filter(usedInstIds contains _.id).sortBy(_.sequence.get)
    }

    case class InstrumentCounts(instrument: Instrument, counts: Seq[Long])

    val instRows = usedInstruments.map(inst => {
      InstrumentCounts(inst, groupPeriods.map(gp => {
        (for {
          gicsForGroup  <- countsByGroup.get(gp.group.id)
          gicInst       <- gicsForGroup.find(_.instrumentId == inst.id)
        } yield gicInst.count) getOrElse 0L
      }))
    })
    val totals = if (instRows.isEmpty) Seq[Long]() else
      for {
        col <- 0 until instRows(0).counts.size
        colNums = (0 until instRows.size).map(r => instRows(r).counts(col))
      } yield colNums.sum

    def detailRows = instRows.map(iRow =>
      <tr>
        <th>{
          SHtml.link(href, () => {
            selectors.selectedGroupId = Left(true)
            selectors.selectedInstId = Right(iRow.instrument.id)
            }, Text(iRow.instrument.name.get))
        }</th>
        {(0 until groupPeriods.size).map(g =>
          <td class="alignRight">
            {iRow.counts(g) match {
              case 0 => ""
              case n =>
                SHtml.link(href, () => {
                  selectors.selectedGroupId = Right(groupPeriods(g).group.id)
                  selectors.selectedInstId = Right(iRow.instrument.id)
                }, Text(n.toString))
            }}
          </td>
        )}
        <th class="alignRight">{iRow.counts.sum}</th>
      </tr>
    )

    def totalsRow =
      <tr>
        <th>Totals</th>{totals.map(t => <th class="alignRight"> {t} </th>)}
        <th class="alignRight">{SHtml.link(href, () => {
          selectors.selectedGroupId = Left(true)
          selectors.selectedInstId = Left(true)
          }, Text(totals.sum.toString))
        }</th>
      </tr>

    def heading = {
      <tr>
        <th>Instrument</th>{groupPeriods.map(gp => <th>
        {
        SHtml.link(href, () => {
          selectors.selectedGroupId = Right(gp.group.id)
          selectors.selectedInstId = Left(true)
          }, Text(gp.group.shortName | gp.group.name))
        }
      </th>)}<td>Totals</td>
      </tr>
    }

    "#heading"  #> heading &
    "#detail"   #> detailRows &
    "#totals"   #> totalsRow
  }
}
