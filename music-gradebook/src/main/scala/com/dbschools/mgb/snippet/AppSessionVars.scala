package com.dbschools.mgb.snippet

import net.liftweb.http.SessionVar
import com.dbschools.mgb.model.{GroupAssignment, PicturesDisplay, SortStudentsBy, StatsDisplay}

object svSortingStudentsBy extends SessionVar[SortStudentsBy.Value](SortStudentsBy.Name)

object svPicturesDisplay extends SessionVar[PicturesDisplay.Value](PicturesDisplay.Small)

object svSelectors extends SessionVar[Selectors](new Selectors())

object svGroupAssignments extends SessionVar[Seq[GroupAssignment]](Nil)

object svStatsDisplay extends SessionVar[StatsDisplay.Value](StatsDisplay.Term)

object svShowStatsOnStudentsPage extends SessionVar[Boolean](true)

object svShowMoreStudentDetails extends SessionVar[Boolean](false)
