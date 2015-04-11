package com.dbschools.mgb
package snippet

import java.io.FileOutputStream

import net.liftweb.util.Helpers._
import net.liftweb.http.SHtml._
import net.liftweb.http.{S, FileParamHolder}
import net.liftweb.common.{Full, Empty, Box}
import org.apache.log4j.Logger
import bootstrap.liftweb.ApplicationPaths
import model.SelectedMusician

class PictureUploader extends Photos with SelectedMusician {
  val log = Logger.getLogger(getClass)

  def render = {

    var upload: Box[FileParamHolder] = Empty

    def processForm() = {
      for {
        FileParamHolder(_, mimeType, fileName, bytes) <- upload
        if fileName.split('.').last == "jpg"
        stu    <- svSelectedMusician
        permId  = stu.permStudentId.get
        p      <- paths(permId)
        absPath = p.abs
      } {
        val s = new FileOutputStream(s"$absPath/$permId.jpg")
        s.write(bytes)
        s.close()
      }

      S.redirectTo(ApplicationPaths.studentDetails.href)
    }

    "#file"       #> fileUpload(f => upload = Full(f)) &
    "type=submit" #> onSubmitUnit(processForm)
  }
}
