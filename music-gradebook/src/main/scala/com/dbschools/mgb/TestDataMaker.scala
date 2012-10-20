package com.dbschools.mgb

import math.random
import org.squeryl.PrimitiveTypeMode._
import schema.{MusicianGroup, Musician, AppSchema}

object TestDataMaker {

  val names = List("""
smith
jones
johnson
lee
brown
williams
rodriguez
garcia
gonzalez
lopez
martinez
martin
perez
miller
taylor
thomas
wilson
davis
khan
ali
singh
sanchez
anderson
hernandez
chan
ahmed
white
wong
thompson
jackson
kumar
moore
gomez
king
diaz
fernandez
walker
harris
james
green
lewis
torres
robinson
clark
roberts
ramirez
young
scott
tan
chen
hall
wright
evans
adams
allen
hill
sharma
patel
baker
wang
rossi
li
campbell
rivera
edwards
murphy
parker
kelly
kim
turner
mitchell
mohamed
carter
phillips
collins
alvarez
morris
morgan
kaya
wood
nelson
cooper
cruz
stewart
morales
flores
ng
hansen
demir
gutierrez
lim
bell
reyes
can
nguyen
silva
hughes
ruiz
shah
davies""",
"""john
david
michael
chris
mike
mark
paul
daniel
james
maria
sarah
laura
robert
lisa
jennifer
andrea
steve
peter
kevin
jason
jessica
michelle
karen
joe
brian
alex
richard
linda
julie
anna
andrew
mary
eric
sandra
tom
stephanie
thomas
sara
martin
scott
jean
susan
matt
jim
amanda
marie
ali
tony
melissa
carlos
jeff
marco
amy
ryan
bob
dave
angela
kim
kelly
patricia
jose
anthony
nicole
tim
barbara
adam
dan
christine
sam
patrick
anne
steven
bill
jonathan
george
nick
matthew
ben
andy
william
sharon
ashley
elizabeth
nancy
antonio
rachel
ahmed
mohamed
stephen
gary
juan
jack
debbie
claudia
monica
heather
christian
luis
carol
cindy""").map(_.split("\\W").map(_.capitalize))

  def make() {
    transaction {
      val instrumentIds = AppSchema.instruments.map(_.instrument_id).toArray
      val groupIds = AppSchema.groups.map(_.group_id).toArray
      var id = 10000 // Until we set up sequences
      names(0).foreach(lastName => {
        val firstName = names(1)((random * names(1).length).toInt)
        val m = AppSchema.musicians.insert(Musician(id, id, firstName, lastName, 2013))
        id += 1
        AppSchema.musicianGroups.insert(MusicianGroup(id, m.musician_id,
          groupIds((random * groupIds.length).toInt),
          instrumentIds((random * instrumentIds.length).toInt), 2013))
        println(lastName + ", " + firstName)
      })
    }
  }
}