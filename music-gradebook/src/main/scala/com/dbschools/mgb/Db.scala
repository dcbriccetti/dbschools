package com.dbschools.mgb

import com.dbschools.mgb.schema.SchemaHelper

import model.DefaultDataCreator
import net.liftweb.http.S
import net.liftweb.squerylrecord.RecordTypeMode.inTransaction
import net.liftweb.util.LoanWrapper
import net.liftweb.util.Props

object Db {

  /**
   * Initialize database processing for the webapp, and for standalone tests.
   */
  def initialize(): Unit = {
    val dbEngine = Props.get("db.engine", "h2")
    if (dbEngine == "h2") {
      SchemaHelper.initH2()
    } else if (dbEngine == "postgres") {
      SchemaHelper.initPostgres()
    }

    if (Props.getBool("db.recreate", defVal = false)) {
      SchemaHelper.recreateSchema()
      DefaultDataCreator.createIfEmpty()
      if (Props.getBool("db.development", defVal = false)) {
        TestDataMaker.createTestData()
      }
    } else {
      SchemaHelper.touch()
    }

    /* Make transaction wrap around the whole HTTP request */
    S.addAround(new LoanWrapper {
      override def apply[T](f: ⇒ T): T = {
        inTransaction { f }
      }
    })
  }
}
