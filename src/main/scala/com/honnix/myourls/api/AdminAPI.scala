package com.honnix.myourls.api

/**
 * Created : 03.12, 2011
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

import net.liftweb.http.rest.RestHelper
import net.liftweb.http.S
import net.liftweb.json.JsonDSL._
import com.honnix.myourls.lib.DependencyFactory
import DependencyFactory.ShortenedUrlMetaRecord
import com.honnix.myourls.model.ShortenedUrl
import net.liftweb.json.JsonAST.JObject
import net.liftweb.common._

object AdminAPI extends RestHelper with Loggable {
  val StatusField = "status"

  val SuccessStatus = "successful"

  val FailedStatus = "failed"

  val shortenedUrl = DependencyFactory.inject[ShortenedUrlMetaRecord].open_!

  private def add = {
    null
  }

  private def delete = {
    null
  }

  private def edit = {
    null
  }

  private def get: JObject = {
    val record = S.param(ShortenedUrl.linkId.name).map(x => shortenedUrl.find(ShortenedUrl.linkId.name -> x).map(_.originUrl.value))
    (StatusField -> record.map(_.map(x => SuccessStatus)).openOr(Full(FailedStatus)).openOr(FailedStatus)) ~
            (ShortenedUrl.originUrl.name -> record.openOr(Full("")).openOr(""))
  }

  private def list = {
    null
  }

  serve {
    case "api" :: "add" :: Nil JsonGet _ => add
    case "api" :: "delete" :: Nil JsonGet _ => delete
    case "api" :: "edit" :: Nil JsonGet _ => edit
    case "api" :: "get" :: Nil JsonGet _ => get
    case "api" :: "list" :: Nil JsonGet _ => list
  }
}
