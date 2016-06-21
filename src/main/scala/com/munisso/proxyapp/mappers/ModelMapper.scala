package com.munisso.proxyapp.mappers

import java.io.File

import com.fasterxml.jackson.databind.{ObjectMapper, SerializationFeature}

import scala.collection.mutable._
import scala.collection.JavaConversions._
import com.munisso.proxyapp.models._


/**
  * Created by rmunisso on 26/04/2016.
  */
class ModelMapper {

  def mapModels(sourcePath: String, destinationPath: String): Mapping = {
    val sourceFile = new File(sourcePath)
    val destinationFile = new File(destinationPath)

    val sourceModel = readModelFromFile(sourceFile)
    val destinationModel = readModelFromFile(destinationFile)

     mapModels(sourceModel, destinationModel)
  }

  def mapModels(source: Model, destination: Model): Mapping = {

    val mapping = new Mapping
    mapping.configurations = destination.configurations
    mapping.signature = destination.signature

    source.operations.foreach(srcOperation => {

      val route = new Route(srcOperation.name, srcOperation.request.url, srcOperation.request.verb)
      mapping.routes.add(route)

      // Find the matching operation in the destination model
      val dop = destination.operations.find(_.name == srcOperation.name)
      if (dop.isEmpty) {
        route.routeError = new MappingError("Operation missing from destination provider")
      }
      else {
        val destOperation = dop.get

        route.remoteUrl = destOperation.request.url
        route.remoteVerb = destOperation.request.verb

        val sourceReqArgs =
          extractArguments("url", srcOperation.request.urlReplacements) ++
            extractArguments("header", srcOperation.request.headers) ++
            extractArguments("query", srcOperation.request.queryString) ++
            extractBodyArguments(srcOperation.request.body)

        val sourceReqMap = sourceReqArgs.map(x => x.logicalName -> x).toMap

        val destReqArgs =
          extractArguments("url", destOperation.request.urlReplacements) ++
            extractArguments("header", destOperation.request.headers) ++
            extractArguments("query", destOperation.request.queryString) ++
            extractBodyArguments(destOperation.request.body)

        destReqArgs.foreach(arg => {
          val p = sourceReqMap.get(arg.logicalName)

          // PARSE SOURCE REQUEST
          if (p.isDefined) {
            route.parseRequest.add(p.get)
          }

          // BUILD DESTINATION REQUEST
          if (p.isEmpty) {

            if (arg.value != null) {
              val dup = MappingParameter.duplicate(arg)
              dup.location = null
              route.parseRequest.add(dup)

              route.buildRequest.add(arg)
            }
            else if (!arg.optional) {
              route.requestErrors.add(new MappingError(String.format("Missing mandatory parameter %s from request", arg.logicalName)))
            }
          }
          else {
            route.buildRequest.add(arg)
          }
        })

        // REQUEST IS SENT HERE!!

        // PARSE DESTINATION RESPONSE

        val destResArgs = extractArguments("header", destOperation.response.headers) ++
          extractBodyArguments(destOperation.response.body)

        val destResMap = destResArgs.map(x => x.logicalName -> x).toMap

        val srcResArgs = extractArguments("header", srcOperation.response.headers) ++
          extractBodyArguments(srcOperation.response.body)

        srcResArgs.foreach(arg => {
          val p = destResMap.get(arg.logicalName)

          // PARSE DESTINATION RESPONSE
          if (p.isDefined) {
            route.parseResponse.add(p.get)
          }

          // BUILD DESTINATION RESPONSE
          if (p.isEmpty) {

            if (arg.value != null) {
              route.buildResponse.add(arg)
            }
            else if (!arg.optional) {
              route.responseErrors.add(new MappingError(String.format("Missing mandatory parameter %s from response", arg.logicalName)))
            }
          }
          else {
            route.buildResponse.add(arg)
          }

        })
      }

      route.parseRequest = aggregateParameters(route.parseRequest.toList)
      route.buildRequest = aggregateParameters(route.buildRequest.toList)
      route.parseResponse = aggregateParameters(route.parseResponse.toList)
      route.buildResponse = aggregateParameters(route.buildResponse.toList)
    })

    return mapping
  }

  private def readModelFromFile(file: File): Model = {
    val mapper: ObjectMapper = new ObjectMapper
    //mapper.configure(SerializationFeature.INDENT_OUTPUT, true);

    mapper.readValue(file, classOf[Model])
  }

  private def extractArguments(location: String, parameters: Array[Parameter]): ListBuffer[MappingParameter] = {
    val res = new ListBuffer[MappingParameter]

    if (parameters != null) {

      parameters.foreach( h => {

        val m = MappingParameter.fromParameter(h)
        m.location = location

        res += m

      })
    }

    return res
  }

  private def extractBodyArguments(body: Parameter): ListBuffer[MappingParameter] = {
    val res = new ListBuffer[MappingParameter]

    if( body != null ) {
      extractBodyArguments("", body, res)
    }

    return res
  }

  private def extractBodyArguments(prefix: String, parameter: Parameter, extracted: ListBuffer[MappingParameter]): Unit = {

    val fullName = appendPrefix(prefix, parameter.name)

    if( parameter.logicalName != null ) {

      val m = MappingParameter.fromParameter(parameter);
      m.location = "body"
      m.name = fullName

      extracted.append(m)
    }

    if(parameter.kind == Types.Object && parameter.properties != null ) {
      parameter.properties.foreach( x => extractBodyArguments(fullName, x, extracted))
    }
  }

  private def appendPrefix(prefix: String, toAppend: String): String = {

    if( prefix.length() > 0){
      return prefix + "." + toAppend
    }
    return toAppend
  }

  private def aggregateParameters(parameters: List[MappingParameter]) : ListBuffer[MappingParameter] = {
    val sorted = parameters.sortBy( x => x.logicalName )
    val res = new ListBuffer[MappingParameter]

    sorted.foreach( p => {
      // if it's a nested argument
      val idx = p.logicalName.lastIndexOf('.')
      if(idx >= 0 ) {
        val prefix = p.logicalName.substring(0, idx)
        // safe to use res, if they are sorted parent should be already added
        val parent = res.find( x => x.logicalName.compare(prefix) == 0 )
        if( parent.isDefined ) {
          val par = parent.get
          par.properties =
            if (par.properties == null)
              Array(p)
            else
              par.properties ++ Array(p)

          if(p.name.startsWith(par.name)){
            p.name = p.name.substring(par.name.length)
          }

          if(p.logicalName.startsWith(par.logicalName)){
            p.logicalName = p.logicalName.substring(par.logicalName.length)
          }
        }
        else {
          res.append(p)
        }
      }
      else {
        res.append(p)
      }

    })

    return res
  }

}
