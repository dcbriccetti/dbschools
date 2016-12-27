package com.dbschools.mgb
package model

import org.apache.log4j.Logger
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.PrimitiveTypeMode.{inTransaction => inT}
import org.joda.time.{DateTime, Days}

import scalaz._
import Scalaz._
import Terms.{currentTerm, termStart, toTs}
import schema._
import Cache.TrendInfo
import net.liftweb.util.Props
import org.joda.time.format.ISODateTimeFormat

case class TestingStats(
  totalPassed:                Int,
  totalFailed:                Int,
  totalDaysTested:            Int,
  inClassDaysTested:          Int,
  outsideClassDaysTested:     Int,
  outsideClassPassed:         Int,
  outsideClassFailed:         Int,
  longestPassingStreakTimes:  Seq[DateTime],
  trendInfo:                  Option[TrendInfo]
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

  private var _lastAssTimeByMusician = inT(for {
    gm <- from(AppSchema.assessments)(a => groupBy(a.musician_id) compute max(a.assessment_time))
    m <- gm.measures
  } yield gm.key -> new DateTime(m.getTime)).toMap

  def lastAssTimeByMusician: Map[Int, DateTime] = _lastAssTimeByMusician
  def updateLastAssTime(musicianId: Int, time: DateTime): Unit = {
    _lastAssTimeByMusician += musicianId -> time
    updateNumDaysTestedThisYearByMusician(musicianId)
  }

  private var _numPassesThisTermByMusician = inT(for {
    gm <- from(AppSchema.assessments)(a =>
      where(a.pass === true and a.assessment_time > toTs(termStart(currentTerm)))
      groupBy a.musician_id
      compute count(a.assessment_time)
    )
    m = gm.measures
  } yield gm.key -> m.toInt).toMap

  def numPassesThisTermByMusician: Map[Int, Int] = _numPassesThisTermByMusician
  def incrementNumPassesThisTermByMusician(musicianId: Int): Unit = {
    _numPassesThisTermByMusician += musicianId -> (_numPassesThisTermByMusician.getOrElse(musicianId, 0) + 1)
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

  case class MusicianTimePass(id: Int, time: DateTime, pass: Boolean, duringClass: Boolean)

  private def readTestingStats(opMusicianId: Option[Int] = None): Map[Int, Map[Option[DateTime], TestingStats]] = inT {
    val testsByMusician: Map[Int, Iterable[MusicianTimePass]] =
      from(AppSchema.assessments)(a =>
      where(a.musician_id === opMusicianId.? and a.assessment_time > toTs(termStart(currentTerm)))
      select {
        val dateTime = new DateTime(a.assessment_time.getTime)
        MusicianTimePass(a.musician_id, dateTime, a.pass,
          Periods.periodWithin(dateTime).isInstanceOf[Periods.Period])
      }
      orderBy(a.musician_id, a.assessment_time)).groupBy(_.id)

    log.info(s"terms: $terms")
    val testsByMusicianByTerm: Map[Int, Map[Option[DateTime], Seq[MusicianTimePass]]] = testsByMusician.map {
      case (musicianId, mtps) =>
        val mtpsByTerm: Map[Option[DateTime], Seq[MusicianTimePass]] =
          mtps.toSeq.groupBy { mtp => Some(terms.find(_.getMillis < mtp.time.getMillis).get) }
        musicianId -> mtpsByTerm
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

  private def statsForMtps(mtps: Iterable[MusicianTimePass]) = {
    val passFails = mtps.map(_.pass)
    val totalPassed = passFails.count(_ == true)
    val totalFailed = passFails.size - totalPassed

    def uniqueDays(mtps: Iterable[MusicianTimePass]): Int =
      mtps.map(a => new DateTime(a.time).withTimeAtStartOfDay.getMillis).toSet.size

    def daysTested(in: Boolean): Int = uniqueDays(mtps.filter(_.duringClass == in))

    val outsideClassTests = mtps.filterNot(_.duringClass)

    TestingStats(totalPassed, totalFailed,
      uniqueDays(mtps), daysTested(true), daysTested(false),
      outsideClassTests.count(_.pass),
      outsideClassTests.count(!_.pass),
      longestStreak(mtps), trend(mtps))
  }

  private def readCanWrite() = usersWithRole(Roles.Write.id)

  private def readAdmins() = usersWithRole(Roles.Admin.id)

  private def usersWithRole(role: Int) = inT {
    AppSchema.userRoles.withFilter(_.roleId == role).map(_.userId).toSet
  }

  private def longestStreak(mtps: Iterable[MusicianTimePass]) = {
    type Times = Seq[DateTime]
    case class StreakInfo(longest: Times, current: Times)
    mtps.foldLeft(StreakInfo(Seq(), Seq()))((si, mtp) => {
      val newCurrent = if (mtp.pass) si.current :+ mtp.time else Seq()
      StreakInfo(if (newCurrent.size > si.longest.size) newCurrent else si.longest, newCurrent)
    }).longest
  }

  case class TrendInfo(passingImprovement: Double, recentDailyPassCounts: Seq[Int])

  private def trend(mtps: Iterable[MusicianTimePass], numDays: Int = 5, maxPerDay: Int = 3) = {
    mtps.groupBy(_.time.withTimeAtStartOfDay) match {
      case passesByDay if passesByDay.size >= numDays =>
        val datesOfRecentTests = passesByDay.keys.toSeq.sortBy(_.getMillis).takeRight(numDays)
        val testsEachDay = datesOfRecentTests map passesByDay
        val recentDailyPassesCountsWithTime = testsEachDay.map(t =>
          t.head.time -> math.min(maxPerDay, t.count(_.pass == true)))
        val points = recentDailyPassesCountsWithTime.map(ct =>
          Stats.Point(Days.daysBetween(testsEachDay.head.head.time, ct._1).getDays, ct._2))
        val recentDailyPassCounts = testsEachDay.map(_.count(_.pass == true))
        Some(TrendInfo(Stats.regressionSlope(points), recentDailyPassCounts))
      case _ => none[TrendInfo]
    }
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