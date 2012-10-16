package com.dbschools.mgb

import java.sql.{Connection, DriverManager}
import org.squeryl.adapters.PostgreSqlAdapter
import org.squeryl.{SessionFactory, Session}

object Db {

  /**
   * Initialize database processing for the webapp, and for standalone tests.
   */
  def initialize() {
    Class.forName("org.postgresql.Driver")
    SessionFactory.concreteFactory = Some(() =>
      Session.create(
        DriverManager.getConnection(
          "jdbc:postgresql://localhost:5432/dbsmusic?user=dbschools", "", ""),
        new PostgreSqlAdapter))
  }
}
