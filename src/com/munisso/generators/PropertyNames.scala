package com.munisso.generators

import com.munisso.models.MappingParameter

/**
  * Created by rmunisso on 07/06/2016.
  */
abstract class PropertyNames(prefix: String) {

  def variable(parameter: MappingParameter): String = formatVariable(parameter.logicalName, prefix)

  def nestedVariable(parameter: MappingParameter, parentParameter: MappingParameter): String = {
    formatVariable(nestedLogicalName(parameter, parentParameter), "")
  }

  def objectVariable(parameter: MappingParameter): String = formatVariable(parameter.logicalName, prefix + "Obj")

  def temporaryVariable(parameter: MappingParameter): String = formatVariable(parameter.logicalName, prefix + "Tmp")

  def iterationVariable(parameter: MappingParameter): String = formatVariable(parameter.logicalName, prefix + "Itr")

  def requestVariable(): String = prefix

  def requestParser(): String = prefix + "Parser"

  private def nestedLogicalName(parameter: MappingParameter, parentParameter: MappingParameter): String = {
    parameter.logicalName.replace(parentParameter.logicalName, "").substring(1)
  }

  private def formatVariable(parameter: String, prefix: String): String = prefix + escapeVariable(parameter)

  // make sure the logicalName contains characters allowed from many languages
  private def escapeVariable(parameter: String): String = parameter
}

class RequestPropertyNames extends PropertyNames("req")

class ResponsePropertyNames extends PropertyNames("res")


