package com.dbschools.mgb
package snippet

import scala.xml.Text
import org.apache.log4j.Logger
import scalaz._
import Scalaz._
import org.squeryl.PrimitiveTypeMode._
import net.liftweb.http.{S, SessionVar, SHtml}
import net.liftweb.util.Helpers._
import net.liftweb.util.{PassThru, ClearNodes, Props, BCrypt}
import net.liftweb.http.js.JsCmds.{Script, Noop, RedirectTo, FocusOnLoad}
import schema.AppSchema
import model.RunState

class Authenticator extends FormHelper {
  val log = Logger.getLogger(getClass)
  import bootstrap.liftweb.ApplicationPaths._
  import Authenticator.{credentialsValid, isDemo, userName}
  
  var password = ""

  def userText(id: String, label: String) =
    FocusOnLoad(SHtml.text(userName.is, name => userName(name.trim), attrs(id, label): _*))
  def passwordText(id: String, label: String) =
    SHtml.password("", password = _, attrs(id, label): _*)

  def authForm =
    "#userFormGroup"      #> addFormGroup("userName", "User Name", userText) &
    "#passwordFormGroup"  #> addFormGroup("password", "Password", passwordText) &
    "#submit"             #> SHtml.submit("Log In", () => {
      if (isDemo || credentialsValid(password)) {
        RunState loggedIn true
        log.info(s"${userName.is} logged in")
        S.redirectTo(groups.href)
      } else {
        log.info(s"${userName.is} failed to log in")
        S.error("Login failed")
      }
    })


  def demoMsg = if (isDemo) PassThru else ClearNodes

  def organization = if (isDemo) ClearNodes else "#organization" #> Text(Authenticator.org)

  def logOut = {
    Authenticator.logOut()
    S.redirectTo(logIn.href)
    PassThru
  }

  def goToLogin = Script(if (RunState.loggedIn.is) Noop else RedirectTo(logIn.href))
}

object Authenticator {
  val org = ~Props.get("organization").toOption
  val isDemo = org == "demo"
  private val startingUserName = if (isDemo) "a" else ""

  object userName extends SessionVar(startingUserName)

  def logOut(): Unit = {
    RunState loggedIn false
    userName(startingUserName)
  }

  private def credentialsValid(password: String) =
    AppSchema.users.where(user => user.login === userName.is and user.enabled === true).exists(user =>
      BCrypt.checkpw(password, user.epassword))
}
