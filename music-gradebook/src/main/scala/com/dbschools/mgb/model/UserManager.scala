package com.dbschools.mgb
package model

import org.squeryl.PrimitiveTypeMode._
import schema.{User, UserRole, AppSchema}

object UserManager {
  def addOrUpdate(user: User, canWrite: Boolean = false): Unit = {
    val userId = AppSchema.users.where(_.login === user.login).headOption match {
      case Some(existingUser) =>
        AppSchema.users.update(user.copy(id = existingUser.id))
        existingUser.id
      case _ =>
        AppSchema.users.insert(user).id
    }
    val existingCanWrite = AppSchema.userRoles.where(userCanWrite(userId)).nonEmpty
    if (canWrite && ! existingCanWrite)
      AppSchema.userRoles.insert(UserRole(userId, Roles.Write.id))
    else if (! canWrite && existingCanWrite)
      AppSchema.userRoles.deleteWhere(userCanWrite(userId))
  }

  private def userCanWrite(userId: Int)(ur: UserRole) = ur.userId === userId and ur.roleId === Roles.Write.id
}
