package com.munisso.proxyapp.generators

import com.munisso.proxyapp.models._

/**
  * Created by rmunisso on 02/05/2016.
  */
trait Generator {
  def generate(mapping: Mapping): List[CodeFile]

  def getNames(parameter: MappingParameter): List[String] = parameter.name :: (if (parameter.aliases != null) parameter.aliases.toList else Nil)

  def requestVariable(parameter: MappingParameter): String = formatVariable(parameter, "req")

  def responseVariable(parameter: MappingParameter): String = formatVariable(parameter, "resp")

  def requestObjectVariable(parameter: MappingParameter): String = formatVariable(parameter, "reqObj")

  def responseObjectVariable(parameter: MappingParameter): String = formatVariable(parameter, "resObj")

  def temporaryVariable(parameter: MappingParameter): String = formatVariable(parameter, "").toLowerCase()

  private def formatVariable(parameter: MappingParameter, prefix: String): String = prefix + escapeVariable(parameter)

  // make sure the logicalName contains characters allowed from many languages
  private def escapeVariable(parameter: MappingParameter): String = parameter.logicalName
}
