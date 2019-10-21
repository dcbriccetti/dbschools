package com.dbschools.perclock.model

object TeacherSettings {
  def defaultFillColors(numPeriods: Int): Seq[String] = {
    val gradation = 340.0 / numPeriods
    (for {
      i <- 0 until numPeriods
    } yield s"hsl(${i * gradation}, 90%, 70%)")
  }

  private def spl(s: String) = s.split(" *\\| *")

  val MusicSettings: (Seq[String], Seq[String]) = (
    spl("Bronze |Silver|Cadet |Symph.|Chorus |Strings|Orch."),
    "#CD7F32 silver lightblue gold   yellow  lightcyan green".split(" +")
    )
  val PredefinedSettings: Map[String, (Seq[String], Seq[String])] = Map(
    "bathayde" -> MusicSettings,
    "lmcnulty" -> MusicSettings,
    "cwindfuh" -> (spl("German 1B|German 1A|Wheel 6|Wheel 6|French 1A|French 1A|7"), defaultFillColors(11))
  )
}
