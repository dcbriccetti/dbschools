package com.dbschools.mgb
package snippet

import java.sql.Timestamp
import org.squeryl.PrimitiveTypeMode._
import org.scala_tools.time.Imports._
import net.liftweb.util.Helpers._
import schema.{User, AppSchema}
import model.Stats

class AssessmentsSummary {

  def render = {
    case class DayAndTester(date: DateTime, tester: User)
    val testersById = AppSchema.users.map(u => u.id -> u).toMap
    val query = AppSchema.assessments.where(
      _.assessment_time > new Timestamp(DateTime.now.minusDays(60).millis))
    val testerDays = query.groupBy(a => DayAndTester(
      new DateTime(a.assessment_time.getTime).withTimeAtStartOfDay, testersById(a.user_id)))
    val sortedTesterDays = testerDays.toSeq.sortBy(dt => (-dt._1.date.millis, dt._1.tester.last_name))
    val dtf = DateTimeFormat.mediumDate()

    "#asmtsSumRow *"  #> sortedTesterDays.map {
      case (td, asmts) =>
        val byStudent = asmts.groupBy(_.musician_id)
        val numStudents = byStudent.size
        val meanPerStu = asmts.size / numStudents.toFloat
        val asmtCounts = byStudent.values.map(_.size.toDouble)
      
        "#asrDate *"      #> dtf.print(td.date) &
        "#asrTester *"    #> td.tester.last_name &
        "#asrStudents *"  #> numStudents &
        "#asrAsmts *"     #> asmts.size &
        "#asrAvgAS *"     #> f"$meanPerStu%.2f" &
        "#asrStDevAS *"   #> f"${Stats.stdev(asmtCounts, meanPerStu)}%.2f"
    }
  }
}
