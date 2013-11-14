package com.dbschools.mgb.dbconn

import java.sql.Connection

/** Abstract Connection Provider.
 * 
 * Intention is to use the [[ConnectionProvider#getConnection(DbSettings)]] only. Such method is in charge to appropriately invoke
 * [[ConnectionProvider#isInitialized()]], [[ConnectionProvider#init(DbSettings)]] and
 * [[ConnectionProvider#getConnection()]]. Direct use of remaining methods
 * is discouraged.
 */
trait ConnectionProvider {

  /** Returns a connection given custom DB settings.
   * 
   * In case this connection provider was already [[ConnectionProvider#init(DbSettings)]] initialized}, then invoking this
   * method is the same as directly invoking [[ConnectionProvider#getConnection()]].
   *
   * @see [[ConnectionProvider#isInitialized()]]
   * @see [[ConnectionProvider#init(DbSettings)]]
   * @see [[ConnectionProvider#getConnection()]]
   */
  def getConnection(settings: DbSettings): Connection = {
    if (!isInitialized) {
      init(settings)
    }

    getConnection
  }

  /**
   * Used to determine whether the connection provider was initialized.
   */
  def isInitialized: Boolean

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
   * @note pre-condition: [[ConnectionProvider#isInitialized()]] must return true
   * @throws IllegalStateException in case this connection provider was not initialized first
   */
  def getConnection: Connection
}