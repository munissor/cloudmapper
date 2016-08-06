package com.munisso.proxyapp.mappers

import java.io.File

import com.fasterxml.jackson.databind.{ObjectMapper}

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

     // TODO: verify models

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
          extractArguments("url", mergeWithDefaults(srcOperation, source, x => x.request.urlReplacements)) ++
            extractArguments("header", mergeWithDefaults(srcOperation, source, x => x.request.headers)) ++
            extractArguments("query", mergeWithDefaults(srcOperation, source, x => x.request.queryString)) ++
            extractBodyArguments(srcOperation.request.body)

        val sourceReqMap = sourceReqArgs.map(x => x.logicalName -> x).toMap

        val destReqArgs =
          extractArguments("url", mergeWithDefaults(destOperation, destination, x => x.request.urlReplacements)) ++
            extractArguments("header", mergeWithDefaults(destOperation, destination, x => x.request.headers)) ++
            extractArguments("query", mergeWithDefaults(destOperation, destination, x => x.request.queryString)) ++
            extractBodyArguments(destOperation.request.body)

        destReqArgs.foreach(arg => {
          val p = sourceReqMap.get(arg.logicalName)

          // PARSE SOURCE REQUEST
          if (p.isDefined) {
            route.parseRequest.add(p.get)
          }

          // BUILD DESTINATION REQUEST
          if (p.isEmpty) {

            if (arg.value != null || arg.fallback != null) {
              val dup = MappingParameter.duplicate(arg)
              dup.location = null
              route.parseRequest.add(dup)

              route.buildRequest.add(arg)
            }
            else if(mapping.configurations.exists( c => c.key == arg.logicalName )){
              val dup = MappingParameter.duplicate(arg)
              dup.location = Locations.LOCATION_CONFIG

              route.parseRequest.add(dup)
              route.buildRequest.add(arg)
            }
            else if (!arg.optional && arg.logicalName != null ) {
              route.requestErrors.add(
                new MappingError(
                  String.format("Missing mandatory parameter %s (%s) from request", arg.logicalName, arg.kind)))
            }
          }
          else {
            route.buildRequest.add(arg)
          }
        })

        // REQUEST IS SENT HERE!!

        // PARSE DESTINATION RESPONSE

        val destResArgs = extractArguments("header", mergeWithDefaults(destOperation, destination, x => x.response.headers)) ++
          extractBodyArguments(destOperation.response.body)

        val destResMap = destResArgs.map(x => x.logicalName -> x).toMap

        val srcResArgs = extractArguments("header", mergeWithDefaults(srcOperation, source, x => x.response.headers)) ++
          extractBodyArguments(srcOperation.response.body)

        srcResArgs.foreach(arg => {
          val p = destResMap.get(arg.logicalName)

          // PARSE DESTINATION RESPONSE
          if (p.isDefined) {
            route.parseResponse.add(p.get)
          }

          // BUILD DESTINATION RESPONSE
          if (p.isEmpty) {
            val fallback = sourceReqArgs.find( x => x.logicalName == arg.logicalName && arg.fallback == FallbackOption.Mirror)
            if( fallback.isDefined ) {
              val f = fallback.get
              if(!route.parseRequest.contains(f)){
                route.parseRequest.add(f)
              }
              val dup = MappingParameter.duplicate(arg)
              dup.location = null
              route.parseResponse.add(dup)
              route.buildResponse.add(arg)
            }
            else if (arg.value != null) {
              route.buildResponse.add(arg)
            }
            else if(mapping.configurations.exists( c => c.key == arg.logicalName )){
              val dup = MappingParameter.duplicate(arg)
              dup.location = Locations.LOCATION_CONFIG

              route.parseResponse.add(dup)
              route.buildResponse.add(arg)
            }
            else if (!arg.optional && arg.logicalName != null) {
              route.responseErrors.add(
                new MappingError(
                  String.format("Missing mandatory parameter %s (%s) from response", arg.logicalName, arg.kind)))
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

    mapping
  }

  private def readModelFromFile(file: File): Model = {
    val mapper: ObjectMapper = new ObjectMapper
    mapper.readValue(file, classOf[Model])
  }

  private def extractArguments(location: String, parameters: List[Parameter]): List[MappingParameter] = {

    if (parameters != null)
      parameters.map( h => {
        val m = MappingParameter.fromParameter(h)
        m.location = location
        m
      })
    else
      List()
  }

  private def extractBodyArguments(body: Parameter): ListBuffer[MappingParameter] = {
    val res = new ListBuffer[MappingParameter]

    if( body != null ) {
      extractBodyArguments("", body, res)
    }

    res
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
    toAppend
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

    res
  }

  private def mergeWithDefaults(operation: Operation, model: Model, extractor: (Operation) => Array[Parameter] ) = {
    val definedParameters = safeExtractor(operation, extractor)
    val defaultParameters = if(model.commonParameters!=null) safeExtractor(model.commonParameters, extractor) else List()

    val missingDefaults = defaultParameters.filter( p => !definedParameters.exists( x => x.logicalName == p.logicalName ) )

    definedParameters ::: missingDefaults
  }

  private def safeExtractor(operation: Operation, extractor: (Operation) => Array[Parameter] ): List[Parameter] = {
    val extracted = extractor(operation)
    if( extracted != null) extracted.toList else Nil
  }

}
