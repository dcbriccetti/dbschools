package bootstrap.liftweb

import scalaz._
import Scalaz._
import net.liftweb._
import common._
import http._
import sitemap._
import Loc._
import net.liftmodules.widgets.flot.Flot
import net.liftmodules.FoBo

import com.dbschools.mgb.model.Cache
import com.dbschools.mgb.dbconn.Db
import com.dbschools.mgb.snippet.{Photos, Authenticator}

class Boot {
  def boot(): Unit = {
    import bootstrap.liftweb.ApplicationPaths._

    LiftRules.liftRequest.append(new LiftRules.LiftRequestPF with Photos {
      val lastPathPart = ~opPdir.flatMap(_.split("/").lastOption)

      def isDefinedAt(r: Req) = test(r)

      def apply(r: Req) = test(r)

      private def test(r: Req) = {
        val path = r.path.wholePath
        val l = path.size
        if (l >= 2 && path(l - 2) == lastPathPart)
          r.request.session.attribute("loggedIn") == null
        else
          false
      }
    })

    // where to search snippet
    LiftRules.addToPackages("com.dbschools.mgb")

    val loggedIn    = If(() => Authenticator.loggedIn,   "Not logged in")
    val notLoggedIn = If(() => ! Authenticator.loggedIn, "Already logged in")

    // Build SiteMap
    def sitemap = SiteMap(
      home.menu,
      logIn.menu                >> notLoggedIn,
      groups.menu               >> loggedIn,
      noGroups.menu             >> loggedIn,
      students.menu             >> loggedIn,
      testing.menu              >> loggedIn,
      editStudent.menu          >> loggedIn >> Hidden,
      studentDetails.menu       >> loggedIn >> Hidden,
      activity.menu             >> loggedIn,
      graphs.menu               >> loggedIn >> Hidden,
      stats.menu                >> loggedIn,
      history.menu              >> loggedIn >> Hidden,
      instrumentsList.menu      >> loggedIn >> Hidden,
      instrumentsCreate.menu    >> loggedIn >> Hidden,
      instrumentsDelete.menu    >> loggedIn >> Hidden,
      instrumentsEdit.menu      >> loggedIn >> Hidden,
      instrumentsView.menu      >> loggedIn >> Hidden,
      studentImport.menu        >> loggedIn >> Hidden,
      logout.menu               >> loggedIn
    )

    LiftRules.setSiteMap(sitemap)

    //Init the FoBo - Front-End Toolkit module,
    //see http://liftweb.net/lift_modules for more info
    FoBo.InitParam.JQuery=FoBo.JQuery191
    FoBo.InitParam.ToolKit=FoBo.Bootstrap300
    FoBo.init()

    //Show the spinny image when an Ajax call starts
    LiftRules.ajaxStart = Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)
    
    // Make the spinny image go away when it ends
    LiftRules.ajaxEnd = Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

    // What is the function to test if a user is logged in?
    LiftRules.loggedInTest = Full(() => Authenticator.loggedIn)

    // Use HTML5 for rendering
    LiftRules.htmlProperties.default.set((r: Req) => new Html5Properties(r.userAgent))

    Db.initialize()
    Cache.init()
    Flot.init()
  }
}
