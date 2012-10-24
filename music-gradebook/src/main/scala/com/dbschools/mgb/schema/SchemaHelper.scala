package com.dbschools.mgb.schema

import org.squeryl.Session
import net.liftweb.common.Loggable
import net.liftweb.squerylrecord.RecordTypeMode.transaction
import net.liftweb.squerylrecord.SquerylRecord
import com.dbschools.mgb.TestDataMaker

/**
 * Initializes a DB connection and setup the connection pool.
 */
object SchemaHelper extends Loggable {

  /** Initializes schema with a H2 DB. */
  def initH2() {
    initSquerylRecord(new H2Settings)
  }

  /** Initializes schema with a PostgreSQL DB. */
  def initPostgres() {
    initSquerylRecord(new PostgresSettings)
  }

  /** Initializes the connection pool only. */
  def touch() {
    transaction {}
  }

  /** Drops and creates the schema. */
  def recreateSchema() {
    transaction {
      try {
        AppSchema.printDdl
        AppSchema.drop
        AppSchema.create
        TestDataMaker.feedDbWithDummyData()
      }
      catch {
        case e: Exception ⇒ {
          logger.error("Recreate schema has failed.")
          throw new Exception("Failed to recreate the schema." + e.printStackTrace)
        }
      }
    }
  }

  /**
   * Initializes Squeryl Record given some custom DB settings.
   * @param settings the custom settings to be used during DB connection initialization 
   */
  private def initSquerylRecord(settings: DbSettings) {
    logger.trace("initSquerylRecord with Settings: driver=%s url=%s user=%s".format(settings.driver, settings.url, settings.user))
    SquerylRecord.initWithSquerylSession {
      Class.forName(settings.driver)
      Session.create(BoneProvider.getConnection(settings), settings.adapter)
    }
  }
}
