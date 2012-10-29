package com.dbschools.mgb.snippet

import net.liftweb.http.{RequestVar, S, SessionVar, SHtml}
import net.liftweb.util.Helpers._
import bootstrap.liftweb.RunState
import xml.NodeSeq

class Authenticator {

  def authForm =
    "#userName *" #> SHtml.text(Authenticator.userName, name => Authenticator.userName(name.trim), "id" -> "userName") &
    "#password"   #> SHtml.password("", Authenticator.password(_),  "id" -> "password") &
    "#submit"     #> SHtml.submit("Log In", () => {
      if (true) {  // todo put authentication here
        RunState.loggedIn(true)
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
    RunState.loggedIn(false)
    userName("")
  }

}