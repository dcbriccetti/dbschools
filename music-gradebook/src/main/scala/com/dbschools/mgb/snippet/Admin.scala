package com.dbschools.mgb
package snippet

import net.liftweb._
import http._
import util.Helpers._
import SHtml.{text, password, checkbox, ajaxSubmit}
import convert.EncryptPasswords
import model.UserManager
import schema.User

class Admin {

  var user = User(0, "", "", "", "", enabled = true, 1)
  var canWrite = false

  def process(): Unit = {
    UserManager.addOrUpdate(user, canWrite)
  }

  def addUser = {
    "#userName"   #> text(user.login,       v => user = user.copy(login = v)) &
    "#password"   #> password("",           v => user = user.copy(password = EncryptPasswords.encrypt(v))) &
    "#canWrite"   #> checkbox(canWrite,     canWrite = _, "id" -> "canWrite") &
    "#firstName"  #> text(user.first_name,  v => user = user.copy(first_name = v)) &
    "#lastName"   #> text(user.last_name,   v => user = user.copy(last_name  = v)) &
    "type=submit" #> ajaxSubmit("Submit", process)
  }
}
