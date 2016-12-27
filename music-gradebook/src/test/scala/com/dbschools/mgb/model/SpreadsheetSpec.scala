package com.dbschools.mgb.model

import com.norbitltd.spoiwo.model.{Row, Sheet}
import com.norbitltd.spoiwo.natures.xlsx.Model2XlsxConversions._
import org.scalatest.{FunSpec, Matchers}

class SpreadsheetSpec extends FunSpec with Matchers {

  describe("Spreadsheet") {
    it("should make one") {
      val helloWorldSheet = Sheet(name = "Hello Sheet",
        row = Row().withCellValues("Hello World!")
      )
      helloWorldSheet.saveAsXlsx("/tmp/hello_world.xlsx")
    }
  }
}
