package com.honnix.myourls {
package snippet {

import net.liftweb.http._
import net.liftweb.util._
import SHtml._
import Helpers._
import model.ShortenedUrl
import java.util.Date
import xml.NodeSeq

class Admin {
  object currentShortenedUrl extends RequestVar[ShortenedUrl](
    ShortenedUrl.createRecord.date(new Date)
  )

  def add = ".origin-url" #> text("http://", currentShortenedUrl.originUrl(_), "id" -> "add-url", "name" -> "url",
    "class" -> "text", "size" -> "90") &
          ".origin-url" #> text("http://", currentShortenedUrl.originUrl(_), "id" -> "add-url", "name" -> "url",
            "class" -> "text", "size" -> "90") andThen "#new-url-form" #> ((ns: NodeSeq) => ajaxForm(ns))


  def fileter = null
}

}

}
