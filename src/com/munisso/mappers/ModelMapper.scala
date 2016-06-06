package com.munisso.mappers

import com.munisso.models._

import scala.collection.mutable._



/**
  * Created by rmunisso on 26/04/2016.
  */
class ModelMapper {


  private def extractArguments(location: String, parameters: Array[Parameter]): ListBuffer[MappingParameter] = {
    val res = new ListBuffer[MappingParameter]

    if (parameters != null) {

      parameters.foreach( h => {

        val m = new MappingParameter()
        m.location = location
        m.name = h.name
        m.aliases = h.aliases
        m.logicalName = h.logicalName
        m.kind = h.kind
        m.value = h.value
        m.optional = h.optional
        m.format = h.format
        m.multiple = h.multiple

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

      val m = new MappingParameter()
      m.location = "body"
      m.name = fullName
      m.aliases = parameter.aliases
      m.logicalName = parameter.logicalName
      m.kind = parameter.kind
      m.value = parameter.value
      m.optional = parameter.optional
      m.format = parameter.format
      m.multiple = parameter.multiple

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


  def mapModels(source: Model, destination: Model): Mapping = {

    val mapping = new Mapping
    mapping.configurations = destination.configurations
    mapping.signature = destination.signature

    source.operations.foreach(srcOperation => {

      val route = new Route(srcOperation.name, srcOperation.request.url, srcOperation.request.verb)
        mapping.routes.add(  route )

        // Find the matching operation in the destination model
        val dop = destination.operations.find( _.name == srcOperation.name )
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

          val sourceReqMap = sourceReqArgs.map( x => x.logicalName -> x ).toMap

          val destReqArgs =
            extractArguments("url", destOperation.request.urlReplacements) ++
            extractArguments("header", destOperation.request.headers) ++
            extractArguments("query", destOperation.request.queryString) ++
            extractBodyArguments(destOperation.request.body)

          destReqArgs.foreach( arg => {
            val p = sourceReqMap.get(arg.logicalName)

            // PARSE SOURCE REQUEST
            if( !p.isEmpty  ){
              route.parseRequest.add(p.get)
            }

            // BUILD DESTINATION REQUEST
            if( p.isEmpty ){

              if(arg.value != null ){
                route.buildRequest.add( arg )
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

          val destResMap =  destResArgs.map( x => x.logicalName -> x ).toMap

          val srcResArgs = extractArguments("header", srcOperation.response.headers) ++
            extractBodyArguments(srcOperation.response.body)

          srcResArgs.foreach( arg => {
            val p = destResMap.get(arg.logicalName)

            // PARSE DESTINATION RESPONSE
            if( !p.isEmpty  ){
              route.parseResponse.add(p.get)
            }

            // BUILD DESTINATION REQUEST
            if( p.isEmpty ) {

              if(arg.value != null ){
                route.buildResponse.add( arg )
              }
              else if (!arg.optional) {
                route.responseErrors.add(new MappingError(String.format("Missing mandatory parameter %s from response", arg.logicalName)))
              }
            }
            else {
              route.buildResponse.add( arg)
            }

          })
        }

    })

    return mapping
  }

  /*
*

Mapper foreach operation generate a route

    Handle request

        The request has parameters that can be anywhere (url, headers, body)
        Extract parameters from request using source model
        Convert each parameter to the format required by the destination model
        if mandatory parameter of the destination model is missing, output Error

        Foreach parameter, format request using destination model

        TODO: how do we describe the "body" or complex types in general (e.g: header containing JSON)?

        Sent the request to the destination provider

        THe response has parameters that can be in the header or body

        Extract parameters from response using destination model

        Convert each paramter to the format required by the source model

        If mandatory parameters are missing, output Error

 If operations are missing from destination provider, output an Error

*/
}
