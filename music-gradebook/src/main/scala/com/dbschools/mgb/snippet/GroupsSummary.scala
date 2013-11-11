package com.dbschools.mgb
package snippet

import org.squeryl.PrimitiveTypeMode._
import net.liftweb.util.Helpers._
import schema.AppSchema
import com.dbschools.mgb.model.{Cache, Terms}

class GroupsSummary {
  def render = {
    case class Count(groupId: Int, instrumentId: Int, count: Long)
    val q = from(AppSchema.musicianGroups)(mg =>
      where(mg.school_year === Terms.currentTerm)
      groupBy(mg.group_id, mg.instrument_id)
      compute count(mg.instrument_id)
    )
    val counts = q.map(g => Count(g.key._1, g.key._2, g.measures))
    val countsByGroup = counts.groupBy(_.groupId)
    val groups = Cache.filteredGroups().sortBy(_.name)
    val usedInstruments = {
      val usedInstIds = counts.map(_.instrumentId).toSet
      Cache.instruments.filter(usedInstIds contains _.id).sortBy(_.sequence.get)
    }

    case class InstrumentCounts(name: String, counts: Seq[Long])

    val instRows = usedInstruments.map(inst => {
      InstrumentCounts(inst.name.get, groups.map(group => {
        (for {
          gicsForGroup  <- countsByGroup.get(group.id)
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
        <th>{iRow.name}</th>
        {iRow.counts.map(count => <td class="alignRight">
          {count match {
            case 0 => ""
            case n => n.toString
          }}
          </td>)}
        <th class="alignRight">{iRow.counts.sum}</th>
      </tr>
    )

    def totalsRow =
      <tr>
        <th>Totals</th>{totals.map(t => <th class="alignRight"> {t} </th>)}
        <th class="alignRight">{totals.sum}</th>
      </tr>

    "#heading" #> <tr> <th>Instrument</th>{groups.map(g => <th>{g.name}</th>)} <td>Totals</td></tr> &
    "#detail" #> detailRows &
    "#totals" #> totalsRow
  }
}
