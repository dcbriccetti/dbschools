package com.dbschools.mgb.model

import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.{FunSpec, Matchers}
import com.dbschools.mgb.model.Periods.{InSpecialSchedule, NotInPeriod, Period}
import org.joda.time.format.ISODateTimeFormat

class PeriodsSpec extends FunSpec with Matchers with TableDrivenPropertyChecks {
  describe("Periods") {
    it("should find the right periods for various values") {
      val table = Table(
        ("DateTime", "Period"),
        ("2016-12-01T00:00:00", None: Option[Int]),
        ("2016-12-05T08:15:00", Some(1)),
        ("2016-12-28T13:48:00", Some(6)),
        ("2017-01-11T13:48:00", Some(5))
      )
      forAll (table) { (dateTimeString: String, opPeriod: Option[Int]) =>
        val dateTime = ISODateTimeFormat.dateTimeParser.parseDateTime(dateTimeString)
        println(dateTime)
        val timeClass = Periods.periodWithin(dateTime)
        timeClass match {
          case NotInPeriod | InSpecialSchedule => opPeriod shouldBe None
          case Period(p, _, _) => opPeriod shouldBe Some(p)
        }
      }
    }
  }
}
