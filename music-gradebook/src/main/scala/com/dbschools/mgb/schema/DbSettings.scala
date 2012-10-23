package com.dbschools.mgb.schema

import org.squeryl.adapters.H2Adapter
import org.squeryl.adapters.PostgreSqlAdapter
import org.squeryl.internals.DatabaseAdapter

import net.liftweb.common.Loggable
import net.liftweb.util.Props

/** Settings used to setup a DB Connection.
 * 
 * @since 1.0.0
 */
trait DbSettings {
    def adapter: DatabaseAdapter
    def driver: String
    def url: String
    def user: String
    def password: String
}

/** Custom settings for H2
 * @since 1.0.0
 */
class H2Settings extends DbSettings with Loggable {
  override val adapter = new H2Adapter;
  override val driver = Props.get("db.driver") openOr "org.h2.Driver"
  override val url = Props.get("db.url") openOr "jdbc:h2:database/dbsmusic"
  override val user = Props.get("db.user") openOr "sa"
  override val password = Props.get("db.password") openOr ""
  logger.trace("H2Settings: setting up H2 Adapter. driver=%s url=%s user=%s".format(driver, url, user))
}
  
/** Custom settings for Postgres
 * @since 1.0.0
 */
class PostgresSettings extends DbSettings with Loggable {
  override val adapter = new PostgreSqlAdapter
  override val driver = Props.get("db.driver") openOr "org.postgresql.Driver"
  override val url = Props.get("db.url") openOr "jdbc:postgresql://localhost:5432/dbschools"
  override val user = Props.get("db.user") openOr "root"
  override val password = Props.get("db.password") openOr "secret"
  logger.trace("PostgresSettings: setting up Posgtres Adapter. driver=%s url=%s user=%s".format(driver, url, user))
}