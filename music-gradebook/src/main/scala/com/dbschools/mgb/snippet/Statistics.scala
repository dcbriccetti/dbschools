package com.dbschools.mgb
package snippet

import org.squeryl.PrimitiveTypeMode._
import net.liftweb._
import util.BindHelpers._
import org.scala_tools.time.Imports._
import schema.AppSchema
import AppSchema.{groups, musicianGroups, musicians, assessments, users}
import java.sql.Timestamp
import org.squeryl.{PrimitiveTypeMode, Query}
import org.squeryl.dsl.GroupWithMeasures

class Statistics {

  private val dtFrom = {
    val n = DateTime.now.withDayOfMonth(1)
    val yearEndMonth = 7
    if (n.getMonthOfYear >= yearEndMonth) n.withMonth(yearEndMonth) else n.withMonth(yearEndMonth).minus(1.year)
  }
  private val dtTo = dtFrom + 1.year

  def assessmentsByGroup  = createTable("Group", queryByGroup)
  def assessmentsByTester = createTable("Tester", queryByTester)

  private def toTs(dt: DateTime) = new Timestamp(dt.millis)

  private type QueryGm = Query[GroupWithMeasures[PrimitiveTypeMode.StringType, PrimitiveTypeMode.LongType]]

  private def queryByGroup(pass: Boolean) = from(groups, musicianGroups, musicians, assessments)((g, mg, m, a) =>
    where(g.group_id === mg.group_id and mg.musician_id === m.musician_id and mg.school_year === dtFrom.getYear
      and m.musician_id === a.musician_id
      and a.pass === pass and a.assessment_time.between(toTs(dtFrom), toTs(dtTo)))
    groupBy(g.name)
    compute(count(a.assessment_id))
  )

  private def queryByTester(pass: Boolean) = from(users, assessments)((u, a) =>
    where(u.id === a.user_id and a.pass === pass and a.assessment_time.between(toTs(dtFrom), toTs(dtTo)))
    groupBy(u.last_name)
    compute(count(a.assessment_id))
  )

  private def createTable(heading: String, query: (Boolean => QueryGm)) = {
    def queryToMap(query: QueryGm) = query.map(gm => gm.key -> gm.measures).toMap
    val passesMap   = queryToMap(query(true))
    val failuresMap = queryToMap(query(false))
    css(heading, passesMap.keys.toSet ++ failuresMap.keys.toSet, passesMap, failuresMap)
  }

  private def css(rowHeading: String, groupNames: Iterable[String],
      passesMap: Map[String, Long], failuresMap: Map[String, Long]) =
    ".rowHeading *"   #> rowHeading &
    ".assessmentsRow" #> groupNames.toSeq.sorted.map(x => {
      val passes = passesMap(x)
      val failures = failuresMap(x)
      val total = passes + failures
      ".rowName    *" #> x &
      ".asses      *" #> total &
      ".pass       *" #> passes &
      ".fail       *" #> failures &
      ".pctPass    *" #> "%.2f".format(passes * 100.0 / total)
    })
}