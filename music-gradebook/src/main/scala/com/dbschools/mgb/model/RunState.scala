package com.dbschools.mgb
package model

import net.liftweb.http.SessionVar
import schema.User

object RunState {
  object loggedInUser extends SessionVar[Option[User]](None)
  def loggedIn = loggedInUser.is.nonEmpty
}
