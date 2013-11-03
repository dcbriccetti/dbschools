package com.dbschools.mgb.snippet

import scala.xml.Text
import org.apache.log4j.Logger
import scalaz._
import Scalaz._
import org.squeryl.PrimitiveTypeMode._
import net.liftweb.http.{S, SessionVar, SHtml}
import net.liftweb.util.Helpers._
import net.liftweb.util.{PassThru, ClearNodes, Props, BCrypt}
import net.liftweb.http.js.JsCmds.FocusOnLoad
import bootstrap.liftweb.RunState
import com.dbschools.mgb.schema.AppSchema

class Authenticator {
  val log = Logger.getLogger(getClass)
  import bootstrap.liftweb.ApplicationPaths._
  import Authenticator.{credentialsValid, userName}
  
  var password = ""

  def authForm =
    "#userName"   #> FocusOnLoad(SHtml.text(userName.is, name => userName(name.trim), "id" -> "userName")) &
    "#password"   #> SHtml.password("", password = _, "id" -> "password") &
    "#submit"     #> SHtml.submit("Log In", () => {
      if (credentialsValid(password)) {
        RunState loggedIn true
        log.info(s"${userName.is} logged in")
        S.redirectTo(students.href)
      } else {
        log.info(s"${userName.is} failed to log in")
        S.error("Login failed")
      }
    }, "id" -> "submit")

  def demoMsg = if (Authenticator.isDemo) PassThru else ClearNodes

  def organization = if (Authenticator.isDemo) ClearNodes else "#organization" #> Text(Authenticator.org)

  def logOut = {
    Authenticator.logOut()
    S.redirectTo(students.href)
    PassThru
  }
}

object Authenticator {
  private val startingUserName = if (Authenticator.isDemo) "jdoe" else ""

  object userName extends SessionVar(startingUserName)

  def logOut(): Unit = {
    RunState loggedIn false
    userName(startingUserName)
  }

  private def credentialsValid(password: String) =
    AppSchema.users.where(user => user.login === userName.is and user.enabled === true).exists(user =>
      BCrypt.checkpw(password, user.epassword))

  val org = ~Props.get("organization").toOption
  val isDemo = org == "demo"
}
