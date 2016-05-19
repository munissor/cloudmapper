package com.munisso.generators

import java.io.{ByteArrayOutputStream, PrintWriter, StringWriter}

import com.munisso.models.{Mapping, MappingParameter}
import com.munisso.util.IndentedPrintWriter

/**
  * Created by rmunisso on 02/05/2016.
  */
trait Generator {
  def generate(mapping: Mapping): List[CodeFile]

  def getNames(parameter: MappingParameter): List[String] = parameter.name :: (if (parameter.aliases != null) parameter.aliases.toList else Nil)

  def requestVariable(parameter: MappingParameter): String = formatVariable(parameter, "req")

  def responseVariable(parameter: MappingParameter): String = formatVariable(parameter, "resp")

  private def formatVariable(parameter: MappingParameter, prefix: String): String = prefix + escapeVariable(parameter)

  // make sure the logicalName contains characters allowed from many languages
  private def escapeVariable(parameter: MappingParameter): String = parameter.logicalName
}
