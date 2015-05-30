package com.dbschools.mgb
package model

import scalaz._, Scalaz._
import org.apache.log4j.Logger
import snippet.Authenticator

trait UserLoggable {
  private val log = Logger.getLogger(getClass)

  def info(msg: String): Unit = {
    log.info((~Authenticator.opLoggedInUser.map(_.name + " ")) + msg)
  }
}
