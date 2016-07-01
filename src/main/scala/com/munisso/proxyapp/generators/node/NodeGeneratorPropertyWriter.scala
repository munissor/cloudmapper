package com.munisso.proxyapp.generators.node

import scala.collection.JavaConverters._

import com.munisso.proxyapp.generators.PropertyNames
import com.munisso.proxyapp.models._
import com.munisso.proxyapp.util.IndentedPrintWriter

/**
  * Created by rmunisso on 12/06/2016.
  */
abstract class NodeGeneratorPropertyWriter(writer: IndentedPrintWriter, propertyNames: PropertyNames) {

  private val queryString = new StringBuilder()

  protected def getContentType(): String

  def writeProperties(remoteUrl: String, parameters: Iterable[MappingParameter]): Unit = {

    // TODO get the content type from the mapping
    if(parameters.find( x=> x.location == Locations.LOCATION_BODY).isDefined ) {
      writer.printLn("var %s = writerFactory.getWriter(%s);", propertyNames.requestWriter, getContentType())
      writer.printLn()
    }

    // TODO: don't hardcode protocol
    if(remoteUrl != null && remoteUrl.nonEmpty) {
      writer.printLn("var urlString = 'https://%s';", remoteUrl)
      writer.printLn("var rHeaders = {};")
    }

    writeProperties(parameters, null, null, null)

    if(queryString.nonEmpty ){
      writer.printLn("urlString = urlString + '?'%s;", queryString.toString)
    }
  }

  private def buildRequestUrl(route: Route): String = {
    val replacements = route.buildRequest.asScala
      .filter( x => x.location == Locations.LOCATION_URL)
      .map( x => String.format(".replace('{%s}', %s)", x.name, this.formatValue(x)))
      .mkString("")

    // TODO: don't hardcode protocol
    String.format("'https://%s'%s", route.remoteUrl, replacements)
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
    val variableName = if(parentVariable != null ) parentVariable + "." + propertyNames.nestedVariable(parameter, parentParameter) else propertyNames.property(parameter)

    //writer.printLn("%s = [];", variableName)

    if( parameter.kind == Types.Object){
      writeMultipleObjectProperties(parameter, parentParameter, parentIterationVariable, parentVariable, variableName)
    }
    else {
      writeMultipleScalarProperties(parameter, parentParameter, parentIterationVariable, parentVariable, variableName)
    }
  }

  private def writeScalarProperty(parameter: MappingParameter, parentParameter: MappingParameter, parentIterationVariable: String, parentVariable: String = null) = {


    parameter.location match {
      case Locations.LOCATION_URL => {
        writer.printLn(String.format("%s.replace('{%s}', %s);", propertyNames.requestUrl(), parameter.name, this.formatValue(parameter)))
      }
      case Locations.LOCATION_QUERY => {
        if(queryString.length > 0){
          queryString.append("+ '&'")
        }
        queryString.append(String.format("+ '%s=' + %s", parameter.name, formatValue(parameter)))
      }
      case Locations.LOCATION_HEADER => {
        writer.printLn(String.format("%s['%s'] = %s;", propertyNames.requestHeaders, parameter.name, formatValue(parameter)))
      }
      case Locations.LOCATION_BODY => {
        val value = if(parentIterationVariable != null) String.format("%s['%s']", parentIterationVariable, normalizeParameter(parameter.logicalName)) else ""
        val parent = if(parentVariable != null) ", " + parentVariable else ""
        writer.printLn(String.format("%s.writeValue('%s',%s%s);", propertyNames.requestWriter, normalizeParameter(parameter.name), value, parent))
      }
    }
  }

  private def normalizeParameter(name: String): String = if(name.startsWith(".")) name.substring(1) else name

  private def writeObjectProperty(parameter: MappingParameter, parentParameter: MappingParameter, parentIterationVariable: String, parentVariable: String): Unit = {
    val parent = if(parentIterationVariable != null) ", " + parentIterationVariable else ""
    writer.printLn("%s.writeObject(%s%s);", propertyNames.requestWriter, parent)
  }

  private def writeMultipleObjectProperties(parameter: MappingParameter, parentParameter: MappingParameter, parentIterationVariable: String, parentVariable: String, arrayVariable: String): Unit = {
    val variableName = if(parentVariable != null ) parentVariable + "." + propertyNames.nestedVariable(parameter, parentParameter) else propertyNames.property(parameter)
    val parent = if(parentVariable != null) ", " + parentVariable else ""

    writer.printLn("%s.forEach(function(%s){", variableName, propertyNames.iterationVariable(parameter))
    writer.increaseIndent()
    val tmp = propertyNames.temporaryVariable(parameter)
    writer.printLn("%s = %s.writeObject('%s%s');", tmp, propertyNames.requestWriter, parameter.name, parent)
    writeProperties(parameter.properties, parameter, propertyNames.iterationVariable(parameter), tmp)
    writer.decreaseIndent()
    writer.printLn("});")
  }

  private def writeMultipleScalarProperties(parameter: MappingParameter, parentParameter: MappingParameter, parentIterationVariable: String, parentVariable: String, arrayVariable: String): Unit = {
  }


  private def formatValue(parameter: MappingParameter): String = {
    if(parameter.format != null && parameter.format.length() > 0 ){
      return String.format("formatUtils.format%s(%s, '%s')", parameter.kind, propertyNames.property(parameter), parameter.format)
    }

    propertyNames.property(parameter)
  }

}

class NodeGeneratorRequestPropertyWriter(writer: IndentedPrintWriter)
  extends NodeGeneratorPropertyWriter(writer, new PropertyNames("srcReq")) {
  override protected def getContentType(): String = "req.header('content-type')"
}

class NodeGeneratorRestifyPropertyWriter(writer: IndentedPrintWriter)
  extends NodeGeneratorPropertyWriter(writer, new PropertyNames("dstRes")){
  override protected def getContentType(): String = "response.headers['content-type']"
}

