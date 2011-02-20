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
package com.honnix.myourls {
package model {

import net.liftweb.mongodb.record.{MongoMetaRecord, MongoId, MongoRecord}
import net.liftweb.mongodb.record.field.DateField
import net.liftweb.record.field.{IntField, StringField}

class ShortenedUrl extends MongoRecord[ShortenedUrl] with MongoId[ShortenedUrl] {
  def meta = ShortenedUrl

  object linkId extends StringField(this, 10)

  object originUrl extends StringField(this, 500)

  object shortUrl extends StringField(this, 100)

  object date extends DateField(this)

  object ip extends StringField(this, 15)

  object clickCount extends IntField(this)
}

object ShortenedUrl extends ShortenedUrl with MongoMetaRecord[ShortenedUrl]
  
class NextId extends MongoRecord[NextId] with MongoId[NextId] {
  def meta = NextId
  
  object next extends StringField(this, 10)
}
  
object NextId extends NextId with MongoMetaRecord[NextId]

}

}
