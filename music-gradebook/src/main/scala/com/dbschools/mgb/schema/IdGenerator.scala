package com.dbschools.mgb.schema

import org.squeryl.Session

/** Temporary ID generator, while we coexist with the desktop app and Hibernate. */
object IdGenerator {
  def genId() = {
    val stmt = Session.currentSession.connection.createStatement
    val res  = stmt.executeQuery("select nextval('hibernate_sequence')")
    res.next
    res.getInt(1)
  }
}
