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

  private val RealContentId = "#real-content"

  private val TemplatesHidden = "templates-hidden"

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
      FadeOut(IdPrefix + record.linkId.value.toString, 0 second, 1 second)
    }

    def edit(record: ShortenedUrl) = {
      def update(originalUrl: String) = {
        record.originUrl(originalUrl).save
        Call("postsave", record.linkId.value)
      }

      val (name, js) = ajaxCall(Call("presave", record.linkId.value), update)
      val urlId = EditPrefix + "url-" + shortenedUrl.linkId.value

      val tr = TemplateFinder.findAnyTemplate(List(TemplatesHidden, "edit")) map {
        (RealContentId + " ^^") #> "true" andThen
                (RealContentId + " [id]") #> (EditPrefix + shortenedUrl.linkId.value) &
                        "#edit-url [name]" #> urlId &
                        "#edit-url [value]" #> shortenedUrl.originUrl.value &
                        "#edit-url [id]" #> urlId &
                        "#save-button [onclick]" #> js.toJsCmd &
                        "#save-button [id]" #> (EditPrefix + "submit-" + shortenedUrl.linkId.value) &
                        "#cancel-button [onclick]" #> ("hide_edit('" + shortenedUrl.linkId.value + "')") &
                        "#cancel-button [id]" #> (EditPrefix + "close-" + shortenedUrl.linkId.value)
      } open_!

      JsCrVar("func", Jx(tr).toJs) & Jq(Call("func", "document") ~> JsVal("firstChild")) ~>
              JsFunc("insertAfter", "#" + IdPrefix + shortenedUrl.linkId.value) & Focus(urlId)
    }

    TemplateFinder.findAnyTemplate(List(TemplatesHidden, "row")) map {
      (RealContentId + " ^^") #> "true" andThen
              (RealContentId + " [id]") #> (IdPrefix + shortenedUrl.linkId.value) &
                      "#id *" #> shortenedUrl.linkId.value &
                      "#id [id]" #> (None: Option[String]) &
                      "#originUrl *" #> <a href={shortenedUrl.originUrl.value}>
                        {shortenedUrl.originUrl.value}
                      </a> &
                      "#originUrl [id]" #> ("url-" + shortenedUrl.linkId.value) &
                      "#shortUrl *" #> <a href={shortenedUrl.shortUrl.value}>
                        {shortenedUrl.shortUrl.value}
                      </a> &
                      "#date *" #> shortenedUrl.date.value.toString &
                      "#ip *" #> shortenedUrl.ip.value &
                      "#clickCount *" #> shortenedUrl.clickCount.value.toString &
                      "#edit-button" #> ajaxInputButton(Text("Edit"), Call("edit", shortenedUrl.linkId.value), () => edit(shortenedUrl),
                        "id" -> ("edit-button-" + shortenedUrl.linkId.value)) &
                      "#delete-button" #> ajaxInputButton(Text("Del"), Call("remove"), () => delete(shortenedUrl),
                        "id" -> ("delete-button-" + shortenedUrl.linkId.value))
    } open_!
  }

  def add = {
    def save = {
      val shortenedUrl = DependencyFactory.inject[ShortenedUrlMetaRecord].open_!

      import net.liftweb.json.JsonDSL._

      if (shortenedUrl.find(ShortenedUrl.originUrl.name -> currentShortenedUrl.originUrl.value).isDefined) {
        warning(currentShortenedUrl.originUrl.value + " already exists in database")
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

  def filter = {
    "#sort-search" #> text("...", println)
  }


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
