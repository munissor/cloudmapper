package com.munisso.proxyapp.generators

import com.munisso.proxyapp.models._

/**
  * Created by rmunisso on 07/06/2016.
  */
class PropertyNames(prefix: String) {

  def variable(): String = prefix + "Data"

  def property(parameter: MappingParameter): String = variable + "." + formatVariable(parameter.logicalName, "")

  def nestedVariable(parameter: MappingParameter, parentParameter: MappingParameter): String = {
    formatVariable(nestedLogicalName(parameter, parentParameter), "")
  }

  def objectVariable(parameter: MappingParameter): String = formatVariable(parameter.logicalName, prefix + "Obj")

  def temporaryVariable(parameter: MappingParameter): String = formatVariable(parameter.logicalName, prefix + "Tmp")

  def iterationVariable(parameter: MappingParameter): String = formatVariable(parameter.logicalName, prefix + "Itr")

  def requestVariable(): String = "req"

  def requestParser(): String = prefix + "Parser"

  def requestWriter(): String = prefix + "Writer"

  def requestHeaders(): String = "rHeaders"

  def requestUrl(): String = "urlString"

  def body(): String = prefix + "Body"

  private def nestedLogicalName(parameter: MappingParameter, parentParameter: MappingParameter): String = {
    parameter.logicalName.replace(parentParameter.logicalName, "").substring(1)
  }

  private def formatVariable(parameter: String, prefix: String): String = prefix + escapeVariable(parameter)

  // make sure the logicalName contains characters allowed from many languages
  private def escapeVariable(parameter: String): String = parameter
}



