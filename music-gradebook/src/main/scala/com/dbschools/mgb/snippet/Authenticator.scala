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
  import Authenticator.isDemo

  var userId = ""
  var password = ""

  def userText(id: String, label: String) = FocusOnLoad(SHtml.text(userId, (u) => userId = u.trim, attrs(id, label): _*))

  def passwordText(id: String, label: String) = SHtml.password("", password = _, attrs(id, label): _*)

  def authForm =
    "#userFormGroup"      #> addFormGroup("userName", "User Name", userText) &
    "#passwordFormGroup"  #> addFormGroup("password", "Password", passwordText) &
    "#submit"             #> SHtml.submit("Log In", () => {
      val opUser = AppSchema.users.where(user => user.login === userId and user.enabled === true).headOption
      if (opUser.nonEmpty && (isDemo || opUser.exists(user => BCrypt.checkpw(password, user.epassword)))) {
        RunState loggedInUser opUser
        log.info(s"$userId logged in")
        S.redirectTo(groups.href)
      } else {
        log.info(s"$userId failed to log in")
        S.error("Login failed")
      }
    })

  def demoMsg = if (isDemo) PassThru else ClearNodes

  def organization = if (isDemo) ClearNodes else "#organization" #> Text(Authenticator.org)

  def logOut = {
    RunState loggedInUser None
    S.redirectTo(logIn.href)
  }

  def goToLogin = Script(if (RunState.loggedInUser.is.nonEmpty) Noop else RedirectTo(logIn.href))
}

object Authenticator {
  val org = ~Props.get("organization").toOption
  val isDemo = org == "demo"
}
