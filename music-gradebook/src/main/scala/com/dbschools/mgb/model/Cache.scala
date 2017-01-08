package com.dbschools.mgb
package model

import org.apache.log4j.Logger
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.PrimitiveTypeMode.{inTransaction => inT}
import org.joda.time.DateTime

import scalaz._
import Scalaz._
import SchoolYears.{current, startDate, toTs}
import schema._
import snippet.svStatsDisplay

object Cache {
  val log: Logger = Logger.getLogger(getClass)

  val terms                                 = new Terms(None)
  var groups:       Seq[Group]              = readGroups
  var groupTerms:   List[GroupTerm]         = readGroupTerms
  var instruments:  Seq[Instrument]         = readInstruments
  var (subinstruments, subsByInstrument)    = readSubinstruments
  var tags:         Seq[PredefinedComment]  = readTags
  var pieces:       Seq[Piece]              = readPieces
  var tempos:       Seq[Tempo]              = readTempos
  private var testingStatsByMusician: Map[Int, Map[Option[DateTime], TestingStats]] = readTestingStats()
  var canWriteUsers: Set[Int]               = readCanWrite()
  var adminUsers:   Set[Int]                = readAdmins()
  val activeTestingWeeks                    = new ActiveTestingWeeks
  activeTestingWeeks.loadCurrentSchoolYearFromDatabase(terms.yearStart)

  /**
    * Gets testing stats for the specified musician.
    * @param musicianId the ID of the musician
    * @param opDateTime None for the entire school year, or a DateTime for the term starting on that date
    * @return Some(TestingStats) if found, None if not
    */
  def testingStatsByMusician(musicianId: Int, opDateTime: Option[DateTime] = None): Option[TestingStats] =
    testingStatsByMusician.get(musicianId).flatMap(_.get(opDateTime))

  def selectedTestingStatsByMusician(musicianId: Int): Option[TestingStats] =
    testingStatsByMusician(musicianId,
      if (svStatsDisplay.is == StatsDisplay.Term) Some(terms.current) else none[DateTime])

  private var _lastTestTimeByMusician: Map[Int, DateTime] = inT(for {
    groupWithMeasures <- from(AppSchema.assessments)(a => groupBy(a.musician_id) compute max(a.assessment_time))
    testTime          <- groupWithMeasures.measures
    musicianId        =  groupWithMeasures.key
  } yield musicianId -> new DateTime(testTime.getTime)).toMap

  def lastTestTimeByMusician: Map[Int, DateTime] = _lastTestTimeByMusician
  private def updateLastTestTime(musicianId: Int, time: DateTime): Unit = {
    _lastTestTimeByMusician += musicianId -> time
  }

  private def readGroups      = inT {AppSchema.groups.toSeq.sortBy(_.name)}
  private def readGroupTerms  = inT {AppSchema.groupTerms.toList}
  private def readInstruments = inT {AppSchema.instruments.toSeq.sortBy(_.sequence.get)}
  private def readSubinstruments = inT {
    val subs = AppSchema.subinstruments.toSeq
    (subs, subs.groupBy(_.instrumentId.get))
  }
  private def readTags        = inT {AppSchema.predefinedComments.toSeq.sortBy(_.commentText)}
  private def readPieces      = inT {AppSchema.pieces.toSeq.sortBy(_.testOrder.get)}
  private def readTempos      = inT {AppSchema.tempos.toSeq.sortBy(_.instrumentId)}

  case class MusicianTestInfo(musicianId: Int, time: DateTime, pass: Boolean, duringClass: Boolean)

  private def readTestingStats(opMusicianId: Option[Int] = None): Map[Int, Map[Option[DateTime], TestingStats]] = inT {
    val testsByMusician: Map[Int, Iterable[MusicianTestInfo]] =
      from(AppSchema.assessments)(a =>
      where(a.musician_id === opMusicianId.? and a.assessment_time > toTs(startDate(current)))
      select {
        val dateTime = new DateTime(a.assessment_time.getTime)
        MusicianTestInfo(a.musician_id, dateTime, a.pass, Periods.isDuringClassPeriod(dateTime))
      }
      orderBy(a.musician_id, a.assessment_time)).groupBy(_.musicianId)

    testsByMusician.map {
      case (musicianId, testInfos) =>
        val testInfosByTerm: Map[Option[DateTime], Seq[MusicianTestInfo]] =
          testInfos.toSeq.groupBy(mti => Some(terms.containing(mti.time)))
        val statsForWholeTerm = none[DateTime] -> TestingStats(testsByMusician(musicianId))
        val testingStatsByTerm = testInfosByTerm.mapValues(TestingStats.apply) + statsForWholeTerm
        musicianId -> testingStatsByTerm
    }
  }

  private def readCanWrite() = usersWithRole(Roles.Write.id)

  private def readAdmins() = usersWithRole(Roles.Admin.id)

  private def usersWithRole(role: Int) = inT {
    AppSchema.userRoles.withFilter(_.roleId == role).map(_.userId).toSet
  }

  def init(): Unit = {
    model.Assessments.registerListener {
      case TestSavedEvent(musicianId, dateTime) =>
        log.info(s"Processing TestSavedEvent for $musicianId at $dateTime")
        Cache.updateLastTestTime(musicianId, dateTime)
        Cache.updateTestingStats(musicianId)
        activeTestingWeeks.addFrom(Seq(dateTime), Cache.terms.yearStart)

      case TestsDeletedEvent(musicianIds) =>
        log.info(s"Processing TestsDeletedEvent for $musicianIds")
        musicianIds foreach Cache.updateTestingStats
    }
  }

  def invalidateGroups(): Unit = { groups = readGroups }

  def invalidateGroupTerms(): Unit = { groupTerms = readGroupTerms }

  def invalidateInstruments(): Unit = { instruments = readInstruments }

  def invalidateSubinstruments(): Unit = {
    val (a, b) = readSubinstruments
    subinstruments = a
    subsByInstrument = b
  }

  def invalidateTags(): Unit = { tags = readTags }

  def invalidatePieces(): Unit = { pieces = readPieces }

  def invalidateTempos(): Unit = { tempos = readTempos }

  private def updateTestingStats(musicianId: Int): Unit = testingStatsByMusician ++= readTestingStats(Some(musicianId))

  def nextPiece(piece: Piece): Option[Piece] = pieces.find(_.testOrder.get.compareTo(piece.testOrder.get) > 0)

  case class GroupPeriod(group: Group, period: Int)
  
  def filteredGroups(opSelectedTerm: Option[Int]): Seq[GroupPeriod] = {
    val groupIdToPeriod = (for {
      gt      <- Cache.groupTerms
      selTerm <- opSelectedTerm
      if gt.term == selTerm
    } yield gt.groupId -> gt.period).toMap

    val unsorted = if (groupIdToPeriod.isEmpty)
      Cache.groups.map(g => GroupPeriod(g, 0))
    else
      for {
        group   <- Cache.groups
        period  <- groupIdToPeriod.get(group.id)
      } yield GroupPeriod(group, period)
    unsorted.sortBy(gp => (gp.period, gp.group.shortOrLongName))
  }
}
