package bootstrap.liftweb

import net.liftweb._
import common._
import http._
import sitemap._
import Loc._
import net.liftmodules.widgets.flot.Flot
import net.liftmodules.FoBo

import com.dbschools.mgb.Db
import com.dbschools.mgb.model.Cache
import akka.actor.{PoisonPill, Props, ActorSystem}
import com.dbschools.mgb.comet.TestSchedulerActor

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
      groups.menu               >> loggedIn,
      noGroups.menu             >> loggedIn >> Hidden,
      students.menu             >> loggedIn,
      testing.menu              >> loggedIn,
      newStudent.menu           >> loggedIn >> Hidden,
      studentDetails.menu       >> loggedIn >> Hidden,
      activity.menu             >> loggedIn,
      graphs.menu               >> loggedIn,
      stats.menu                >> loggedIn,
      history.menu              >> loggedIn,
      instrumentsList.menu      >> loggedIn,
      instrumentsCreate.menu    >> loggedIn >> Hidden,
      instrumentsDelete.menu    >> loggedIn >> Hidden,
      instrumentsEdit.menu      >> loggedIn >> Hidden,
      instrumentsView.menu      >> loggedIn >> Hidden,
      logout.menu               >> loggedIn
    )

    LiftRules.setSiteMap(sitemap)

    //Init the FoBo - Front-End Toolkit module,
    //see http://liftweb.net/lift_modules for more info
    FoBo.InitParam.JQuery=FoBo.JQuery191
    FoBo.InitParam.ToolKit=FoBo.Bootstrap300
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
    Cache.init()
    Flot.init()
  }
}

object Actors {
  val system = ActorSystem()
  val testScheduler = system.actorOf(Props[TestSchedulerActor], "testScheduler")
  private val all = Seq(testScheduler)

  def stop(): Unit = {
    all.foreach(_ ! PoisonPill)
  }
}
