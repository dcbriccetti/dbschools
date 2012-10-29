package com.dbschools.mgb.schema

import java.sql.Connection

import com.jolbox.bonecp.BoneCP
import com.jolbox.bonecp.BoneCPConfig

import net.liftweb.common.Loggable
import net.liftweb.http.LiftRules
import net.liftweb.http.LiftRulesMocker.toLiftRules

/** Connection Provider implemented with BoneCP.
 * 
 * A mutable attribute [[com.dbschools.mgb.schema.ConnectionProvider#pool]] is used to maintain the connection pool. As previously
 * documented in [[com.dbschools.mgb.schema.ConnectionProvider]], [[com.dbschools.mgb.schema.ConnectionProvider#init(DbSettings)]] 
 * should be invoked only once. Doing this way the pool attribute '''race condition free'''. 
 * 
 * @since 1.0.0
 */
object BoneProvider extends ConnectionProvider with Loggable {
  /** Mutable connection pool.
   * 
   * @note Special care must be taken to invoke [[com.dbschools.mgb.schema.ConnectionProvider#init(DbSettings)]] only once.
   */
  private var pool: BoneCP = null
  
  /** @inheritdoc */
  def isInitialized = pool != null
  
  /** @inheritdoc */
  def init(settings: DbSettings) {
    val config = new BoneCPConfig
    config.setJdbcUrl(settings.url)
    config.setUsername(settings.user)
    config.setPassword(settings.password)

    try {
      Class.forName(settings.driver)

      pool = new BoneCP(config)

      LiftRules.unloadHooks.append(() ⇒ {
        goodCitizen; logger.info("Good citizen: closed connection pool.")
      })
      logger.info("BoneCP connection pool properly initialized.")
    }
    catch {
      case e: Exception ⇒ {
        logger.error("BoneCP Connection pool setup has failed.")
        throw new Exception("Failed to initialize connection pool." + e.printStackTrace)
      }
    }

    /** Cleans-up the connection pool. */
    def goodCitizen = {
      pool.shutdown
    }
  }
  
  /** @inheritdoc */
  def getConnection(): Connection = {
    if(isInitialized){
      pool.getConnection
    } else {
      logger.error("Tried to get a connection without initializing the connection provider first")
      throw new IllegalStateException("Connection provider was not initialized.")
    }
  }
}