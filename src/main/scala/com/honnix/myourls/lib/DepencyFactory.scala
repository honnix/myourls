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
package com.honnix.myourls {
package lib {

import net.liftweb._
import http._
import mongodb.record.MongoMetaRecord
import util._
import model.{NextId, ShortenedUrl}

/**
 * A factory for generating new instances of Date.  You can create
 * factories for each kind of thing you want to vend in your application.
 * An example is a payment gateway.  You can change the default implementation,
 * or override the default implementation on a session, request or current call
 * stack basis.
 */
object DependencyFactory extends Factory {
  type ShortenedUrlMetaRecord = MongoMetaRecord[ShortenedUrl]

  type NextIdMetaRecord = MongoMetaRecord[NextId]

  implicit object time extends FactoryMaker(Helpers.now _)

  implicit object shortenedUrl extends FactoryMaker(() => ShortenedUrl.asInstanceOf[ShortenedUrlMetaRecord])

  implicit object nextId extends FactoryMaker(() => NextId.asInstanceOf[NextIdMetaRecord])


  /**
   * objects in Scala are lazily created.  The init()
   * method creates a List of all the objects.  This
   * results in all the objects getting initialized and
   * registering their types with the dependency injector
   */
  private def init() {
    List(time, shortenedUrl, nextId)
  }

  init()
}

}

}
