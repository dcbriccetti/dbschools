package com.dbschools.mgb.snippet

import xml.NodeSeq
import org.squeryl.PrimitiveTypeMode._
import net.liftweb.http.{S, SessionVar, SHtml}
import net.liftweb.util.Helpers._
import net.liftweb.util.BCrypt
import bootstrap.liftweb.RunState
import com.dbschools.mgb.schema.AppSchema

class Authenticator {
  import Authenticator.{credentialsValid, userName}
  var password = ""

  def authForm =
    "#userName"   #> SHtml.text(userName.is, name => userName(name.trim), "id" -> "userName") &
    "#password"   #> SHtml.password("", password = _,  "id" -> "password") &
    "#submit"     #> SHtml.submit("Log In", () => {
      if (credentialsValid(password)) {
        RunState loggedIn true
        S.redirectTo("/index")
      } else
        S.error("Login failed")
    }, "id" -> "submit")

  def logOut(content: NodeSeq): NodeSeq = {
    Authenticator.logOut()
    S.redirectTo("index")
    content
  }
}

object Authenticator {
  object userName extends SessionVar("")

  def logOut() {
    RunState loggedIn false
    userName("")
  }

  private def credentialsValid(password: String) =
    AppSchema.users.where(_.login === userName.is).exists(user => BCrypt.checkpw(password, user.epassword))
}
