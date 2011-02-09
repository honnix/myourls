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

/**
 * Shortener view who does the real job.
 *
 * @author honnix
 */
class Shortener extends LiftView {
  val map = Map("1" -> "http://google.com",
    "2" -> "http://honnix.com")

  def dispatch = {
    case id if map.contains(id) => S.redirectTo(map(id))
    case _ => S.redirectTo("/index")
  }
}
