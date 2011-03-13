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
import Helpers._
import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.http.provider._
import net.liftweb.sitemap._
import net.liftweb.sitemap.Loc._
import net.liftweb.mongodb._

import com.honnix.myourls.constant.SystemConstant._
import com.honnix.myourls.lib._
import com.honnix.myourls.api.AdminAPI

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment.
 *
 * @author honnix
 */
class Boot {
  private def makeUtf8(req: HTTPRequest) {
    req.setCharacterEncoding("UTF-8")
  }

  private def addUnloadHooks {
    LiftRules.unloadHooks.append(() => DependencyFactory.inject[NextIdGenerator].open_! ! 'exit)
  }

  def boot {
    MongoDB.defineDb(DefaultMongoIdentifier,
      MongoAddress(MongoHost(Props.get("db.host") openOr "localhost",
        (Props.get("db.port") openOr "27017").toInt),
        Props.get("db.name") openOr ProductName))

    // where to search snippet
    LiftRules.addToPackages("com.honnix.myourls")

    def sitemap() = SiteMap(
      Menu("admin", "Admin") / "index",
      Menu("shortener", "Shortener") / "shortener" / **)

    LiftRules.setSiteMapFunc(sitemap _)

    // rest API
    LiftRules.dispatch.append(AdminAPI)

    /*
     * Rewrite http://server/<id> to http://server/shortener/<id>
     */
    LiftRules.statelessRewrite.append {
      case RewriteRequest(ParsePath(List(id), "", _, _), _, _) if "index" != id =>
        RewriteResponse(List("shortener", id))
    }

    // define resource bundle base name 
    LiftRules.resourceNames = List(ProductName)

    LiftRules.noticesAutoFadeOut.default.set((x: NoticeType.Value) => Full((1 seconds, 2 seconds)))

    LiftRules.htmlProperties.default.set((x: Req) => new Html5Properties(x.userAgent))

    DependencyFactory.inject[NextIdGenerator].open_!.start

    LiftRules.early.append(makeUtf8)
  }
}
