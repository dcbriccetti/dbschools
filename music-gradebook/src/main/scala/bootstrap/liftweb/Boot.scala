package bootstrap.liftweb

import net.liftweb._
import util._

import common._
import http._
import js.jquery.JQueryArtifacts
import sitemap._
import Loc._

import net.liftmodules.JQueryModule
import org.squeryl.PrimitiveTypeMode._
import com.dbschools.mgb.Db

object RunState {
  object loggedIn extends SessionVar[Boolean] (false)
}

class Boot {
  def boot {
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
      problems.menu             >> loggedIn,
      stats.menu                >> loggedIn,
      logout.menu               >> loggedIn
    )

    LiftRules.setSiteMap(sitemap)

    //Init the jQuery module, see http://liftweb.net/jquery for more information.
    LiftRules.jsArtifacts = JQueryArtifacts
    JQueryModule.InitParam.JQuery=JQueryModule.JQuery172
    JQueryModule.init()

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
  }
}
