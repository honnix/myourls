package com.honnix.myourls.snippet

import scala.xml.{NodeSeq, Text}
import net.liftweb.util._
import net.liftweb.common._
import java.util.Date
import com.honnix.myourls.lib._
import Helpers._

class HelloWorld {
  lazy val date: Box[Date] = DependencyFactory.inject[Date]
  // inject the date

  // bind the date into the element with id "time"
  def howdy = "#time *" #> date.map(_.toString)
}
