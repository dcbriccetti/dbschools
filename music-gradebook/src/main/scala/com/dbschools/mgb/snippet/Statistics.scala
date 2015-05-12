package com.dbschools.mgb
package snippet

import java.text.NumberFormat
import scalaz._
import Scalaz._
import org.squeryl.{PrimitiveTypeMode, Query}
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.dsl.GroupWithMeasures
import net.liftweb._
import util.BindHelpers._
import net.liftweb.http.Templates
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.JsCmds.Replace
import net.liftweb.common.Loggable
import schema.AppSchema
import AppSchema.{groups, musicianGroups, musicians, assessments, users}
import model.Terms
import model.Terms.toTs

class Statistics extends Loggable {

  private val selectors = new Selectors(Some(() => replaceContents))

  private def replaceContents = {
    val template = "_statsData"
    Templates(List(template)).map(Replace("statsData", _)) openOr {
      logger.error("Error loading template " + template)
      Noop
    }
  }

  def yearSelector = selectors.yearSelector

  private def fromTo = selectors.selectedTerm.rto.map(Terms.termFromTo) | Terms.allTermsFromTo

  def assessmentsByGroup  = createTable("Group",  queryByGroup)
  def assessmentsByGrade  = createTable("Grade",  queryByGrade, (gradYear: String) =>
    (Terms.graduationYearAsGrade(gradYear.toInt) -
    (Terms.currentTerm - (selectors.selectedTerm.rto | Terms.currentTerm))).toString)
  def assessmentsByTester = createTable("Tester", queryByTester)

  private type QueryGm = Query[GroupWithMeasures[PrimitiveTypeMode.StringType, PrimitiveTypeMode.LongType]]

  private def queryByGroup(pass: Boolean) = {
    val (dtFrom, dtTo) = fromTo
    from(groups, musicianGroups, musicians, assessments)((g, mg, m, a) =>
      where(g.id === mg.group_id and mg.musician_id === m.musician_id.get and mg.school_year === dtFrom.getYear
        and m.musician_id.get === a.musician_id
        and a.pass === pass and a.assessment_time.between(toTs(dtFrom), toTs(dtTo)))
        groupBy g.name
        compute count(a.id)
    )
  }

  private def queryByGrade(pass: Boolean) = {
    val (dtFrom, dtTo) = fromTo
    from(musicians, assessments)((m, a) =>
      where(m.musician_id.get === a.musician_id and a.pass === pass and a.assessment_time.between(toTs(dtFrom), toTs(dtTo)))
        groupBy m.graduation_year.get.toString
        compute count(a.id)
    )
  }

  private def queryByTester(pass: Boolean) = {
    val (dtFrom, dtTo) = fromTo
    from(users, assessments)((u, a) =>
      where(u.id === a.user_id and a.pass === pass and a.assessment_time.between(toTs(dtFrom), toTs(dtTo)))
        groupBy u.last_name
        compute count(a.id)
    )
  }

  private def identityKeyTransformer(key: String) = key

  private def createTable(
    rowHeading:     String,
    query:          Boolean => QueryGm,
    keyTransformer: String => String = identityKeyTransformer
  ) = {
    def queryToMap(query: QueryGm) = query.map(gm => keyTransformer(gm.key) -> gm.measures).toMap
    val passesMap   = queryToMap(query(true))
    val failuresMap = queryToMap(query(false))
    css(rowHeading, passesMap.keys.toSet ++ failuresMap.keys.toSet, passesMap, failuresMap)
  }

  private val numf = NumberFormat.getNumberInstance
  private def fmt[A](num: A) = numf.format(num)

  private def css(rowHeading: String, groupNames: Iterable[String],
      passesMap: Map[String, Long], failuresMap: Map[String, Long]) =
    ".rowHeading *"   #> rowHeading &
    ".assessmentsRow" #> groupNames.toSeq.sorted.map(x => {
      val passes = passesMap.getOrElse(x, 0L)
      val failures = failuresMap.getOrElse(x, 0L)
      val total = passes + failures
      ".rowName    *" #> x &
      ".asses      *" #> fmt(total) &
      ".pass       *" #> fmt(passes) &
      ".fail       *" #> fmt(failures) &
      ".pctPass    *" #> f"${passes * 100.0 / total}%.2f"
    })
}
