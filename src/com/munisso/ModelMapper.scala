package com.munisso

import java.io.{File, PrintWriter}
import com.munisso.models._


/**
  * Created by rmunisso on 26/04/2016.
  */
class ModelMapper {

  private def extractArgumentsRequest(writer: IndentedPrintWriter, source: String, parameters: Array[Parameter]): Unit = {
    if(parameters != null) {
      parameters.foreach( h => {
        writer.printLn("REQEXTRACT '%s', '%s', '%s', '%s'", source, h.logicalName, h.`type`.toString, h.optional.toString )
      })
    }
  }

  private def useArgumentsRequest(writer: IndentedPrintWriter, source: String, parameters: Array[Parameter]): Unit = {
    if(parameters != null) {
      parameters.foreach( h => {
        writer.printLn("REQUSE '%s', '%s', '%s', '%s'", source, h.logicalName, h.`type`.toString, h.optional.toString )
      })
    }
  }

  private def extractArgumentsResponse(writer: IndentedPrintWriter, source: String, parameters: Array[Parameter]): Unit = {
    if(parameters != null) {
      parameters.foreach( h => {
        writer.printLn("RESEXTRACT '%s', '%s', '%s', '%s'", source, h.logicalName, h.`type`.toString, h.optional.toString )
      })
    }
  }

  private def useArgumentsResponse(writer: IndentedPrintWriter, source: String, parameters: Array[Parameter]): Unit = {
    if(parameters != null) {
      parameters.foreach( h => {
        writer.printLn("RESUSE '%s', '%s', '%s', '%s'", source, h.logicalName, h.`type`.toString, h.optional.toString )
      })
    }
  }

  def mapModels(source: Model, destination: Model, file: File): Unit = {
    val printWriter = new PrintWriter(file)
    val indentedWriter = new IndentedPrintWriter(printWriter)
    source.operations.foreach(operation => {
      //try{

        // TODO: remove this
        val req = if (operation.request == null) new Request() else operation.request

        indentedWriter.printLn("ROUTE \'%s\', \'%s\', \'%s\'", operation.name, req.verb, req.url)
        indentedWriter.increaseIndent()

        // Find the matching operation in the destination model
        val dop = destination.operations.find( _.name == operation.name )
        if(dop.isEmpty){
          indentedWriter.printLn("ERROR 'Operation missing from destination provider'")
        }
        else{
          val destOperation = dop.get
          val sourceMap = Map[String, Parameter]()

          // PARSE SOURCE REQUEST
          extractArgumentsRequest(indentedWriter, "url", operation.request.urlReplacements)
          extractArgumentsRequest(indentedWriter, "header", operation.request.headers)
          extractArgumentsRequest(indentedWriter, "query", operation.request.queryString)
          // TODO: how do we extract body

          // TODO: convert types

          // BUILD DESTINATION REQUEST
          indentedWriter.printLn("REQUEST \'%s\', \'%s\'", destOperation.request.url, destOperation.request.verb )

          useArgumentsRequest(indentedWriter, "url", destOperation.request.urlReplacements)
          useArgumentsRequest(indentedWriter, "header", destOperation.request.headers)
          useArgumentsRequest(indentedWriter, "query", destOperation.request.queryString)
          // TODO: build body

          // SEND REQUEST
          indentedWriter.printLn("SEND REQUEST")

          // PARSE DESTINATION RESPONSE
          extractArgumentsResponse(indentedWriter, "header", destOperation.response.headers)
          // TODO: how do we extract body

          // TODO: convert types

          // BUILD DESTINATION RESPONE
          useArgumentsResponse(indentedWriter, "header", operation.response.headers)
          // TODO: build body

          // RETURN
          indentedWriter.printLn("RETURN")
          indentedWriter.printLn("")
        }

      //}
      //catch{
      //  case e: Exception => {
      //
      //  }
      //}
      // {
        indentedWriter.decreaseIndent()
      //}

    })
    printWriter.flush()
    printWriter.close()
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
