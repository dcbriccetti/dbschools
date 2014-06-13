package bootstrap.liftweb

/** These are used to build the site menu, and to create inter-page links. */
object ApplicationPaths {
  val home              = new Path("Home", "index")
  val logIn             = new Path("Log In", "logIn")
  val logout            = new Path("Log Out", "logOut")

  val noGroups          = new Path("No Groups", "noGroups")
  val groups            = new Path("Groups", "groups")

  val graphs            = new Path("Graphs", "graphs")

  val activity          = new Path("Activity",    "activity")
  val stats             = new Path("Statistics",  "stats")
  val students          = new Path("Students",    "students")
  val testing           = new Path("Testing",     "testing")
  val editStudent       = new Path("Edit Student", "editStudent")
  val studentDetails    = new Path("Student Details", "studentDetails")

  val history           = new Path("History",     "history")

  val instrumentsList   = new Path("Instruments",       "instruments/list")
  val instrumentsCreate = new Path("New Instrument",    "instruments/create")
  val instrumentsDelete = new Path("Delete Instrument", "instruments/delete")
  val instrumentsEdit   = new Path("Edit Instrument",   "instruments/edit")
  val instrumentsView   = new Path("View Instrument",   "instruments/view")
  val studentImport     = new Path("Import",  "import")
}