package bootstrap.liftweb
import net.liftweb._
import sitemap._
import Loc._

import sitemap.Menu._

/** A path to a specific functionality of this application. Every Path is lately used to construct the SiteMap
 * 
 * @param name name given to this path
 * @param url partial url related to the functionality of this path. Example: instruments/list 
 */
class Path (name: String, url: String) {
  lazy val menu = generateMenu(Menu.i(name), getPathList)
  lazy val href: String = menu.loc.calcDefaultHref
  
  private def getPathList = {
    url.split("/").toList
  }
  
  private def generateMenu(preMenu: PreMenu, paths: List[String]): Menuable with WithSlash = {
    def generateMenu0(paths: List[String], accum: Menuable with WithSlash): Menuable with WithSlash = paths match {
      case List() => accum
      case x::xs => generateMenu0(xs, accum / x)
    }

    generateMenu0(paths.tail, preMenu / paths.head)
  } 
}