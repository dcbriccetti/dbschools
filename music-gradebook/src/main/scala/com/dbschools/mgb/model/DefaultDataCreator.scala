package com.dbschools.mgb.model

import io.Source
import org.squeryl.PrimitiveTypeMode._
import net.liftweb.common.Loggable
import com.dbschools.mgb.schema.{Tempo, Piece, RejectionReason, Group, User, Subinstrument, Instrument, AppSchema, PredefinedComment}
import com.dbschools.mgb.convert.EncryptPasswords

object DefaultDataCreator extends Loggable {
  def createIfEmpty(): Unit = {
    transaction {
      if (AppSchema.users.headOption.isEmpty) {
        createUser()
        createInstruments()
        createGroups()
        createPieces()
        createRejectionReasons()
        createPredefinedComments()
      }
    }
  }

  private def getLines(namePart: String) =
    Source.fromInputStream(classOf[PredefinedComment].getResourceAsStream(s"/data/$namePart.txt")).
      getLines().map(_.trim).filterNot(l => l.isEmpty || l.startsWith("#")).toList

  private def createPredefinedComments(): Unit = {
    val comments = getLines("PredefinedComment").map(PredefinedComment(0, _, ""))
    AppSchema.predefinedComments.insert(comments)
    Cache.invalidateTags()
  }

  private def createUser(): Unit = {
    AppSchema.users.insert(getLines("User").map(line => {
      val fields = line.split("\t")
      User(0, fields(0), fields(1), EncryptPasswords.encrypt(fields(1)), fields(2), fields(3), enabled = true)
    }))
  }

  private def createInstruments(): Unit = {
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

  private def createGroups(): Unit = {
    AppSchema.groups.insert(getLines("Group").map(Group(0, _, doesTesting = true)))
  }

  private def createPieces(): Unit = {
    var seq: Int = 1
    getLines("Piece").foreach(line => {
      val fields = line.split("\t")
      val piece = Piece.createRecord.testOrder(seq).name(fields(0))
      AppSchema.pieces.insert(piece)
      seq += 1
      if (fields.length == 2) {
        AppSchema.tempos.insert(Tempo(0, piece.id, None, Integer.parseInt(fields(1))))
      }
    })
  }

  private def createRejectionReasons(): Unit = {
    AppSchema.rejectionReasons.insert(getLines("RejectionReason").map(RejectionReason(0, _)))
  }
}
