package com.honnix.myourls {
package snippet {

import net.liftweb.http._
import S._
import js._
import JsCmds._
import JE._
import net.liftweb.util._
import SHtml._
import Helpers._
import java.util.Date
import xml.NodeSeq
import net.liftweb.common.Loggable
import model.{NextId, ShortenedUrl}

import lib.DependencyFactory
import lib.DependencyFactory.ShortenedUrlMetaRecord
import lib.NextIdGenerator

class Admin extends Loggable {
  val currentShortenedUrl = ShortenedUrl.createRecord.date(new Date)

  def add = {
    def save: JsCmd = {
      val shortenedUrl = DependencyFactory.inject[ShortenedUrlMetaRecord].open_!

      import net.liftweb.json.JsonDSL._

      if (shortenedUrl.find(ShortenedUrl.originUrl.name -> currentShortenedUrl.originUrl.value).isDefined)
        notice(currentShortenedUrl.originUrl.value + " already exists in database")
      else {
        val linkId = (DependencyFactory.inject[NextIdGenerator].open_! !? 'id).toString
        currentShortenedUrl.linkId(linkId).shortUrl(Props.get("site").open_! + "/" + linkId).
                ip(containerRequest.open_!.remoteAddress).clickCount(0).save
        notice(currentShortenedUrl.originUrl.value + " added to database")
      }

      Call("end_loading", "#add-button") &
              Call("end_disable", "#add-button")
    }

    "#add-url" #> text("http://", currentShortenedUrl.originUrl(_)) &
            "#add-button" #> ajaxSubmit("Shorten The URL", save _) andThen {
      "form" #> ((ns: NodeSeq) => ajaxForm(JsIf(JsEq(Call("validate"), JsFalse), JsReturn(false)), ns))
    }
  }

  def filter = null

  def info = {
    val shortenedUrl = DependencyFactory.inject[ShortenedUrlMetaRecord].open_!

    ".from" #> 0.toString & ".to" #> 0.toString & ".total" #> shortenedUrl.count.toString &
            ".links" #> shortenedUrl.count.toString & ".clicks" #> 0.toString
  }

  def list = {
    val shortenedUrl = DependencyFactory.inject[ShortenedUrlMetaRecord].open_!

    if (shortenedUrl.count == 0) {
      "tr [class]" #> "nourl_found"
    } else {
      "tr" #> shortenedUrl.findAll.map(x => {
        <tr>
          <td>{x.linkId.value}</td>
          <td>
            <a href={x.originUrl.value}>{x.originUrl.value}</a>
          </td>
          <td>
            <a href={x.shortUrl.value}>{x.shortUrl.value}</a>
          </td>
          <td>{x.date.value}</td>
          <td>{x.ip.value}</td>
          <td>{x.clickCount.value}</td>
          <td class="actions">
              <input type="button" value="Edit" class="button"/>
              <input type="button" value="Del" class="button"/>
          </td>
        </tr>
      })
    }
  }
}

}

}
