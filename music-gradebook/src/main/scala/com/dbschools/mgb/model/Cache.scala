package com.dbschools.mgb
package model

import org.apache.log4j.Logger
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.PrimitiveTypeMode.{inTransaction => inT}
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat

import scalaz._
import Scalaz._
import net.liftweb.util.Props
import Terms.{currentTerm, termStart, toTs}
import schema._
import snippet.svStatsDisplay

case class TestingStats(
  totalPassed:                Int,
  totalFailed:                Int,
  totalDaysTested:            Int,
  inClassDaysTested:          Int,
  outsideClassDaysTested:     Int,
  outsideClassPassed:         Int,
  outsideClassFailed:         Int,
  longestPassingStreakTimes:  Seq[DateTime],
  opTestingImprovement:       Option[TestingImprovement]
) {
  def numTests: Int = totalPassed + totalFailed
  def percentPassed: Int = if (numTests == 0) 0 else math.round(totalPassed * 100.0 / numTests).toInt
}

object Cache {
  val log: Logger = Logger.getLogger(getClass)
  /** Term start dates, ordered from most to least recent */
  val terms: Seq[DateTime] = {
    val dtp = ISODateTimeFormat.dateTimeParser()
    Props.get("terms").get.split(',').map(dtp.parseDateTime).toSeq.sortBy(_.getMillis).reverse
  }
  def currentMester: DateTime = terms.find(_.getMillis < DateTime.now.getMillis).get
  var groups: Seq[Group] = readGroups
  var groupTerms: List[GroupTerm] = readGroupTerms
  var instruments: Seq[Instrument] = readInstruments
  var (subinstruments, subsByInstrument) = readSubinstruments
  var tags: Seq[PredefinedComment] = readTags
  var pieces: Seq[Piece] = readPieces
  var tempos: Seq[Tempo] = readTempos
  private var testingStatsByMusician: Map[Int, Map[Option[DateTime], TestingStats]] = readTestingStats()
  var canWriteUsers: Set[Int] = readCanWrite()
  var adminUsers: Set[Int] = readAdmins()

  def testingStatsByMusician(musicianId: Int, opDateTime: Option[DateTime] = None): Option[TestingStats] =
    testingStatsByMusician.get(musicianId).flatMap(_.get(opDateTime))

  def selectedTestingStatsByMusician(musicianId: Int) = testingStatsByMusician(musicianId,
    if (svStatsDisplay.is == StatsDisplay.Term) Some(Cache.currentMester) else none[DateTime])

  private var _lastAssTimeByMusician = inT(for {
    gm <- from(AppSchema.assessments)(a => groupBy(a.musician_id) compute max(a.assessment_time))
    m <- gm.measures
  } yield gm.key -> new DateTime(m.getTime)).toMap

  def lastAssTimeByMusician: Map[Int, DateTime] = _lastAssTimeByMusician
  def updateLastAssTime(musicianId: Int, time: DateTime): Unit = {
    _lastAssTimeByMusician += musicianId -> time
    updateNumDaysTestedThisYearByMusician(musicianId)
  }

  private def numDaysTestedThisYear(musicianId: Option[Int]) = inT {
    val testTimes = from(AppSchema.assessments)(a =>
      where(a.assessment_time > toTs(termStart(currentTerm)) and a.musician_id === musicianId.?)
      select(a.musician_id, new DateTime(a.assessment_time).withTimeAtStartOfDay.getMillis)).groupBy(_._1)
    testTimes.map { case (id, times) => id -> times.toSet.size }
  }

  private var _numDaysTestedThisYearByMusician = numDaysTestedThisYear(None)

  def numDaysTestedThisYearByMusician: Map[Int, Int] = _numDaysTestedThisYearByMusician

  private def updateNumDaysTestedThisYearByMusician(musicianId: Int): Unit = {
    val numDays = numDaysTestedThisYear(Some(musicianId)).getOrElse(musicianId, 0)
    inT{_numDaysTestedThisYearByMusician += musicianId -> numDays}
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

  case class MusicianTestInfo(id: Int, time: DateTime, pass: Boolean, duringClass: Boolean)

  private def readTestingStats(opMusicianId: Option[Int] = None): Map[Int, Map[Option[DateTime], TestingStats]] = inT {
    val testsByMusician: Map[Int, Iterable[MusicianTestInfo]] =
      from(AppSchema.assessments)(a =>
      where(a.musician_id === opMusicianId.? and a.assessment_time > toTs(termStart(currentTerm)))
      select {
        val dateTime = new DateTime(a.assessment_time.getTime)
        MusicianTestInfo(a.musician_id, dateTime, a.pass,
          Periods.periodWithin(dateTime).isInstanceOf[Periods.Period])
      }
      orderBy(a.musician_id, a.assessment_time)).groupBy(_.id)

    log.info(s"terms: $terms")
    val testsByMusicianByTerm: Map[Int, Map[Option[DateTime], Seq[MusicianTestInfo]]] = testsByMusician.map {
      case (musicianId, mtis) =>
        val mtisByTerm: Map[Option[DateTime], Seq[MusicianTestInfo]] =
          mtis.toSeq.groupBy { mtp => Some(terms.find(_.getMillis < mtp.time.getMillis).get) }
        musicianId -> mtisByTerm
    }

    testsByMusicianByTerm.map {
      case (musicianId, mtpsByTerm) =>
        val termMtps = {
          val allTermsStats = none[DateTime] -> statsForMtps(testsByMusician(musicianId))
          mtpsByTerm.map {
            case (dateTime, mtps) =>
              dateTime -> statsForMtps(mtps)
          } ++ Map(allTermsStats)
        }
        musicianId -> termMtps
    }
  }

  private def statsForMtps(mtps: Iterable[MusicianTestInfo]) = {
    val passFails = mtps.map(_.pass)
    val totalPassed = passFails.count(_ == true)
    val totalFailed = passFails.size - totalPassed

    def uniqueDays(mtps: Iterable[MusicianTestInfo]): Int =
      mtps.map(a => new DateTime(a.time).withTimeAtStartOfDay.getMillis).toSet.size

    def daysTested(in: Boolean): Int = uniqueDays(mtps.filter(_.duringClass == in))

    val outsideClassTests = mtps.filterNot(_.duringClass)

    TestingStats(totalPassed, totalFailed,
      uniqueDays(mtps), daysTested(true), daysTested(false),
      outsideClassTests.count(_.pass),
      outsideClassTests.count(!_.pass),
      longestStreak(mtps), OptionTestingImprovement(mtps))
  }

  private def readCanWrite() = usersWithRole(Roles.Write.id)

  private def readAdmins() = usersWithRole(Roles.Admin.id)

  private def usersWithRole(role: Int) = inT {
    AppSchema.userRoles.withFilter(_.roleId == role).map(_.userId).toSet
  }

  private def longestStreak(mtps: Iterable[MusicianTestInfo]) = {
    type Times = Seq[DateTime]
    case class StreakInfo(longest: Times, current: Times)
    mtps.foldLeft(StreakInfo(Seq(), Seq()))((si, mtp) => {
      val newCurrent = if (mtp.pass) si.current :+ mtp.time else Seq()
      StreakInfo(if (newCurrent.size > si.longest.size) newCurrent else si.longest, newCurrent)
    }).longest
  }

  def init(): Unit = {}

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

  def updateTestingStats(musicianId: Int): Unit = testingStatsByMusician ++= readTestingStats(Some(musicianId))

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
    unsorted.toSeq.sortBy(gp => (gp.period, gp.group.shortOrLongName))
  }
}