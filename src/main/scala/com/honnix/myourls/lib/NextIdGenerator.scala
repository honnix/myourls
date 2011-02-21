/**
 * Created : 02.21, 2011
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

import actors.Actor
import DependencyFactory.NextIdMetaRecord

trait NextIdGenerator extends Actor

object DefaultNextIdGenerator extends NextIdGenerator {
  def act {
    loop {
      react {
        case 'id =>
          val nextId = DependencyFactory.inject[NextIdMetaRecord].open_!
          val records = nextId.findAll
          val id = if (records.isEmpty) {
            nextId.createRecord.next("2").save
            "1"
          } else {
            val record = records.head
            val newId = record.next.value
            record.next((Integer.parseInt(newId, 16) + 1).toHexString.toUpperCase).save
            newId
          }
          reply(id)
        case 'exit => exit
      }
    }
  }
}

}

}
