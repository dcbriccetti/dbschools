package bootstrap.liftweb

object ApplicationPaths {
  lazy val instrumentsList = new Path("Instruments", "instruments/list")
  lazy val instrumentsCreate = new Path("New Instrument", "instruments/create")
  lazy val instrumentsDelete = new Path("Delete Instrument", "instruments/delete")
  lazy val instrumentsEdit = new Path("Edit Instrument", "instruments/edit")
  lazy val instrumentsView = new Path("View Instrument", "instruments/view")
}