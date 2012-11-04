package com.dbschools.mgb.snippet

import xml.NodeSeq
import org.squeryl.PrimitiveTypeMode._
import net.liftweb.http.{RequestVar, S, SessionVar, SHtml}
import net.liftweb.util.Helpers._
import net.liftweb.util.BCrypt
import bootstrap.liftweb.RunState
import com.dbschools.mgb.schema.AppSchema

class Authenticator {

  def authForm =
    "#userName *" #> SHtml.text(Authenticator.userName, name => Authenticator.userName(name.trim), "id" -> "userName") &
    "#password"   #> SHtml.password("", Authenticator.password(_),  "id" -> "password") &
    "#submit"     #> SHtml.submit("Log In", () => {
      if (AppSchema.users.where(_.login === Authenticator.userName.get).exists(user =>
        BCrypt.checkpw(Authenticator.password.get, user.epassword))) {
        RunState loggedIn true
        S.redirectTo("/index")
      } else {
        S.error("Login failed")
      }
    }, "id" -> "submit")

  def logOut(content: NodeSeq): NodeSeq = {
    Authenticator.logOut()
    S.redirectTo("index")
    content
  }
}

object Authenticator {
  object userName extends SessionVar("")
  object password extends RequestVar("")

  def logOut() {
    RunState loggedIn false
    userName("")
  }
}
