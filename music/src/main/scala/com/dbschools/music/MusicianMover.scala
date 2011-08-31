package com.dbschools.music

import scala.collection.JavaConversions._
import com.dbschools.music.ui.AbstractGroupAndInstrumentFilteredTableModel
import com.dbschools.music.dao.RemoteSaver
import org.apache.log4j.Logger
import com.dbschools.music.orm.Group
import com.dbschools.music.orm.Instrument
import com.dbschools.music.orm.Musician
import com.dbschools.music.orm.MusicianGroup
import java.util.{ArrayList, Arrays}

/**
 * MusicianMover changes a musician's connections to groups and instruments.
 *
 * @author David C. Briccetti
 */
class MusicianMover(musicianTableModel: AbstractGroupAndInstrumentFilteredTableModel, remoteSaver: RemoteSaver,
                    unassignedInstrument: Instrument) {

  private val log = Logger.getLogger(classOf[MusicianMover])

  /**
   * Moves or adds the specified musicians to the specified group.
   * @param term the term defining the scope of the move
   * @param movingIntoMusicGroup the group the musicians are to me moved or
   * added to
   * @param musicians the musicians to be changed
   * @param copy whether the musicians are to be added (<code>true</code>)
   * or moved (<code>false</code>) to the specified group
   */
  def moveSelectedToGroup(term: Int, movingIntoMusicGroup: Group,
      musicians: java.lang.Iterable[Musician], copy: Boolean) {

    def createNewMusicianGroup(musician: Musician, opFirstGroup: Option[MusicianGroup]): MusicianGroup = {
      val newMusicianGroup = new MusicianGroup
      newMusicianGroup.setMusician(musician)
      newMusicianGroup.setGroup(movingIntoMusicGroup)
      newMusicianGroup.setSchoolYear(term)
      val (inst, rank) = opFirstGroup match {
        case Some(group) => (group.getInstrument, group.getInstrumentRanking)
        case None        => (unassignedInstrument, java.lang.Integer.valueOf(0))
      }
      newMusicianGroup.setInstrument(inst)
      newMusicianGroup.setInstrumentRanking(rank)
      newMusicianGroup
    }

    for {
      musician <- musicians
      allGroupsForThisMusician = MusicianGroupUtil.getMusicianGroups(musician, termsColl(term))
      if allGroupsForThisMusician.size <= 1
    } {
      val opFirstGroup = allGroupsForThisMusician.headOption
      if (! copy) {
        allGroupsForThisMusician.clear()
        musicianTableModel.removeMusicianFromGroups(term, musician.getId.intValue)
      }
      musicianTableModel.addMusicianToGroup(movingIntoMusicGroup.getId.intValue, term, musician.getId.intValue)
      allGroupsForThisMusician.add(createNewMusicianGroup(musician, opFirstGroup))
      remoteSaver.saveMusicianMusicGroups(term, musician.getId.intValue, allGroupsForThisMusician)
    }
    musicianTableModel.fireTableDataChanged()
  }

  /**
   * Disconnects the specified musicians from all groups in the specified term.
   * @param term the term defining the scope of the disconnection
   * @param musicians the musicians to be changed
   */
  def disconnect(term: Int, musicians: java.lang.Iterable[Musician]) {
    remoteSaver.removeMusiciansFromGroupsInTerm(term, new java.util.ArrayList[Musician](musicians.toList))
    musicianTableModel.fireTableDataChanged()
  }

  /**
   * Changes the specified musicians to the specified instrument,
   * if the musician is only in one group.
   * @param term the term defining the scope of the move
   * @param movingIntoInstrument the instrument to which the musicians are
   * to be set
   * @param musicians the musicians to be changed
   */
  def moveSelectedToInstrument(term: Int, movingIntoInstrument: Instrument, musicians: java.lang.Iterable[Musician]) {
    val terms = termsColl(term)
    for (musician <- musicians) {
      val allGroupsForThisMusician = MusicianGroupUtil.getMusicianGroups(musician, terms)
      allGroupsForThisMusician.toList match {
        case musicianGroup :: Nil =>
          musicianGroup.setInstrument(movingIntoInstrument)
          remoteSaver.saveMusicianMusicGroups(term, musician.getId.intValue, allGroupsForThisMusician)
          musicianTableModel.setInstrument(term, musician.getId.intValue, musicianGroup.getInstrument)
        case _ =>
      }
    }
    musicianTableModel.fireTableDataChanged()
  }

  private def termsColl(term: java.lang.Integer) = Arrays.asList(term)

}
