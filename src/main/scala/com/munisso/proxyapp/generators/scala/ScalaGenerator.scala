package com.munisso.proxyapp.generators.scala

import scala.collection.mutable.ListBuffer

import com.munisso.proxyapp.generators._
import com.munisso.proxyapp.models._
/**
  * Created by rmunisso on 02/05/2016.
  */
class ScalaGenerator extends Generator {
  def generate(mapping: Mapping): List[CodeFile] = {
    val l = new ListBuffer[CodeFile]
    l.toList
  }
}
