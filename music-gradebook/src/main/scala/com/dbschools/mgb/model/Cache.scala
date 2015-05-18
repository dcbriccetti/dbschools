package com.dbschools.mgb
package model

import org.squeryl.PrimitiveTypeMode._
import org.squeryl.PrimitiveTypeMode.{inTransaction => inT}
import org.joda.time.DateTime
import scalaz._
import Scalaz._
import Terms.{toTs, termStart, currentTerm}
import schema.{Group, Piece, AppSchema}
import Cache.TrendInfo

case class TestingStats(
  numTests:                   Int,
  longestPassingStreakTimes:  Seq[DateTime],
  trendInfo:                  Option[TrendInfo],
  percentPassed:              Int
)

object Cache {
  var groups = readGroups
  var groupTerms = readGroupTerms
  var instruments = readInstruments
  var (subinstruments, subsByInstrument) = readSubinstruments
  var tags = readTags
  var pieces = readPieces
  var tempos = readTempos
  var testingStatsByMusician = readTestingStats()

  private var _lastAssTimeByMusician = inT(for {
    gm <- from(AppSchema.assessments)(a => groupBy(a.musician_id) compute max(a.assessment_time))
    m <- gm.measures
  } yield gm.key -> new DateTime(m.getTime)).toMap

  def lastAssTimeByMusician = _lastAssTimeByMusician
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

  def numPassesThisTermByMusician = _numPassesThisTermByMusician
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

  def numDaysTestedThisYearByMusician = _numDaysTestedThisYearByMusician

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

  case class MusicianTimePass(id: Int, time: DateTime, pass: Boolean)

  private def readTestingStats(opMusicianId: Option[Int] = None) = inT {
    val testsByMusician = from(AppSchema.assessments)(a =>
      where(a.musician_id === opMusicianId.? and a.assessment_time > toTs(termStart(currentTerm)))
      select MusicianTimePass(a.musician_id, new DateTime(a.assessment_time.getTime), a.pass)
      orderBy(a.musician_id, a.assessment_time)).groupBy(_.id)
    testsByMusician.map {
      case (musicianId, mtps) =>
        val passFails = mtps.map(_.pass)
        val a = trend(mtps)
        musicianId -> TestingStats(passFails.size, longestStreak(mtps), a,
          if (passFails.isEmpty) 0 else math.round(passFails.count(_ == true) * 100 / passFails.size))
    }
  }

  private def longestStreak(mtps: Iterable[MusicianTimePass]) = {
    type Times = Seq[DateTime]
    case class StreakInfo(longest: Times, current: Times)
    mtps.foldLeft(StreakInfo(Seq(), Seq()))((si, mtp) => {
      val newCurrent = if (mtp.pass) si.current :+ mtp.time else Seq()
      StreakInfo(if (newCurrent.size > si.longest.size) newCurrent else si.longest, newCurrent)
    }).longest
  }

  case class TrendInfo(passingImprovement: Float, recentDailyPassCounts: Seq[Int])

  private def trend(mtps: Iterable[MusicianTimePass]) = {
    val NumDaysToConsider = 5
    val NumDaysToConsiderCurrent = 2
    val groupedTests = mtps.groupBy(_.time.withTimeAtStartOfDay)
    if (groupedTests.size < NumDaysToConsider) none[TrendInfo] else {
      val datesOfTests = groupedTests.keys.toSeq.sortBy(_.getMillis).takeRight(NumDaysToConsider)
      val testsEachDay = datesOfTests map groupedTests
      val recentDailyPassCounts = testsEachDay.map(_.count(_.pass == true))
      val beforeDays = recentDailyPassCounts take NumDaysToConsider - NumDaysToConsiderCurrent
      val latestDays  = recentDailyPassCounts takeRight NumDaysToConsiderCurrent
      def avg(s: Seq[Int]) = s.sum.toFloat / s.size
      val avgBefore = avg(beforeDays)
      val avgLatest = avg(latestDays)
      val changeInAverages = avgLatest - avgBefore
      val passingImprovement = // Avoid divide by zero
        if (avgBefore == 0f && avgLatest == 0f) 0f else
        changeInAverages / (if (avgBefore == 0f) avgLatest else avgBefore)
      Some(TrendInfo(passingImprovement, recentDailyPassCounts))
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

  def nextPiece(piece: Piece) = pieces.find(_.testOrder.get.compareTo(piece.testOrder.get) > 0)

  case class GroupPeriod(group: Group, period: Int)
  
  def filteredGroups(opSelectedTerm: Option[Int]) = {
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