/**
 * Created : 02.11, 2011
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

import org.specs._
import net.liftweb._
import http._
import net.liftweb.util._
import net.liftweb.common._
import org.specs.matcher._
import org.specs.specification._
import Helpers._
import runner.{JUnit4, ConsoleRunner}

import com.honnix.myourls.view.Shortener

class ShortenerTestSpecsAsTest extends JUnit4(ShortenerTestSpecs)

object ShortenerTestSpecsRunner extends ConsoleRunner(ShortenerTestSpecs)

object ShortenerTestSpecs extends Specification {
  val session = new LiftSession("", randomString(20), Empty)
  
  var shortner: Shortener = _

  override def executeExpectations(ex: Examples, t: => Any): Any = {
    S.initIfUninitted(session) {
      super.executeExpectations(ex, t)
    }
  }
  
  override def beforeExample(ex: Examples) {
    shortner = new Shortener
  }

  "Shortner" should {
    "redirect me to /index if i input not valid path" in {
      new Shortener
    }
  }
}
