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
package com.honnix.myourls.view

import net.liftweb.http.{S, LiftView}
import net.liftweb.http.S._
import com.honnix.myourls.model.ShortenedUrl

/**
 * Shortener view who does the real job.
 *
 * @author honnix
 */
class Shortener extends LiftView {
  val AdminPage = "/index"

  def dispatch = {
    case id: String if id.matches("\\w+") =>
      import net.liftweb.json.JsonDSL._
      val record = ShortenedUrl.find(ShortenedUrl.linkId.name -> id)
      val url = if (record.isDefined) record.open_!.originUrl.value else AdminPage
      redirectTo(url)
    case _ => redirectTo(AdminPage)
  }
}
