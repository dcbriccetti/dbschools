package com.dbschools.mgb
package snippet

import scala.xml.NodeSeq
import net.liftweb.http.S
import net.liftweb.util.Helpers._
import model.BoxOpener._

trait FormHelper {

  def addFormGroup(id: String, label: String, control: (String, String) => NodeSeq): NodeSeq = {
    S.runTemplate(List("_formGroup"), "formGroup" -> {
      "label [for]" #> id &
      "label *"     #> label &
      "#controlId"  #> control(id, label)
    }).open
  }

  def attrs(id: String, label: String) = Seq(
    "id" -> id, "class" -> "form-control", "placeholder" -> label
  )
}
