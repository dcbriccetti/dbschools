package com.dbschools.mgb.schema

import org.squeryl.adapters.H2Adapter
import org.squeryl.adapters.PostgreSqlAdapter
import org.squeryl.internals.DatabaseAdapter

import net.liftweb.common.Loggable
import net.liftweb.util.Props

/** 
 * Settings used to set up a DB Connection.
 */
trait DbSettings {
  def adapter: DatabaseAdapter
  private def prop(name: String) = Props.get(name).get
  def driver    = prop("db.driver")
  def url       = prop("db.url")
  def user      = prop("db.user")
  def password  = prop("db.password")
}

/** 
 * Custom settings for H2
 */
class H2Settings extends DbSettings with Loggable {
  val adapter = new H2Adapter
  logger.trace(s"H2Settings: setting up H2 Adapter. driver=$driver url=$url user=$user")
}

/**
 * Custom settings for Postgres
 */
class PostgresSettings extends DbSettings with Loggable {
  val adapter = new PostgreSqlAdapter
  logger.trace(s"PostgresSettings: setting up Posgtres Adapter. driver=$driver url=$url user=$user")
}
