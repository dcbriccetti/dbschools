package bootstrap.liftweb

/** Convenient placeholder for all application paths. */
object ApplicationPaths {
  val instrumentsList = new Path("Instruments", "instruments/list")
  val instrumentsCreate = new Path("New Instrument", "instruments/create")
  val instrumentsDelete = new Path("Delete Instrument", "instruments/delete")
  val instrumentsEdit = new Path("Edit Instrument", "instruments/edit")
  val instrumentsView = new Path("View Instrument", "instruments/view")
}