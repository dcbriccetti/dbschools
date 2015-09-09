package com.dbschools.perclock.model

object TeacherSettings {
  val DefaultPeriodNames = "1    |2     |3     |4    |5    |6     |7"
  val DefaultFillColors  = "red   orange yellow green blue  indigo violet"
  val DefaultTextColors  = "white black  black  white white white  black"

  val DefaultSettings = (DefaultPeriodNames, DefaultFillColors, DefaultTextColors)
  val MusicSettings = (
    "Silver|Bronze |Gold |Cadet 4|Cadet 5|Orchestra|Chorus/Str.",
    "silver #CD7F32 gold  indigo  blue    green     yellow",
    "black  black   black white   white   white     black"
    )
  val PredefinedSettings = Map(
    "bathayde" -> MusicSettings,
    "lmcnulty" -> MusicSettings,
    "cwindfuh" -> ("German 1B|German 1A|Wheel 6|Wheel 6|French 1A|French 1A|7", DefaultFillColors, DefaultTextColors)
  )

}
