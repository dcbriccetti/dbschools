package bootstrap.liftweb

/** These are used to build the site menu, and to create inter-page links. */
object ApplicationPaths {
  val home              = new Path("Home", "index")

  val instrumentsList   = new Path("Instruments", "instruments/list")
  val instrumentsCreate = new Path("New Instrument", "instruments/create")
  val instrumentsDelete = new Path("Delete Instrument", "instruments/delete")
  val instrumentsEdit   = new Path("Edit Instrument", "instruments/edit")
  val instrumentsView   = new Path("View Instrument", "instruments/view")
  
  val logIn             = new Path("Log In", "logIn")
  val logout            = new Path("Log Out", "logOut")
  
  val noGroups          = new Path("No Groups", "noGroups")
  
  val graphs            = new Path("Graphs", "graphs")
  
  val activity          = new Path("Activity",    "activity")
  val stats             = new Path("Statistics",  "stats")
  val students          = new Path("Students",    "students")
  val newStudent        = new Path("New Student", "newStudent")
  val studentDetails    = new Path("Details",     "studentDetails")

  val history           = new Path("History",     "history")
}