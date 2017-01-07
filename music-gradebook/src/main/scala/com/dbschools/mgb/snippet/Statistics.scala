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
import AppSchema._
import model.{Cache, SchoolYears}
import model.SchoolYears.toTs

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

  private def fromTo = selectors.selectedSchoolYear.rto.map(SchoolYears.fromToDates) | SchoolYears.allFromToDates

  def assessmentsByGroup = {
    val (dtFrom, dtTo) = fromTo
    val gps = Cache.filteredGroups(selectors.selectedSchoolYear.value.right.toOption)
    val namesToPer = gps.map(gp => gp.group.name -> gp.period).toMap
    val groupIds = gps.map(_.group.id)
    val q = join(groups, musicianGroups, musicians, assessments)((g, mg, m, a) =>
      where(
        mg.school_year === dtFrom.getYear
        and g.doesTesting === true
        and a.assessment_time.between(toTs(dtFrom), toTs(dtTo))
      )
      groupBy (g.name, a.pass)
      compute count(a.id)
      on(
        g.id === mg.group_id,
        mg.musician_id === m.musician_id.get,
        m.musician_id.get === a.musician_id
      )
    )
    case class Npf(name: String, passes: Long, failures: Long)
    val npfs = q.groupBy(_.key._1).map {case (name, gm) =>
      val (p, f) = gm.partition(_.key._2)
      Npf(name, p.head.measures, f.head.measures)
    }.toSeq.sortBy(npf => namesToPer.getOrElse(npf.name, 0))
    val passesMap   = npfs.map(npf => npf.name -> npf.passes  ).toMap
    val failuresMap = npfs.map(npf => npf.name -> npf.failures).toMap
    css("Group", npfs.map(_.name), passesMap, failuresMap)
  }

  def assessmentsByGrade  = createTable("Grade",  queryByGrade, (gradYear: String) =>
    (SchoolYears.graduationYearAsGrade(gradYear.toInt) -
    (SchoolYears.current - (selectors.selectedSchoolYear.rto | SchoolYears.current))).toString)

  def assessmentsByTester = createTable("Tester", queryByTester)

  private type QueryGm = Query[GroupWithMeasures[PrimitiveTypeMode.StringType, PrimitiveTypeMode.LongType]]

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
    css(rowHeading, (passesMap.keys.toSet ++ failuresMap.keys).toSeq.sorted, passesMap, failuresMap)
  }

  private val numf = NumberFormat.getNumberInstance
  private def fmt[A](num: A) = numf.format(num)

  private def css(rowHeading: String, groupNames: Iterable[String],
      passesMap: Map[String, Long], failuresMap: Map[String, Long]) =
    ".rowHeading *"   #> rowHeading &
    ".assessmentsRow" #> groupNames.map(x => {
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
