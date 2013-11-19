package com.dbschools.mgb.snippet

import net.liftweb.util.PassThru
import net.liftweb.util.Helpers._
import com.dbschools.mgb.model.SelectedMusician

class NextStudent extends SelectedMusician {

  def render = {
    val groupAssignments = svGroupAssignments.is
    val groupNames = groupAssignments.map(_.group.name)
    val instNames  = groupAssignments.map(_.instrument.name.get)

    def uniqueOrEmpty(names: Iterable[String]) = names.toSet.toList match {case one :: Nil => s"$one " case _ => ""}

    case class NextInfo(stuLink: xml.Elem, index: Int, count: Int)
    val opNextInfo = for {
      m <- opMusician
      idxThis = groupAssignments.indexWhere(_.musician == m)
      if idxThis >= 0
      idxNext = idxThis + 1
      if idxNext < groupAssignments.size
    } yield NextInfo(Testing.studentNameLink(groupAssignments(idxNext).musician, test = false), idxNext, groupAssignments.size)

    opNextInfo.map(ni =>
      "#snGroup *"      #> uniqueOrEmpty(groupNames) &
      "#snInstrument *" #> uniqueOrEmpty(instNames) &
      "#snLink *"       #> ni.stuLink
    ) getOrElse PassThru
  }
}
