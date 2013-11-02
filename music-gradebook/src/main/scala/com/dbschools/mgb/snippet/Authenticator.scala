package com.dbschools.mgb.snippet

import xml.NodeSeq
import org.squeryl.PrimitiveTypeMode._
import net.liftweb.http.{S, SessionVar, SHtml}
import net.liftweb.util.Helpers._
import net.liftweb.util.BCrypt
import net.liftweb.http.js.JsCmds.FocusOnLoad
import bootstrap.liftweb.RunState
import com.dbschools.mgb.schema.AppSchema
import org.apache.log4j.Logger

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
        //S.redirectTo(students.href)
      } else {
        log.info(s"${userName.is} failed to log in")
        S.error("Login failed")
      }
    }, "id" -> "submit")

  def logOut(content: NodeSeq): NodeSeq = {
    Authenticator.logOut()
    S.redirectTo(students.href)
    content
  }
}

object Authenticator {
  object userName extends SessionVar("")

  def logOut(): Unit = {
    RunState loggedIn false
    userName("")
  }

  private def credentialsValid(password: String) =
    AppSchema.users.where(user => user.login === userName.is and user.enabled === true).exists(user =>
      BCrypt.checkpw(password, user.epassword))
}
