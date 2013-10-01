package com.dbschools.mgb.schema

import java.sql.SQLException
import org.squeryl.Session
import net.liftweb.common.Loggable
import net.liftweb.squerylrecord.RecordTypeMode.transaction
import net.liftweb.squerylrecord.SquerylRecord

/**
 * Initializes a DB connection and setup the connection pool.
 */
object SchemaHelper extends Loggable {

  /** Initializes schema with a H2 DB. */
  def initH2(): Unit = {
    initSquerylRecord(new H2Settings)
  }

  /** Initializes schema with a PostgreSQL DB. */
  def initPostgres(): Unit = {
    initSquerylRecord(new PostgresSettings)
  }

  /** Initializes the connection pool only. */
  def touch(): Unit = {
    transaction {}
  }

  /** Drops and creates the schema. */
  def recreateSchema(): Unit = {
    transaction {
      try {
        AppSchema.printDdl
        AppSchema.drop
        AppSchema.create
      } catch {
        case exception: SQLException ⇒ {
          val msg = "Failed to recreate the schema."
          logger.error(msg, exception)
          throw new Exception(msg + " " + exception.getMessage)
        }
      }
    }
  }

  /**
   * Initializes Squeryl Record given some custom DB settings.
   * @param settings the custom settings to be used during DB connection initialization 
   */
  private def initSquerylRecord(settings: DbSettings): Unit = {
    logger.trace(s"initSquerylRecord with Settings: driver=${settings.driver} url=${settings.url} user=${settings.user}")
    SquerylRecord.initWithSquerylSession {
      Class.forName(settings.driver)
      Session.create(BoneProvider.getConnection(settings), settings.adapter)
    }
  }
}
