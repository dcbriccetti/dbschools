package bootstrap.liftweb
import net.liftweb._
import sitemap._
import Loc._

import sitemap.Menu._

class Path (name: String, path: String) {
  lazy val menu = generateMenu(Menu.i(name), getPathList)
  lazy val href: String = menu.loc.calcDefaultHref
  
  private def getPathList = {
    path.split("/").toList
  }
  
  private def generateMenu(preMenu: PreMenu, paths: List[String]): Menuable with WithSlash = {
    def generateMenu0(pat: List[String], accum: Menuable with WithSlash): Menuable with WithSlash = pat match {
      case List() => accum
      case x::xs => generateMenu0(xs, accum / x)
    }

    generateMenu0(paths.tail, preMenu / paths.head)
  } 
}