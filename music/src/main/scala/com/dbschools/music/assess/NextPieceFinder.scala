package com.dbschools.music.assess

import scala.collection.JavaConversions._
import java.util.Collection
import java.util.TreeSet
import com.dbschools.music.orm.{Assessment, Instrument, NoNextPieceIndicator, Piece, Subinstrument}

/**
 * NextPieceFinder finds the next piece on which a musician is to be tested.
 * @param pieces the pieces to consider
 *
 * @author David C. Briccetti
 */
final class NextPieceFinder(piecesCol: Collection[Piece]) {
  private val pieces = new TreeSet[Piece](piecesCol)

  /**
   * Finds the next piece on which the musician must test,
   * in the given collection of assessments,
   * on the specified instrument and subinstrument.
   * @param assessments a collection of assessments to search
   * @param instrument an instrument, or null if instrument is not to be considered
   * @param subinstrument a subinstrument, or null if subinstrument is not to be considered
   * @return the next piece
   */
  def nextPiece(assessments: Collection[Assessment], instrument: Instrument, subinstrument: Subinstrument): Piece =
    findHighestPass(assessments, instrument, subinstrument) match {
      case None => pieces.first
      case Some(highestPassedPiece) =>
        pieces.find(_.compareTo(highestPassedPiece) > 0).getOrElse(new NoNextPieceIndicator)
    }

  /**
   * Finds the highest piece passed, in the given collection of assessments,
   * on the specified instrument and subinstrument.
   * @param assessments a collection of assessments to search
   * @param instrument an instrument, or null if instrument is not to be considered
   * @param subinstrument a subinstrument, or null if subinstrument is not to be considered
   * @return the highest piece passed
   */
  private def findHighestPass(assessments: Collection[Assessment], instrument: Instrument,
      subinstrument: Subinstrument): Option[Piece] = {

    assessments.foldLeft(None: Option[Piece]) { (opHighestPassedPiece, assmnt) =>

      val instrMismatch    = instrument    != null && (instrument    != assmnt.getMusicInstrument)
      val subinstrMismatch = subinstrument != null && (subinstrument != assmnt.getMusicSubinstrument)
      val piece = assmnt.getMusicPiece

      val isPieceHigher = opHighestPassedPiece match {
        case None => true
        case Some(highestPassedPiece) => piece.compareTo(highestPassedPiece) > 0
      }

      if (assmnt.isPass && ! instrMismatch && ! subinstrMismatch && isPieceHigher)
        Some(piece)
      else
        opHighestPassedPiece
    }
  }
}

