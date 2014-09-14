package com.dbschools.mgb.convert

import io.Source

object AddPermIds extends App {
  val path = "/Users/daveb/devel/dbschools/music-gradebook/src/main/scala/com/dbschools/mgb/convert"
  Source.fromFile(s"$path/ids.tsv").getLines().foreach(line => {
    val ids = line.split('\t')
    val permId = ids(0)
    val id = ids(1)
    println(s"update musician set perm_student_id = $permId where student_id = $id;")
  })
}