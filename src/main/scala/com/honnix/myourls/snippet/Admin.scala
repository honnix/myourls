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
import net.liftweb.json.JsonDSL._
import java.util.Date
import net.liftweb.common.Loggable
import net.liftweb.mongodb.MongoDB
import model.ShortenedUrl

import lib.DependencyFactory
import lib.DependencyFactory.ShortenedUrlMetaRecord
import lib.NextIdGenerator
import xml.{Elem, Text, NodeSeq, MetaData, UnprefixedAttribute, Null}
import net.liftweb.json.JsonAST.JObject
import net.liftweb.mongodb.{Limit, Skip, FindOption}

class Admin extends Loggable {
  val shortenedUrl = DependencyFactory.inject[ShortenedUrlMetaRecord].open_!

  object currentShortenedUrlVar extends RequestVar[ShortenedUrl](
    shortenedUrl.createRecord.date(new Date)
  )

  object page extends RequestVar[Int](S.param("page").openOr("1").toInt)

  object perpage extends RequestVar[Int](S.param("perpage").openOr("10").toInt)

  object offset extends RequestVar[Int]((page - 1) * perpage)

  object search extends RequestVar[String](S.param("search").openOr(""))

  object searchIn extends RequestVar[String](S.param("search-in").openOr(ShortenedUrl.originUrl.name))

  object sortBy extends RequestVar[String](S.param("sort-by").openOr(ShortenedUrl.linkId.name))

  object sortOrder extends RequestVar[Int](S.param("sort-order").openOr("-1").toInt)

  object clickFilter extends RequestVar[String](S.param("click-filter").openOr("gte"))

  object clickLimit extends RequestVar[String](S.param("click-limit").openOr(""))

  object clickObject extends RequestVar[JObject](
    if (clickLimit.isEmpty) JObject(Nil)
    else (ShortenedUrl.clickCount.name -> (("$" + clickFilter) -> clickLimit.is.toInt))
  )

  object searchObject extends RequestVar[JObject](
    if (search.isEmpty) JObject(Nil)
    else ("$where" -> ("this." + searchIn + ".indexOf('" + search + "') != -1"))
  )

  object findOptions extends RequestVar[List[FindOption]](
    List(Skip(offset), Limit(perpage))
  )

  object totalItems extends RequestVar[Long](
    shortenedUrl.count(clickObject.is ~ searchObject.is)
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
                      "#originUrl *" #> <a href={shortenedUrl.originUrl.value}>{shortenedUrl.originUrl.value}</a> &
                      "#originUrl [id]" #> ("url-" + shortenedUrl.linkId.value) &
                      "#shortUrl *" #> <a href={shortenedUrl.shortUrl.value}>{shortenedUrl.shortUrl.value}</a> &
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

  def info = {
    def countClicks = {
      MongoDB.useCollection(shortenedUrl.collectionName) {
        case x if x.count == 0 => "0"
        case x =>
          val map = """function() { emit("totalClickCount", this.%s); }""" format ShortenedUrl.clickCount.name
          val reduce = """function(key, values) { return Array.sum(values); }"""
          val results = x.mapReduce(map, reduce, null, null).results

          /**
           * all numbers returned by mongodb is Double since this is how number defined by javascript
           */
          if (results.hasNext) results.next.get("value").asInstanceOf[Number].intValue.toString else "0"
      }
    }

    ".from" #> (if (offset.is + 1 > totalItems) totalItems else offset.is + 1).toString &
            ".to" #> (if (offset.is + perpage > totalItems) totalItems else offset.is + perpage).toString &
            ".total" #> totalItems.toString &
            ".links" #> shortenedUrl.count.toString &
            ".clicks" #> countClicks
  }

  def filter = {
    def select(default: Any, actual: Any) = {
      ("value=" + default + " [selected]") #> (None: Option[String]) andThen
              ("value=" + actual) #> ((x: NodeSeq) => x.asInstanceOf[Elem] % ("selected" -> "selected"))
    }

    "name=search [value]" #> search.is &
            "name=search-in" #> select(ShortenedUrl.originUrl.name, searchIn) &
            "name=sort-by" #> select(ShortenedUrl.linkId.name, sortBy) &
            "name=sort-order" #> select(-1, sortOrder) &
            "name=perpage [value]" #> perpage.is.toString &
            "name=click-filter" #> select("gte", clickFilter) &
            "name=click-limit [value]" #> clickLimit.is
  }

  def paging = {
    val totalPages = java.lang.Math.ceil(totalItems.is.toDouble / perpage.is).toInt

    def generateNav: NodeSeq = {
      def generateHref(page: Int) = "/?search=" + search + "&sort-by=" + sortBy + "&sort-order=" + sortOrder +
              "&search-in=" + searchIn + "&click-filter=" + clickFilter + "&click-limit=" + clickLimit +
              "&perpage=" + perpage + "&page=" + page

      val left = if (page.is != 1) <a href={generateHref(page - 1)} title={"« Go to Page %d" format (page - 1)}>«</a>
      else Nil

      val pages = (1 to totalPages).toList.map {
        x =>
          if (x == page.is)
            <strong>{"[" + x + "]"}</strong>
          else
            <a href={generateHref(x)} title={"Page " + x}>{x}</a>
      }

      val right = if (page.is != totalPages) <a href={generateHref(page.is + 1)} title={"Go to Page %d »" format (page.is + 1)}>»</a>
      else Nil

      left ++ pages ++ right
    }

    "#total-pages" #> totalPages.toString &
            "#nav" #> generateNav
  }

  def list = if (totalItems.is == 0) {
    ".nourl-found ^^" #> "true"
  } else {
    ".nourl-found" #> shortenedUrl.findAll(clickObject.is ~ searchObject.is,
      (sortBy.is -> sortOrder.is), findOptions: _*).map(generateRow)
  }
}

}

}
