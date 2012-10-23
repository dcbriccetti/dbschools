package com.dbschools.mgb.schema

import java.sql.Connection

/** Abstract Connection Provider.
 * 
 * Intention is to use the [[com.dbschools.mgb.schema.ConnectionProvider#getConnection(DbSettings)]] only. Such method is in charge to appropriately invoke 
 * [[com.dbschools.mgb.schema.ConnectionProvider#isInitialized()]], [[com.dbschools.mgb.schema.ConnectionProvider#init(DbSettings)]] and 
 * [[com.dbschools.mgb.schema.ConnectionProvider#getConnection()]]. Direct use of remaining methods
 * is discouraged.
 * 
 * @since 1.0.0
 */
trait ConnectionProvider {

  /** Returns a connection given custom DB settings.
   * 
   * In case this connection provider was already [[com.dbschools.mgb.schema.ConnectionProvider#init(DbSettings)]] initialized}, then invoking this
   * method is the same as directly invoking [[com.dbschools.mgb.schema.ConnectionProvider#getConnection()]].
   *
   * @see [[com.dbschools.mgb.schema.ConnectionProvider#isInitialized()]]
   * @see [[com.dbschools.mgb.schema.ConnectionProvider#init(DbSettings)]]
   * @see [[com.dbschools.mgb.schema.ConnectionProvider#getConnection()]]
   */
  def getConnection(settings: DbSettings): Connection = {
    if (!isInitialized()) {
      init(settings);
    }

    getConnection();
  }

  /**
   * Used to determine whether the connection provider was initialized.
   */
  def isInitialized(): Boolean

  /** Initializes the connection provider.
   * 
   * '''Should be invoked only once''', at application start-up.
   *
   * @param settings custom DB settings to be used during initialization
   */
  def init(settings: DbSettings)

  /** Returns a connection.
   * 
   * Only invoke this method if you are really sure the '''initialization was already done'''.
   *
   * @note pre-condition: [[com.dbschools.mgb.schema.ConnectionProvider#isInitialized()]] must return true
   * @throws IllegalStateException in case this connection provider was not initialized first
   */
  def getConnection(): Connection
}