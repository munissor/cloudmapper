package com.munisso.proxyapp.generators.node

import com.munisso.proxyapp.util.IndentedPrintWriter
import com.munisso.proxyapp.generators._
import com.munisso.proxyapp.models._

/**
  * Created by rmunisso on 07/06/2016.
  */
abstract class NodeGeneratorPropertyReader(writer: IndentedPrintWriter, protected val propertyNames: PropertyNames) {

  def extractProperties(parameters: Iterable[MappingParameter]): Unit = {
    writer.printLn("var %s = {};", propertyNames.variable() )

    val bodyArg = parameters.find( x=> x.location == Locations.LOCATION_BODY)
    bodyArg match {
      case Some(i) => {
        val contentType = i.kind match {
          case Types.Binary => "'_raw'"
          case _ => getContentType()
        }
        writer.printLn("var %s = parserFactory.getParser(%s, %s);", propertyNames.requestParser, contentType, getBody)
       }
      case None =>
    }

    writer.printLn()
    extractProperties(parameters, null, null, null)
  }

  protected def getContentType(): String

  protected def getBody(): String


  private def extractProperties(parameters: Iterable[MappingParameter], parentParameter: MappingParameter, parentIterationVariable: String, parentVariable: String): Unit = {
    parameters.foreach( x => {

      if( x.multiple ){
        extractMultipleProperties(x, parentParameter, parentIterationVariable, parentVariable)
      }
      else if(x.kind == Types.Object) {
        extractObjectProperty(x, parentParameter, parentIterationVariable, parentVariable)
      }
      else {
        extractScalarProperty(x, parentParameter, parentIterationVariable, parentVariable)
      }
    })
  }

  private def extractMultipleProperties(parameter: MappingParameter, parentParameter: MappingParameter, parentIterationVariable: String, parentVariable: String): Unit = {
    val variableName = if(parentVariable != null ) parentVariable + "." + propertyNames.nestedVariable(parameter, parentParameter) else propertyNames.property(parameter)

    writer.printLn("%s = [];", variableName)

    if( parameter.kind == Types.Object){
      extractMultipleObjectProperties(parameter, parentParameter, parentIterationVariable, parentVariable, variableName)
    }
    else {
      extractMultipleScalarProperties(parameter, parentParameter, parentIterationVariable, parentVariable, variableName)
    }
  }

  protected def getHeaderFormat(): String

  private def extractScalarProperty(parameter: MappingParameter, parentParameter: MappingParameter, parentIterationVariable: String, parentVariable: String = null) = {

    val variableName = if(parentVariable != null ) parentVariable + "." + propertyNames.nestedVariable(parameter, parentParameter) else propertyNames.property(parameter)
    val parent = if(parentIterationVariable != null) ", " + parentIterationVariable else ""

    val read = parameter.location match {
      case Locations.LOCATION_URL => formatExtractParameter(propertyNames.requestVariable + ".params.%s", parameter)
      case Locations.LOCATION_QUERY => formatExtractParameter(propertyNames.requestVariable + ".query.%s", parameter)
      case Locations.LOCATION_HEADER => formatExtractParameter(getHeaderFormat, parameter, n => n.toLowerCase)
      case Locations.LOCATION_BODY => formatExtractParameter(propertyNames.requestParser + ".getValue('%s'" + parent + ")", parameter, n => if (n.startsWith(".")) n.substring(1) else n)
      case _ =>
        if (parameter.fallback == FallbackOption.Config)
          String.format("config.%s", parameter.logicalName)
        else
          String.format("'%s'", parameter.value)
      }

    writer.printLn("%s = %s;", variableName, read)
  }

  private def extractObjectProperty(parameter: MappingParameter, parentParameter: MappingParameter, parentIterationVariable: String, parentVariable: String): Unit = {
    val respObjVar = propertyNames.objectVariable(parameter)
  }

  private def extractMultipleObjectProperties(parameter: MappingParameter, parentParameter: MappingParameter, parentIterationVariable: String, parentVariable: String, arrayVariable: String): Unit = {

    // READ MULTIPLE FROM SOURCE (url, query, header, body)
    val oVariable = propertyNames.objectVariable(parameter)
    val iVariable = propertyNames.iterationVariable(parameter)
    var parName = parameter.name
    var parent = ""
    if(parName.startsWith(".") && parentIterationVariable != null ){
      parName = parName.substring(1)
      parent = String.format(", %s", parentIterationVariable)
    }

    writer.printLn("var %s = %s.getObjects('%s'%s);", oVariable, propertyNames.requestParser(), parName, parent)

    writer.printLn("%s.forEach(function(%s){", oVariable, iVariable)
    writer.increaseIndent()

    val tVariable = propertyNames.temporaryVariable(parameter)
    writer.printLn("var %s = {};", tVariable)

    // EXTRACT nested properties into temp property
    extractProperties(parameter.properties.toIterable, parameter, iVariable, tVariable)

    writer.printLn("%s.push(%s);", arrayVariable, tVariable)

    writer.decreaseIndent()
    writer.printLn("});")
  }

  private def extractMultipleScalarProperties(parameter: MappingParameter, parentParameter: MappingParameter, parentIterationVariable: String, parentVariable: String, arrayVariable: String): Unit = {

    // READ MULTIPLE FROM SOURCE (url, query, header, body)
    val oVariable = propertyNames.objectVariable(parameter)
    val iVariable = propertyNames.iterationVariable(parameter)
    var parName = parameter.name
    var parent = ""
    if(parName.startsWith(".") && parentIterationVariable != null ){
      parName = parName.substring(1)
      parent = String.format(", %s", parentIterationVariable)
    }

    writer.printLn("var %s = %s.getValues('%s'%s);", oVariable, propertyNames.requestParser(), parName, parent)

    writer.printLn("%s.forEach(function(%s){", oVariable, iVariable)
    writer.increaseIndent()

    writer.printLn("%s.push(%s);", arrayVariable, iVariable)

    writer.decreaseIndent()
    writer.printLn("}")
  }

  private def getNames(parameter: MappingParameter): List[String] = parameter.name :: (if (parameter.aliases != null) parameter.aliases.toList else Nil)

  private def formatExtractParameter(format: String, parameter: MappingParameter, nameTransform: (String) => String = (x) => x): String = {
    getNames(parameter).map(n => String.format(format, nameTransform(n))).mkString(" || ")
  }

}

class NodeGeneratorRestifyPropertyReader(writer: IndentedPrintWriter)
  extends NodeGeneratorPropertyReader(writer, new PropertyNames("srcReq")) {

  override protected def getContentType(): String = propertyNames.requestVariable + ".headers['content-type']"

  override protected def getHeaderFormat(): String = propertyNames.requestVariable + ".header('%s')"

  override protected def getBody(): String = "req.body"
}

class NodeGeneratorRequestPropertyReader(writer: IndentedPrintWriter)
  extends NodeGeneratorPropertyReader(writer, new PropertyNames("dstRes")) {

  override protected def getContentType(): String = "response.headers['content-type']"

  override protected def getHeaderFormat(): String = "response.headers['%s']"

  override protected def getBody(): String = "body"
}