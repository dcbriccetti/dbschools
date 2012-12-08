package com.dbschools.mgb

import math.random
import org.squeryl.PrimitiveTypeMode._
import schema.{MusicianGroup, Musician, AppSchema, User}
import net.liftweb.common.Loggable
import net.liftweb.util.Props
import java.sql.SQLException

object TestDataMaker extends Loggable {

  def getNames(names: String): Array[String] = names.split("\\W").map(_.capitalize)

  private val lastNames = getNames(
"""smith
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
davies""")
  private val firstNames = getNames(
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
cindy""")

  def main(args: Array[String]) {
    Db.initialize()
    createTestData()
  }

  def createTestData() {
    transaction {
      deleteStudents()
      createAndGroupStudents()
    }
  }

  private def deleteStudents() {
    AppSchema.musicianGroups  .deleteWhere(mg => mg.id === mg.id)
    AppSchema.assessmentTags  .deleteWhere(a => a.assessmentId === a.assessmentId)
    AppSchema.assessments     .deleteWhere(a => a.assessment_id === a.assessment_id)
    AppSchema.musicians       .deleteWhere(m => m.idField.is === m.idField.is)
  }
  
  private def createAndGroupStudents() {
    val instrumentIds = AppSchema.instruments.map(_.idField.get).toArray
    var id = 10000 // Until we set up sequences
    lastNames.foreach(lastName => {
      val firstName = firstNames((random * firstNames.length).toInt)
      val m = AppSchema.musicians.insert(Musician.createRecord.student_id(id).first_name(firstName).
        last_name(lastName).graduation_year(2013).sex("M"))
      id += 1

      val groupIds = random match {
        case n if n < .1  => GroupIds(0)
        case n if n < .2  => GroupIds(2)
        case n if n < .25 => GroupIds(3)
        case _            => GroupIds(1)
      }
      groupIds.foreach(groupId => {
        AppSchema.musicianGroups.insert(MusicianGroup(id, m.musician_id.is, groupId,
          instrumentIds((random * instrumentIds.length).toInt), 2013))
        id += 1
      })
      logger.trace("Added %s, %s to %d groups".format(lastName, firstName, groupIds.size))
    })
  }

  private object GroupIds {
    private val groupIds = AppSchema.groups.map(_.group_id).toArray

    /** Returns the requested number of unique, randomly-chosen group IDs */
    def apply(num: Int) = {
      var ids = Set[Int]()
      if(!groupIds.isEmpty){
        while(ids.size < num)
          ids += groupIds((random * groupIds.length).toInt)
        }
      ids
    }
  }
  
  /** Initializes the music_user table with a demo user.*/
  def createDefaultUserData() {
    def prop(name: String) = Props.get(name).get
    transaction {
      try {
        AppSchema.users.insert(
          User(
            1,
            prop("data.users.demo.login"),
            "",
            prop("data.users.demo.epassword"),
            prop("data.users.demo.name.first"),
            prop("data.users.demo.name.last"),
            enabled = true))
        logger.info("Added a demo user")
      }
      catch {
        case exception: SQLException ⇒ {
          // Only warn about the problem. Failing to add a default user is not considered fatal.
          logger.warn("Failed to add a test user.", exception)
        }
      }
    }
  }
}