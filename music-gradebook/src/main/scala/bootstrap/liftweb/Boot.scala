package bootstrap.liftweb

import org.squeryl.PrimitiveTypeMode._
import net.liftweb._
import util._
import common._
import http._
import sitemap._
import Loc._
import net.liftmodules.widgets.flot.Flot
import net.liftmodules.FoBo

import com.dbschools.mgb.Db
import com.dbschools.mgb.model.Cache

object RunState {
  object loggedIn extends SessionVar[Boolean] (false)
}

class Boot {
  def boot(): Unit = {
    import bootstrap.liftweb.ApplicationPaths._
    
    // where to search snippet
    LiftRules.addToPackages("com.dbschools.mgb")

    val loggedIn    = If(() => RunState.loggedIn,   "Not logged in")
    val notLoggedIn = If(() => ! RunState.loggedIn, "Already logged in")

    // Build SiteMap
    def sitemap = SiteMap(
      home.menu,
      logIn.menu                >> notLoggedIn,
      students.menu             >> loggedIn,
      newStudent.menu           >> loggedIn >> Hidden,
      instrumentsList.menu      >> loggedIn,
      instrumentsCreate.menu    >> loggedIn >> Hidden,
      instrumentsDelete.menu    >> loggedIn >> Hidden,
      instrumentsEdit.menu      >> loggedIn >> Hidden,
      instrumentsView.menu      >> loggedIn >> Hidden,
      noGroups.menu             >> loggedIn,
      studentDetails.menu       >> loggedIn >> Hidden,
      graphs.menu               >> loggedIn,
      stats.menu                >> loggedIn,
      history.menu              >> loggedIn,
      logout.menu               >> loggedIn
    )

    LiftRules.setSiteMap(sitemap)

    //Init the FoBo - Front-End Toolkit module,
    //see http://liftweb.net/lift_modules for more info
    FoBo.InitParam.JQuery=FoBo.JQuery1102 // jquery v1.10.2
    FoBo.InitParam.ToolKit=FoBo.Bootstrap300 //bootstrap v3.0.0
    FoBo.init()

    //Show the spinny image when an Ajax call starts
    LiftRules.ajaxStart =
      Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)
    
    // Make the spinny image go away when it ends
    LiftRules.ajaxEnd =
      Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

    // What is the function to test if a user is logged in?
    LiftRules.loggedInTest = Full(() => RunState.loggedIn.is)

    // Use HTML5 for rendering
    LiftRules.htmlProperties.default.set((r: Req) =>
      new Html5Properties(r.userAgent))    

    Db.initialize()
    S.addAround(new LoanWrapper{override def apply[T](f: => T): T = {inTransaction {f}}})

    Cache.init()
    Flot.init()
  }
}
