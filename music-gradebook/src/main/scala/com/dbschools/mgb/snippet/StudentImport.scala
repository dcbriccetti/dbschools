package com.dbschools.mgb
package snippet

import scalaz._
import Scalaz._
import org.apache.log4j.Logger
import net.liftweb.util.Helpers._
import net.liftweb.http.SHtml
import model.{SchoolYears, StudentImporter}
import net.liftweb.util.CssSel

/** Imports students. */
class StudentImport {
  private val log = Logger.getLogger(getClass)
  private val importYear = SchoolYears.current
  private val selectors = svSelectors.is

  def render: CssSel = {
    var data = ""

    def process(): Unit = {
      for (groupId <- selectors.selectedGroupId.rto)
        new StudentImporter().importStudents(data, groupId)
    }

    "#data"     #> SHtml.textarea("", data = _, "class" -> "form-control", "rows" -> "20") &
    "#submit"   #> SHtml.submitButton(process)
  }
}
