package com.dbschools.mgb.convert

import net.liftweb.util.BCrypt
import org.squeryl.PrimitiveTypeMode._
import com.dbschools.mgb.Db
import com.dbschools.mgb.schema.AppSchema

/** Do a one-time encryption of the plaintext passwords in the users table */
object EncryptPasswords {
  def apply() {
    Db.initialize()
    transaction {
      val updatedUsers = AppSchema.users.map(u => u.copy(epassword = BCrypt.hashpw(u.password, BCrypt.gensalt())))
      AppSchema.users update updatedUsers
    }
  }
}
