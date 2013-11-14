package bootstrap.liftweb

import net.liftweb.sitemap.Menu
import net.liftweb.sitemap.Menu.{WithSlash, Menuable, PreMenu}

/** A path to a page, used to construct the SiteMap.
 * 
 * @param name name given to this path
 * @param url partial url related to the functionality of this path. Example: instruments/list 
 */
class Path(name: String, url: String) {
  val menu = generateMenu(Menu.i(name), url.split("/").toList)
  val href = menu.loc.calcDefaultHref
  
  private def generateMenu(preMenu: PreMenu, paths: List[String]): Menuable with WithSlash = {
    def generateMenu0(paths: List[String], accum: Menuable with WithSlash): Menuable with WithSlash = paths match {
      case List() => accum
      case x :: xs => generateMenu0(xs, accum / x)
    }

    generateMenu0(paths.tail, preMenu / paths.head)
  } 
}