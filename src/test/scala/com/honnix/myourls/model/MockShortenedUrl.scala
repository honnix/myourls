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

import net.liftweb.mongodb.record.MongoMetaRecord
import net.liftweb.common.{Box, Full, Empty}
import net.liftweb.json.JsonAST.JObject

object MockShortenedUrl extends ShortenedUrl with MongoMetaRecord[ShortenedUrl] {
  override def find(json: JObject): Box[ShortenedUrl] = {
    if ("1" == json.obj.head.value.values)
      Full(ShortenedUrl.createRecord.originUrl("http://google.com"))
    else Empty
  }
}

}

}


