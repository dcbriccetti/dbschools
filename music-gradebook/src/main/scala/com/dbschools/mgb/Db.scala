package com.dbschools.mgb

import java.sql.{Connection, DriverManager}
import org.squeryl.adapters.PostgreSqlAdapter
import org.squeryl.{SessionFactory, Session}
import net.liftweb.util.{LoanWrapper, Props}
import net.liftweb.http.{S, LiftRules}
import net.liftweb.squerylrecord.RecordTypeMode._
import scala.Some
import schema.SchemaHelper

object Db {

  /**
   * Initialize database processing for the webapp, and for standalone tests.
   */
  def initialize() {
    val dbEngine = Props.get("db.engine", "h2");
    if (dbEngine == "h2") {
      SchemaHelper.initH2
    } else if (dbEngine == "postgres") {
      SchemaHelper.initPostgres
    }

    if (Props.getBool("db.recreate", false)) {
      SchemaHelper.recreateSchema
    }else {
      SchemaHelper.touch
    }

    /* Make transaction wrap around the whole HTTP request */
    S.addAround(new LoanWrapper {
      override def apply[T](f: ⇒ T): T = {
        inTransaction { f }
      }
    })
  }
}
