package com.munisso.generators

import com.munisso.models.Mapping

import scala.collection.mutable.ListBuffer

/**
  * Created by rmunisso on 02/05/2016.
  */
class ScalaGenerator extends Generator {
  def generate(mapping: Mapping): List[CodeFile] = {
    val l = new ListBuffer[CodeFile]
    l.toList
  }
}
