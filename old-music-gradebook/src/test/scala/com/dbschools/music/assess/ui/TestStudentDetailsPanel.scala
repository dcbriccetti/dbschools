package com.dbschools.music.assess

import java.util.ArrayList
import java.util.HashSet
import java.util.Date

import junit.framework.TestCase
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNotNull
import org.hamcrest.{Description, TypeSafeMatcher, Matcher}
import org.jmock.Expectations
import org.jmock.lib.action.ReturnValueAction
import org.jmock.Mockery
import com.dbschools.music.dao.RemoteDao
import com.dbschools.music.NonPersistentPreferences
import com.dbschools.music.orm._
import com.dbschools.music.events.EventObserver
import com.dbschools.music.events.Event
import com.dbschools.music.events.TypeCode
import com.dbschools.music.assess.ui.StudentDetailsPanel

/**
 * Work around issue with “with” being a reserved word in Scala
 */
class SExpectations extends Expectations {
  def withArg[T](matcher: Matcher[T]): T = super.`with`(matcher)  
}

class TestStudentDetailsPanel extends TestCase {
  var capturedEventObserver: EventObserver = _

  /**
   * Special matcher that captures the passed parameter for later use
   */
  class EventObserverMatcher extends TypeSafeMatcher[EventObserver] {

      override def matchesSafely(eventObserver: EventObserver): Boolean = {
          capturedEventObserver = eventObserver
          true
      }

      override def describeTo(description: Description) {
          // Empty
      }
  }
  
  def eventObserver: EventObserverMatcher = {
    new EventObserverMatcher
  }

  /**
   *  Assessments for a student other than the one in the window are not added to the model. 
   */
  def testNotificationsHandling {
    val dave = new Musician(101L, "Dave", "Clark", 2010, "M")
    dave.setId(1)
    setUpMusician(dave)
    val sue = new Musician(102L, "Sue", "Fields", 2010, "M")
    dave.setId(2)
    setUpMusician(sue)

    val mockery = new Mockery()
    val dao = mockery.mock(classOf[RemoteDao])
    mockery.checking(new SExpectations {{
        oneOf(dao).getUser
        will(new ReturnValueAction(new User))
        oneOf(dao).getRejectionReasons
        will(new ReturnValueAction(new ArrayList[RejectionReason]))
        oneOf(dao).addEventObserver(withArg(new EventObserverMatcher)) // Grab the observer with this matcher
        oneOf(dao).getMusicianAndAssessmentInfo(1)
        will(new ReturnValueAction(dave))
        oneOf(dao).getMusicianAndAssessmentInfo(2)
        will(new ReturnValueAction(sue))
    }})
    val sdp = new StudentDetailsPanel(dao, dave, new NonPersistentPreferences)
    assertEquals("assessments", 0, sdp.getAssessmentsCount)   

    // Add an assessment for Dave and check the assessments count
    val ass = new Assessment()
    ass.setId(1)
    setUpAssessment(ass)
    ass.setMusician(dave)
    val event = new Event(TypeCode.SAVE_OBJECT, ass)
    capturedEventObserver.notify(event)
    assertEquals("assessments", 1, sdp.getAssessmentsCount)   

    // Add the same assessment for Dave and check that the assessments count stays the same
    capturedEventObserver.notify(event)
    assertEquals("assessments", 1, sdp.getAssessmentsCount)   

    // Add an assessment for Sue and check that it isn't added to the model
    val ass2 = new Assessment()
    ass2.setId(2)
    setUpAssessment(ass2)
    ass2.setMusician(sue)
    val event2 = new Event(TypeCode.SAVE_OBJECT, ass2)
    capturedEventObserver.notify(event2)
    assertEquals("assessments", 1, sdp.getAssessmentsCount)
    
    mockery.assertIsSatisfied();
  }
  
  private def setUpMusician(musician: Musician) {
    musician.setAssessments(new HashSet[Assessment])
    musician.setRejections(new HashSet[Rejection])
    musician.setMusicianGroups(new HashSet[MusicianGroup])
  }
  
  private def setUpAssessment(ass: Assessment) {
    ass.setAssessmentTime(new Date())
    ass.setNotes("Well done")
    ass.setPredefinedComments(new ArrayList[PredefinedComment])
  }
}