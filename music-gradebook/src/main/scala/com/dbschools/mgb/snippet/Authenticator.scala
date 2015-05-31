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
import schema.{User, AppSchema}
import model.{Cache, testingState}

class Authenticator extends FormHelper {
  val log = Logger.getLogger(getClass)
  import bootstrap.liftweb.ApplicationPaths._
  import Authenticator.isDemo

  private var userId = ""
  private var password = ""

  def userText(id: String, label: String) = FocusOnLoad(SHtml.text(userId, (u) => userId = u.trim, attrs(id, label): _*))

  def passwordText(id: String, label: String) = SHtml.password("", password = _, attrs(id, label): _*)

  def authForm =
    "#userFormGroup"      #> addFormGroup("userName", "User Name", userText) &
    "#passwordFormGroup"  #> addFormGroup("password", "Password", passwordText) &
    "#submit"             #> SHtml.submit("Log In", () => {
      val opUser = AppSchema.users.where(user => user.login === userId and user.enabled === true).headOption
      if (opUser.nonEmpty && (isDemo || opUser.exists(user => BCrypt.checkpw(password, user.password)))) {
        Authenticator svLoggedInUser opUser
        S.containerRequest.foreach(_.session.setAttribute("loggedIn", true))
        log.info(s"$userId logged in")
        S.redirectTo((if (testingState.enqueuedMusicians.nonEmpty) testing else groups).href)
      } else {
        log.info(s"$userId failed to log in")
        S.error("Login failed")
      }
    })

  def demoMsg = if (isDemo) PassThru else ClearNodes

  def organization = if (isDemo) ClearNodes else "#organization" #> Text(Authenticator.org)

  def logOut = {
    Authenticator svLoggedInUser None
    S.containerRequest.foreach(_.session.removeAttribute("loggedIn"))
    S.redirectTo(logIn.href)
  }

  def goToLogin = Script(if (Authenticator.svLoggedInUser.is.nonEmpty) Noop else RedirectTo(logIn.href))
}

object Authenticator {
  val org = ~Props.get("organization").toOption
  val isDemo = org == "demo"
  object svLoggedInUser extends SessionVar[Option[User]](None)
  def loggedIn = svLoggedInUser.is.nonEmpty
  def opLoggedInUser = svLoggedInUser.is
  private def userIn(ids: Set[Int]) = opLoggedInUser.map(u => ids contains u.id) | false
  def canWrite = userIn(Cache.canWriteUsers)
  def isAdmin  = userIn(Cache.adminUsers)

  def metronome(num: Int): Unit = {
    opLoggedInUser.foreach(user => {
      val newUser = user.copy(metronome = num)
      AppSchema.users.update(newUser)
      svLoggedInUser(Some(newUser))
    })
  }

  def metronome = opLoggedInUser.map(_.metronome) | 1
}
