package com.dbschools.mgb.convert

import net.liftweb.util.BCrypt
import org.squeryl.PrimitiveTypeMode._
import net.liftweb.common.Loggable
import com.dbschools.mgb.Db
import com.dbschools.mgb.schema.AppSchema

/** Do a one-time encryption of the plaintext passwords in the users table */
object EncryptPasswords extends Loggable {
  def apply() {
    Db.initialize()
    transaction {
      val query = AppSchema.users.where(_.epassword === "")
      val updatedUsers = query.map(u => u.copy(epassword = BCrypt.hashpw(u.password, BCrypt.gensalt())))
      if (updatedUsers.isEmpty)
        logger.info("All passwords are already encrypted")
      else {
        logger.info("Encrypting passwords for " + updatedUsers.map(_.login).toSeq.sorted.mkString(", "))
        AppSchema.users update updatedUsers
      }
    }
  }
}
