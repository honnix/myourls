package com.honnix.myourls {
package snippet {

import net.liftweb.http._
import S._
import js._
import js.jquery.JqJsCmds.{FadeOut, PrependHtml, Hide, FadeIn}
import JsCmds._
import JE._
import net.liftweb.util._
import SHtml._
import Helpers._
import java.util.Date
import net.liftweb.common.Loggable
import model.ShortenedUrl

import lib.DependencyFactory
import lib.DependencyFactory.ShortenedUrlMetaRecord
import lib.NextIdGenerator
import xml.{Elem, Text, NodeSeq, MetaData, UnprefixedAttribute, Null}

class Admin extends Loggable {
  val currentShortenedUrl = ShortenedUrl.createRecord.date(new Date)

  private implicit def pairToMetaData(int: (String, String)) = new UnprefixedAttribute(int._1, int._2, Null)

  private def ajaxInputButton(text: NodeSeq, jsFunc: Call, func: () => JsCmd, attrs: MetaData*): Elem = {
    def deferCall(data: JsExp, jsFunc: Call): Call =
      Call(jsFunc.function, (jsFunc.params ++ List(AnonFunc(makeAjaxCall(data)))): _*)

    attrs.foldLeft(fmapFunc(contextFuncBuilder(func))(name =>
        <input type="button" value={text}
               onclick={deferCall(Str(name + "=true"), jsFunc).toJsCmd + "; return false;"}/>))(_ % _)
  }

  private def generateRow(shortenedUrl: ShortenedUrl) = {
    def delete(record: ShortenedUrl) = {
      record.delete_!
      new FadeOut(record.id.toString, 0 second, 1 second)
    }

    <tr id={shortenedUrl.id.toString}>
      <td>
        {shortenedUrl.linkId.value}
      </td>
      <td>
        <a href={shortenedUrl.originUrl.value}>
          {shortenedUrl.originUrl.value}
        </a>
      </td>
      <td>
        <a href={shortenedUrl.shortUrl.value}>
          {shortenedUrl.shortUrl.value}
        </a>
      </td>
      <td>
        {shortenedUrl.date.value}
      </td>
      <td>
        {shortenedUrl.ip.value}
      </td>
      <td>
        {shortenedUrl.clickCount.value}
      </td>
      <td class="actions">
        {ajaxInputButton(Text("Edit"), Call("edit"), () => delete(shortenedUrl), "class" -> "button") ++ Text(" ") ++
              ajaxInputButton(Text("Del"), Call("remove"), () => delete(shortenedUrl), "class" -> "button")}
      </td>
    </tr>
  }

  def add = {
    def save: JsCmd = {
      val shortenedUrl = DependencyFactory.inject[ShortenedUrlMetaRecord].open_!

      import net.liftweb.json.JsonDSL._

      val cmd = if (shortenedUrl.find(ShortenedUrl.originUrl.name -> currentShortenedUrl.originUrl.value).isDefined) {
        notice(currentShortenedUrl.originUrl.value + " already exists in database")
        _Noop
      }
      else {
        val linkId = (DependencyFactory.inject[NextIdGenerator].open_! !? 'id).toString
        currentShortenedUrl.linkId(linkId).shortUrl(Props.get("site").open_! + "/" + linkId).
                ip(containerRequest.open_!.remoteAddress).clickCount(0).save
        notice(currentShortenedUrl.originUrl.value + " added to database")
        PrependHtml("tblUrl-body", generateRow(currentShortenedUrl)) & Hide(currentShortenedUrl.id.toString) &
                FadeIn(currentShortenedUrl.id.toString, 0 second, 1 second)
      }

      cmd & Call("end_loading", "#add-button") &
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
      "tr" #> shortenedUrl.findAll.map(generateRow)
    }
  }
}

}

}
