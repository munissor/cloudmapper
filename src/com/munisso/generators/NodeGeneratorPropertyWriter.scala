package com.munisso.generators

import com.munisso.models.{Locations, MappingParameter, Types}
import com.munisso.util.IndentedPrintWriter

/**
  * Created by rmunisso on 12/06/2016.
  */
abstract class NodeGeneratorPropertyWriter(writer: IndentedPrintWriter, propertyNames: PropertyNames) {
  def writeProperties(parameters: Iterable[MappingParameter]): Unit = {
    writeProperties(parameters, null, null, null)
  }

  private def writeProperties(parameters: Iterable[MappingParameter], parentParameter: MappingParameter, parentIterationVariable: String, parentVariable: String): Unit = {
    parameters.foreach( x => {

      if( x.multiple ){
        writeMultipleProperties(x, parentParameter, parentIterationVariable, parentVariable)
      }
      else if(x.kind == Types.Object) {
        writeObjectProperty(x, parentParameter, parentIterationVariable, parentVariable)
      }
      else {
        writeScalarProperty(x, parentParameter, parentIterationVariable, parentVariable)
      }
    })
  }

  private def writeMultipleProperties(parameter: MappingParameter, parentParameter: MappingParameter, parentIterationVariable: String, parentVariable: String): Unit = {
    val declaration = if (parentVariable != null) "" else "var "
    val variableName = if(parentVariable != null ) parentVariable + "." + propertyNames.nestedVariable(parameter, parentParameter) else propertyNames.variable(parameter)

    writer.printLn("%s%s = [];", declaration, variableName)

    if( parameter.kind == Types.Object){
      writeMultipleObjectProperties(parameter, parentParameter, parentIterationVariable, parentVariable, variableName)
    }
    else {
      writeMultipleScalarProperties(parameter, parentParameter, parentIterationVariable, parentVariable, variableName)
    }
  }

  private def writeScalarProperty(parameter: MappingParameter, parentParameter: MappingParameter, parentIterationVariable: String, parentVariable: String = null) = {
  }

  private def writeObjectProperty(parameter: MappingParameter, parentParameter: MappingParameter, parentIterationVariable: String, parentVariable: String): Unit = {
  }

  private def writeMultipleObjectProperties(parameter: MappingParameter, parentParameter: MappingParameter, parentIterationVariable: String, parentVariable: String, arrayVariable: String): Unit = {
  }

  private def writeMultipleScalarProperties(parameter: MappingParameter, parentParameter: MappingParameter, parentIterationVariable: String, parentVariable: String, arrayVariable: String): Unit = {
  }

}

class NodeGeneratorRequestPropertyWriter(writer: IndentedPrintWriter)
  extends NodeGeneratorPropertyWriter(writer, new RequestPropertyNames) {
}

class NodeGeneratorRestifyPropertyWriter(writer: IndentedPrintWriter)
  extends NodeGeneratorPropertyWriter(writer, new ResponsePropertyNames){
}

