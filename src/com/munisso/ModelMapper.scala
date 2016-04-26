package com.munisso

import java.io.{File, PrintWriter}

import com.munisso.models.Model


/**
  * Created by rmunisso on 26/04/2016.
  */
class ModelMapper {

  def mapModels(source: Model, destination: Model, file: File): Unit = {
    val printWriter = new PrintWriter(file)
    val indentedWriter = new IndentedPrintWriter(printWriter)
    source.operations.foreach(operation => {
      try{

        indentedWriter.printLn("ROUTE '%1', '%2'", operation.name, operation.request.url)
        indentedWriter.increaseIndent()

        // Find the matching operation in the destination model
        val dop = destination.operations.find( _.name == operation.name )
        if(dop.isEmpty){
          indentedWriter.printLn("ERROR 'Operation missing from destination provider'")
        }
        else{
          val destOperation = dop.get;
          // TODO: map arguments here
        }

      }
      finally {
        indentedWriter.decreaseIndent()
      }

    });
    printWriter.flush();
    printWriter.close();
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
