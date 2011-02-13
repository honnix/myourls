/**
 * Created : 02.09, 2011
 *
 * Copyright : (C) 2011 by Honnix
 * Email     : hxliang1982@gmail.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package bootstrap.liftweb

import net.liftweb.util._
import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.http.provider._
import net.liftweb.sitemap._
import net.liftweb.sitemap.Loc._
import net.liftweb.mongodb._

import com.honnix.myourls.constant.SystemConstant._

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment.
 *
 * @author honnix
 */
class Boot {
  def boot {
    // define mongodb connection
    MongoDB.defineDb(DefaultMongoIdentifier,
      MongoAddress(MongoHost(Props.get("db.host") openOr "localhost",
        (Props.get("db.port") openOr "27017").toInt),
        Props.get("db.name") openOr ProductName))

    // where to search snippet
    LiftRules.addToPackages("com.honnix.myourls")

    // Build SiteMap
    def sitemap() = SiteMap(
      Menu("admin", "Admin") / "index",
      Menu(Loc("shortener", Link(List("shortener"), true, ""),
        "Shortener", Hidden)))

    LiftRules.setSiteMapFunc(sitemap _)

    /*
     * Show the spinny image when an Ajax call starts
     */
    LiftRules.ajaxStart =
            Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)

    /*
     * Make the spinny image go away when it ends
     */
    LiftRules.ajaxEnd =
            Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    /*
     * Rewrite http://server/<id> to http://server/shortener/<id>
     */
    LiftRules.statelessRewrite.append {
      case RewriteRequest(ParsePath(List(id), "", _, _), _, _) if "index" != id =>
        RewriteResponse(List("shortener", id))
    }

    // define resource bundle base name 
    LiftRules.resourceNames = List(ProductName)

    LiftRules.early.append(makeUtf8)
  }

  /**
   * Force the request to be UTF-8
   */
  private def makeUtf8(req: HTTPRequest) {
    req.setCharacterEncoding("UTF-8")
  }
}
