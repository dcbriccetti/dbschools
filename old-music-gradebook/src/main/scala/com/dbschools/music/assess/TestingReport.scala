package com.dbschools.music.assess

import scala.collection.JavaConversions._
import java.text.SimpleDateFormat
import java.util.Date
import org.antlr.stringtemplate.StringTemplateGroup
import com.dbschools.music.decortrs.{RejectionDecorator, AssessmentDecorator}
import com.dbschools.music.orm.Assessment
import com.dbschools.music.orm.Musician

object TestingReport {
  def create(maInfo: Musician, groupMembershipsLabelText: String, keywordCountsAsString: String,
        selectedAssessments: java.util.Collection[Assessment]): String = {
    val group = new StringTemplateGroup("myGroup")
    val report = group.getInstanceOf("templates/report-html")
    report.setAttribute("musician", maInfo)
    report.setAttribute("groupMemberships", groupMembershipsLabelText)
    val df = new SimpleDateFormat(com.dbschools.music.decortrs.Constants.DATE_TIME_FORMAT)
    report.setAttribute("datetime", df.format(new Date))
    report.setAttribute("keywordCounts", keywordCountsAsString)
    report.setAttribute("assessments", asJavaList(selectedAssessments.toList.sortBy(_.getAssessmentTime.getTime).
      map(new AssessmentDecorator(_))))
    report.setAttribute("rejections", asJavaList(maInfo.getRejections.toList.sortBy(_.getRejectionTime.getTime).
      map(new RejectionDecorator(_))))
    report.toString
  }
}
