package com.dbschools.mgb.schema

import java.sql.Connection

import org.squeryl.internals.DatabaseAdapter
import org.squeryl.adapters.{PostgreSqlAdapter, H2Adapter}
import org.squeryl.Session

import com.jolbox.bonecp.BoneCP
import com.jolbox.bonecp.BoneCPConfig

import net.liftweb.common.{Loggable, Logger}
import net.liftweb.http.LiftRulesMocker.toLiftRules
import net.liftweb.squerylrecord.RecordTypeMode.transaction
import net.liftweb.squerylrecord.SquerylRecord
import net.liftweb.util.Props
import net.liftweb.squerylrecord.RecordTypeMode._

/**
 * Helper object used to initialize DB connection, setup pooling and recreate schema when needed.
 * @since 1.0.0
 */
object SchemaHelper extends Loggable {

  /**
   *  Init schema with H2 DB
   */
  def initH2 {
    initSquerylRecord(new H2Settings)
  }

  /**
   *  Init schema with Postgres DB
   */
  def initPostgres {
    initSquerylRecord(new PostgresSettings)
  }

  /**
   *  Initializes the connection pool.
   */
  def touch {
    transaction {}
  }

  /**
   * Drop and create the schema again.
   */
  def recreateSchema {
    transaction {
      try {
        AppSchema.printDdl
        AppSchema.drop
        AppSchema.create
        DataHelper.createInitialData
      }
      catch {
        case e ⇒
          e.printStackTrace()
          throw e;
      }
    }

  }

  private def initSquerylRecord(settings: Settings) {
    logger.info("initSquerylRecord with Settings: driver=%s url=%s user=%s".format(settings.driver, settings.url, settings.user))
    SquerylRecord.initWithSquerylSession {
      Class.forName(settings.driver)
      Session.create(ConnectionPoolProvider.getPoolConnection(settings), settings.adapter)
    }
  }

  /**
   * Trait for DB Settings
   * @since 1.0.0
   */
  trait Settings {
    def adapter: DatabaseAdapter
    def driver: String
    def url: String
    def user: String
    def password: String
  }

  /**
   * Custom settings for H2
   * @since 1.0.0
   */
  class H2Settings extends Settings with Loggable {
    override val adapter = new H2Adapter;
    override val driver = Props.get("db.driver", "org.h2.Driver")
    override val url = Props.get("db.url", "jdbc:h2:database/testXYZDB;FILE_LOCK=NO")
    override val user = Props.get("db.user", "test")
    override val password = Props.get("db.password", "")
    logger.info("H2Settings: setting up H2 Adapter. driver=%s url=%s user=%s".format(driver, url, user))
  }

  /**
   * Custom settings for Postgres
   * @since 1.0.0
   */
  class PostgresSettings extends Settings with Loggable {
    override val adapter = new PostgreSqlAdapter
    override val driver = Props.get("db.driver", "org.h2.Driver")
    override val url = Props.get("db.url", "")
    override val user = Props.get("db.user", "test")
    override val password = Props.get("db.password", "test")
    logger.info("PostgresSettings: setting up Posgres Adapter. driver=%s url=%s user=%s".format(driver, url, user))
  }

  /**
   *  Connection Pool Provider
   * @since 1.0.0
   */
  object ConnectionPoolProvider extends Loggable {
    def getPoolConnection(settings: Settings): Connection = {
      if (pool == null) {
        pool = init(settings)
      }
      pool.getConnection
    }

    private var pool: BoneCP = null

    private def init(settings: Settings): BoneCP = {
      // create a new configuration object
      val config = new BoneCPConfig
      config.setJdbcUrl(settings.url)
      config.setUsername(settings.user)
      config.setPassword(settings.password)

      try {
        Class.forName(settings.driver)

        val result = new BoneCP(config)

        net.liftweb.http.LiftRules.unloadHooks.append(() ⇒ {
          result.shutdown; logger.info("Good citizen: closed connection pool.")
        })
        logger.info("Connection pool properly initialized.")
        result
      }
      catch {
        case e: Exception ⇒ {
          logger.error("Connection pool setup has failed.")
          throw new Exception("Failed to initialize connection pool." + e.printStackTrace)
        }
      }
    }

    def cleanup = {
      pool.shutdown()
    }
  }
}