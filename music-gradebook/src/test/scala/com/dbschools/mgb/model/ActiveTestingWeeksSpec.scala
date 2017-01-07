package com.dbschools.mgb.model

import org.joda.time.format.ISODateTimeFormat.dateTimeParser
import org.scalatest.{FunSpec, Matchers}

class ActiveTestingWeeksSpec extends FunSpec with Matchers {

  private val parser = dateTimeParser

  describe("ActiveTestingWeeks") {

    it("should collect the right data for a set of dates") {
      val atw = new ActiveTestingWeeks()
      val schoolYearStart = parser.parseDateTime("2016-08-22")
      val testDates = Seq(
        "2016-08-22",
        "2016-08-28",
        "2016-08-29"
      ).map(parser.parseDateTime)
      val mondayWeek2 = testDates(2)

      atw.addFrom(testDates, schoolYearStart)

      atw.forSchoolYear(2016) shouldEqual Seq(1, 2)
      atw.forSchoolTerm(2016, schoolYearStart, schoolYearStart) shouldEqual Seq(1, 2)
      atw.forSchoolTerm(2016, schoolYearStart, mondayWeek2) shouldEqual Seq(2)
    }
  }
}
