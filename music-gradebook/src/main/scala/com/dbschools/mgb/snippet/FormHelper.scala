package com.dbschools.mgb.snippet

import scala.xml.NodeSeq
import net.liftweb.http.S
import net.liftweb.util.Helpers._

trait FormHelper {

  def addFormGroup(id: String, label: String, control: (String, String) => NodeSeq): NodeSeq = {
    S.runTemplate(List("_formGroup"), "formGroup" -> {
      "label [for]" #> id &
      "label *"     #> label &
      "#controlId"  #> control(id, label)
    }).openOrThrowException("")
  }

  def attrs(id: String, label: String) = Seq(
    "id" -> id, "class" -> "form-control", "placeholder" -> label
  )
}
