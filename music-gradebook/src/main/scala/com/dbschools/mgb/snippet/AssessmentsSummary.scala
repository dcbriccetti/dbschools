package com.dbschools.mgb
package snippet

import java.sql.Timestamp
import org.squeryl.PrimitiveTypeMode._
import org.scala_tools.time.Imports._
import net.liftweb.util.Helpers._
import com.dbschools.mgb.schema.{User, AppSchema}

class AssessmentsSummary {

  def render = {
    case class DayTester(date: DateTime, tester: User)
    val testersById = AppSchema.users.map(u => u.id -> u).toMap
    val query = from(AppSchema.assessments)(a =>
      where(a.assessment_time > new Timestamp(DateTime.now.minusDays(60).millis))
      select a)
    val testerDays = query.groupBy(a => DayTester(
      new DateTime(a.assessment_time.getTime).withTimeAtStartOfDay, testersById(a.user_id)))
    val sortedTesterDays = testerDays.toSeq.sortBy(dt => (-dt._1.date.millis, dt._1.tester.last_name))
    val dtf = DateTimeFormat.mediumDate

    "#asmtsSumRow *"  #> sortedTesterDays.map {
      case (td, asmts) =>
        val distinctStudents = asmts.map(_.musician_id).toSet.size
        "#asrDate *"      #> dtf.print(td.date) &
        "#asrTester *"    #> td.tester.last_name &
        "#asrStudents *"  #> distinctStudents &
        "#asrAsmts *"     #> asmts.size &
        "#asrAvgAS *"     #> f"${asmts.size / distinctStudents.toFloat}%.2f"
    }
  }
}
