package com.dbschools.music.assess.ui

import java.util.{HashMap, ArrayList}
import junit.framework.TestCase
import junit.framework.Assert._
import org.jmock.lib.action.ReturnValueAction
import org.jmock.Mockery
import org.jmock.Expectations
import com.dbschools.music.events.EventObserver
import com.dbschools.music.dao.{RemoteDao}
import com.dbschools.music.orm._
import com.dbschools.music.assess.Pieces

class TestAssessmentRecordDialog extends TestCase {
  /**
   * Test that the right instrument is selected in the combo box.
   */
  def testCorrectInstrumentIsSelected {
    val mockery = new Mockery()
    val dao = mockery.mock(classOf[RemoteDao])
    val pieces = Pieces.createPieces
    var instruments = new ArrayList[Instrument]
    var flute = new Instrument("Flute", 1)
    flute.setId(1)
    instruments.add(flute)
    var clarinet = new Instrument("Clarinet", 2)
    clarinet.setId(2)
    instruments.add(clarinet)
    var trombone = new Instrument("Trombone", 3)
    trombone.setId(3)
    instruments.add(trombone)
    val dave = new Musician(101L, "Dave", "Clark", 2010, "M")
    dave.setId(1)
    var musicianGroupsMap = new HashMap[Integer, ArrayList[MusicianGroup]]
    var group = new Group("Symphonic Band", 1, true)
    var mg = new MusicianGroup(dave, TermUtils.getCurrentTerm(), group, trombone)
    var groups = new ArrayList[MusicianGroup]
    groups.add(mg)
    musicianGroupsMap.put(1, groups)

    mockery.checking(new Expectations {{
        oneOf(dao).getUser
        will(new ReturnValueAction(new User))
        oneOf(dao).getMusicPieces
        will(new ReturnValueAction(pieces))
        oneOf(dao).getInstruments
        will(new ReturnValueAction(instruments))
        oneOf(dao).getMusicianGroupsMap
        will(new ReturnValueAction(musicianGroupsMap))
        oneOf(dao).getComments
        will(new ReturnValueAction(new ArrayList[PredefinedComment]))
        oneOf(dao).getRejectionReasons
        will(new ReturnValueAction(new ArrayList[RejectionReason]))
    }})
    val asms = new ArrayList[Assessment]
    val ard = new AssessmentRecordDialog(dao, dave, asms, null);
    assertEquals(ard.getSelectedInstrument, trombone)
    mockery.assertIsSatisfied();
  }
}