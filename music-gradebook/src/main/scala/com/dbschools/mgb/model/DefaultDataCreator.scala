package com.dbschools.mgb.model

import io.Source
import org.squeryl.PrimitiveTypeMode._
import net.liftweb.common.Loggable
import com.dbschools.mgb.schema.{User, Subinstrument, Instrument, AppSchema, PredefinedComment}
import com.dbschools.mgb.convert.EncryptPasswords

object DefaultDataCreator extends Loggable {
  def createIfEmpty() {
    transaction {
      if (AppSchema.users.headOption.isEmpty) {
        createUser()
        createInstruments()
          /*
        createGroups()
        createPieces()
        createRejectionReasons()
        */
        createPredefinedComments()
      }
    }
  }

  private def getLines(namePart: String) =
    Source.fromInputStream(classOf[PredefinedComment].getResourceAsStream("/data/%s.txt".format(namePart))).
      getLines().map(_.trim).filterNot(l => l.isEmpty || l.startsWith("#")).toList

  private def createPredefinedComments() {
    val comments = getLines("PredefinedComment").map(PredefinedComment(0, _, ""))
    AppSchema.predefinedComments.insert(comments)
  }

  private def createUser() {
    AppSchema.users.insert(getLines("User").map(line => {
      val fields = line.split("\t")
      User(0, fields(0), fields(1), EncryptPasswords.encrypt(fields(1)), fields(2), fields(3), enabled = true)
    }))
  }

  private def createInstruments() {
    var seq = 10
    getLines("Instrument").foreach(line => {
      val fields = line.split("\t")
      val inst = Instrument.createRecord.name(fields(0)).sequence(seq)
      AppSchema.instruments.insert(inst)
      seq += 10
      if (fields.length > 1) {
        var subSeq = 10
        fields.toList.tail.foreach(field => {
          AppSchema.subinstruments.insert(Subinstrument.createRecord.instrumentId(inst.id).sequence(subSeq).name(field))
          subSeq += 10
        })
      }
    })
  }
/*
  private def createGroups() {
    import scala.collection.JavaConversions._
    for (line <- new FileLineProvider(DATA_PATH + "Group.txt")) {
      session.save(new Group(line, 0, true))
    }
  }

  private def createPieces() {
    var seq: Int = 1
    import scala.collection.JavaConversions._
    for (line <- new FileLineProvider(DATA_PATH + "Piece.txt")) {
      val fields: Array[String] = line.split(FIELD_DELIMITER)
      val piece: Piece = new Piece(({
        seq += 1; seq - 1
      }), fields(0))
      session.save(piece)
      if (fields.length == 2) {
        session.save(new Tempo(piece, Integer.parseInt(fields(1))))
      }
    }
  }

  private def createRejectionReasons() {
    import scala.collection.JavaConversions._
    for (line <- new FileLineProvider(DATA_PATH + "RejectionReason.txt")) {
      session.save(new RejectionReason(line))
    }
  }
*/
}
