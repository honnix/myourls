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
package com.honnix.myourls {
package api {

import net.liftweb.http.rest.RestHelper
import net.liftweb.http.S._
import net.liftweb.json.JsonDSL._
import lib.DependencyFactory
import DependencyFactory.ShortenedUrlMetaRecord
import com.honnix.myourls.model.ShortenedUrl
import net.liftweb.json.JsonAST.JObject
import net.liftweb.common._
import com.honnix.myourls.lib._
import net.liftweb.util.Props
import constant.SystemConstant._
import java.util.Date

object AdminAPI extends RestHelper {
  val StatusField = "status"

  val SuccessStatus = "successful"

  val FailedStatus = "failed"

  val shortenedUrl = DependencyFactory.inject[ShortenedUrlMetaRecord].open_!

  private def add = {
    val linkId = param(ShortenedUrl.originUrl.name).map(x => {
      shortenedUrl.find(ShortenedUrl.originUrl.name -> x).map(_.linkId.value) or {
        val tmp = (DependencyFactory.inject[NextIdGenerator].open_! !? 'id).toString
        shortenedUrl.createRecord.linkId(tmp).originUrl(x).shortUrl(Props.get(Site).open_! + "/" + tmp).date(new Date).
                ip(containerRequest.map(_.remoteAddress) openOr "unkown").clickCount(0).save
        Full(tmp)
      }
    })
    (StatusField -> linkId.map(_.map(x => SuccessStatus)).openOr(Full(FailedStatus)).openOr(FailedStatus)) ~
            (ShortenedUrl.linkId.name -> linkId.openOr(Full("")).openOr(""))
  }

  private def delete = {
    val linkId = param(ShortenedUrl.linkId.name).map(x =>
      shortenedUrl.find(ShortenedUrl.linkId.name -> x).map(x => {
        x.delete_!
        x.linkId.value
      })
    )
    (StatusField -> linkId.map(_.map(x => SuccessStatus)).openOr(Full(FailedStatus)).openOr(FailedStatus)) ~
            (ShortenedUrl.linkId.name -> linkId.openOr(Full("")).openOr(""))
  }

  private def edit = {
    val originUrl = param(ShortenedUrl.linkId.name).map(x =>
      shortenedUrl.find(ShortenedUrl.linkId.name -> x).map(x =>
        param(ShortenedUrl.originUrl.name).map(x.originUrl(_).save.originUrl.value)
      )
    )
    (StatusField -> originUrl.map(_.map(_.map(x => SuccessStatus))).openOr(Full(Full(FailedStatus))).
            openOr(Full(FailedStatus)).openOr(FailedStatus)) ~
            (ShortenedUrl.originUrl.name -> originUrl.openOr(Full(Full(""))).openOr(Full("")).openOr(""))
  }

  private def get: JObject = {
    val originUrl = param(ShortenedUrl.linkId.name).map(x => shortenedUrl.find(ShortenedUrl.linkId.name -> x).map(_.originUrl.value))
    (StatusField -> originUrl.map(_.map(x => SuccessStatus)).openOr(Full(FailedStatus)).openOr(FailedStatus)) ~
            (ShortenedUrl.originUrl.name -> originUrl.openOr(Full("")).openOr(""))
  }

  serve {
    case "api" :: "add" :: Nil JsonGet _ => add
    case "api" :: "delete" :: Nil JsonGet _ => delete
    case "api" :: "edit" :: Nil JsonGet _ => edit
    case "api" :: "get" :: Nil JsonGet _ => get
  }
}

}

}
