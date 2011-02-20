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
import lib.DependencyFactory.{NextIdMetaRecord, ShortenedUrlMetaRecord}

class Admin extends Loggable {
  val currentShortenedUrl = ShortenedUrl.createRecord.date(new Date)

  def add = {
    def save: JsCmd = {
      def generateId = {
        val nextId = DependencyFactory.inject[NextIdMetaRecord].open_!
        val id = nextId.findAll
        if (id.isEmpty) {
          nextId.createRecord.next("2").save
          "1"
        } else {
          val record = id.head
          val newId = record.next.value
          record.next((Integer.parseInt(newId, 16) + 1).toHexString.toUpperCase).save
          newId
        }
      }

      val shortenedUrl = DependencyFactory.inject[ShortenedUrlMetaRecord].open_!

      import net.liftweb.json.JsonDSL._

      if (shortenedUrl.find(ShortenedUrl.originUrl.name -> currentShortenedUrl.originUrl.value).isDefined)
        notice(currentShortenedUrl.originUrl.value + " already exists in database")
      else {
        val linkId = generateId
        currentShortenedUrl.linkId(linkId).shortUrl("http://localhost/" + linkId).clickCount(0).save
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

  def list = {
    val shortenedUrl = DependencyFactory.inject[ShortenedUrlMetaRecord].open_!

    if (shortenedUrl.count == 0) {
      "tr [class]" #> "nourl_found"
    } else {
      "tr" #> shortenedUrl.findAll.map(x => {
        <tr>
          <td>{x.linkId.value}</td>
          <td>{x.originUrl.value}</td>
          <td>{x.shortUrl.value}</td>
          <td>{x.date.value}</td>
          <td>{x.ip.value}</td>
          <td>{x.clickCount.value}</td>
          <td>
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
