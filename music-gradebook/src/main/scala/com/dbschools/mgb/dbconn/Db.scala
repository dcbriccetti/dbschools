package com.dbschools.mgb.dbconn

import com.dbschools.mgb.schema.{AppSchema, SchemaHelper}

import net.liftweb.http.S
import net.liftweb.squerylrecord.RecordTypeMode.{inTransaction, transaction}
import net.liftweb.util.{LiftFlowOfControlException, LoanWrapper, Props}
import com.dbschools.mgb.model.DefaultDataCreator
import com.dbschools.mgb.TestDataMaker

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

    if (Props.getBool("db.recreate", false)) {
      SchemaHelper.recreateSchema()
      DefaultDataCreator.createIfEmpty()
      if (Props.getBool("db.development", false)) {
        TestDataMaker.createTestData()
      }
    } else {
      SchemaHelper.touch()
    }

    S.addAround(new LoanWrapper {
      override def apply[T](f: => T): T = {
        inTransaction {
          try {
            Right(f)
          } catch {
            case e: LiftFlowOfControlException => Left(e)
          }
        } match {
          case Left(e)  => throw e
          case Right(r) => r
        }
      }
    })
  }

  def main(args: Array[String]) {
    initialize()
    transaction { AppSchema.printDdl }
  }
}
