package com.honnix.myourls {
package snippet {

import net.liftweb.http._
import S._
import js._
import js.jquery.JqJsCmds.{FadeOut, PrependHtml, Hide, FadeIn}
import js.jquery.JqJE._
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
  object currentShortenedUrlVar extends RequestVar[ShortenedUrl](
    ShortenedUrl.createRecord.date(new Date)
  )

  private val IdPrefix = "id-"

  private val EditPrefix = "edit-"

  private val DeletePrefix = "delete-"

  private var currentShortenedUrl: ShortenedUrl = _

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
      FadeOut(record.id.toString, 0 second, 1 second)
    }

    def edit(record: ShortenedUrl) = {
      def update(originalUrl: String) = {
        record.originUrl(originalUrl).save
        Call("postsave", record.linkId.value)
      }

      val (name, js) = ajaxCall(Call("presave", record.linkId.value), update)
      val urlId = EditPrefix + "url-" + shortenedUrl.linkId.value

      val tr = <tr id={EditPrefix + shortenedUrl.linkId.value} class="edit-row">
        <td colspan="6">
          Edit:
          <strong>original URL</strong>
          :
          <input type="text" id={urlId} name={urlId} value={shortenedUrl.originUrl.value}
                 class="text" size="100"></input>
        </td>
        <td colspan="1">
          <input type="button" id={EditPrefix + "submit-" + shortenedUrl.linkId.value} title="Save new value"
                 value="Save" class="button" onclick={js.toJsCmd}></input>
          <input type="button" id={EditPrefix + "close-" + shortenedUrl.linkId.value} title="Cancel editing" value="X" class="button"
                 onclick={"hide_edit('" + shortenedUrl.linkId.value + "')"}></input>
        </td>
      </tr>

      JsCrVar("func", Jx(tr).toJs) & Jq(Call("func", "document") ~> JsVal("firstChild")) ~>
              JsFunc("insertAfter", "#" + IdPrefix + shortenedUrl.linkId.value) & Focus(urlId)
    }

    <tr id={IdPrefix + shortenedUrl.linkId.value}>
      <td>
        {shortenedUrl.linkId.value}
      </td>
      <td id={"url-" + shortenedUrl.linkId.value}>
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
        {ajaxInputButton(Text("Edit"), Call("edit", shortenedUrl.linkId.value), () => edit(shortenedUrl),
        "class" -> "button", "id" -> (EditPrefix + "button-" + shortenedUrl.linkId.value)) ++ Text(" ") ++
              ajaxInputButton(Text("Del"), Call("remove"), () => delete(shortenedUrl),
                "class" -> "button", "id" -> (DeletePrefix + "button-" + shortenedUrl.linkId.value))}
      </td>
    </tr>
  }

  def add = {
    def save = {
      val shortenedUrl = DependencyFactory.inject[ShortenedUrlMetaRecord].open_!

      import net.liftweb.json.JsonDSL._

      if (shortenedUrl.find(ShortenedUrl.originUrl.name -> currentShortenedUrl.originUrl.value).isDefined) {
        notice(currentShortenedUrl.originUrl.value + " already exists in database")
        Call("restore_add_button").cmd
      }
      else {
        val linkId = (DependencyFactory.inject[NextIdGenerator].open_! !? 'id).toString
        currentShortenedUrl.linkId(linkId).shortUrl(Props.get("site").open_! + "/" + linkId).
                ip(containerRequest.open_!.remoteAddress).clickCount(0).save
        notice(currentShortenedUrl.originUrl.value + " added to database")
        PrependHtml("tblUrl-body", generateRow(currentShortenedUrl)) & Hide(currentShortenedUrl.id.toString) &
                FadeIn(currentShortenedUrl.id.toString, 0 second, 1 second) & Call("postadd")
      }
    }

    "#add-url" #> text("http://", currentShortenedUrlVar.originUrl(_)) &
            "#current" #> hidden(() => currentShortenedUrl = currentShortenedUrlVar.is) &
            "#add-button" #> ajaxSubmit("Shorten The URL", save _) andThen {
      "form" #> ((ns: NodeSeq) => ajaxForm(JsIf(JsEq(Call("preadd"), JsFalse), JsReturn(false)), ns))
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
