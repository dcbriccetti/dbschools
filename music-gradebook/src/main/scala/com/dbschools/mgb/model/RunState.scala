package com.dbschools.mgb.model

import net.liftweb.http.SessionVar

object RunState {
  object loggedIn extends SessionVar[Boolean] (false)
}
