package com.munisso.proxyapp.generators.node

import java.io.StringWriter
import java.net.URI

import scala.collection.mutable.ListBuffer
import scala.collection.JavaConverters._
import scala.io.Source

import com.munisso.proxyapp.models._
import com.munisso.proxyapp.generators._
import com.munisso.proxyapp.util.IndentedPrintWriter

/**
  * Created by rmunisso on 02/05/2016.
  */
class NodeGenerator extends Generator {

  def generate(mapping: Mapping): List[CodeFile] = {
    val l = new ListBuffer[CodeFile]

    l.append(readResource("package.json"))
    l.append(generateConfig())

    l.append(generateProxy(mapping))

    l.append(readResource("signature.js"))
    l.append(readResource("formatUtils.js"))

    l.append(readResource("rawParser.js"))
    l.append(readResource("xmlParser.js"))
    l.append(readResource("jsonParser.js"))
    l.append(readResource("parserFactory.js"))

    l.append(readResource("rawWriter.js"))
    l.append(readResource("xmlWriter.js"))
    l.append(readResource("jsonWriter.js"))
    l.append(readResource("writerFactory.js"))

    l.toList
  }

  private def readResource(resource: String): CodeFile = {
    val pkg = new CodeFile()
    pkg.name = resource
    pkg.code = Source.fromFile("./src/main/resources/NodeGenerator/" + resource ).mkString

    return pkg
  }


  private def generateConfig(): CodeFile = {
    val pkg = new CodeFile()
    pkg.name = "config/default.json"

    val stringWriter = new StringWriter()
    val indentedWriter = new IndentedPrintWriter(stringWriter)

    indentedWriter.printLn("{")
    indentedWriter.increaseIndent()

    indentedWriter.printLn("\"port\": 3000")

    indentedWriter.decreaseIndent()
    indentedWriter.printLn("}")

    pkg.code = stringWriter.toString

    return pkg
  }

  private def generateRoute(mapping: Mapping, route: Route, indentedPrintWriter: IndentedPrintWriter): Unit = {

    if (route.url == null)
      return

    // the scheme here is not relevant, is just needed for url to be created correctly
    // Urls cannot contain { or }, replace curly brackets with __
    val u = "http://" + route.url.replace("{", "__").replace("}", "__")

    // TODO: what do we do with the domain? Restify just supports relative paths, the server needs to be configured with that (eg: nginx)
    val url = new URI(u)
    var path = url.getPath

    // replace url arguments with restify paramters
    path = path.replaceAll("__(:?[a-z0-9]+)__", ":$1")

    indentedPrintWriter.printLn("// %s", route.name)
    indentedPrintWriter.printLn("server.%s('%s', function(req, res, next){", getRestifyRoute(route.verb), path)
    indentedPrintWriter.increaseIndent()

    writeMappingError(route.routeError, indentedPrintWriter)

    val reader = new NodeGeneratorRestifyPropertyReader(indentedPrintWriter)
    reader.extractProperties(route.parseRequest.asScala)


    var reqWriter = new NodeGeneratorRequestPropertyWriter(indentedPrintWriter)
    reqWriter.writeProperties(route.remoteUrl, route.buildRequest.asScala)

    indentedPrintWriter.printLn("var options = {method: '%s', url: urlString, body: %s, headers: rHeaders};", route.remoteVerb, reqWriter.propertyNames.body )
    indentedPrintWriter.printLn()

    indentedPrintWriter.printLn("signature.buildSignature('%s', options);", mapping.signature)
    indentedPrintWriter.printLn()
    indentedPrintWriter.printLn("request(options, function(error, response, body){")
    indentedPrintWriter.increaseIndent()


    var resReader = new NodeGeneratorRequestPropertyReader(indentedPrintWriter)
    resReader.extractProperties(route.parseResponse.asScala)


    // Build response
    var resWriter = new NodeGeneratorRestifyPropertyWriter(indentedPrintWriter)
    resWriter.writeProperties("", route.buildResponse.asScala)


    // TODO: map status ?
    indentedPrintWriter.printLn("var status = response.statusCode;")

    indentedPrintWriter.printLn("res.writeHead(status, {});")
    indentedPrintWriter.printLn("res.write(%s);", resWriter.propertyNames.body)
    indentedPrintWriter.printLn("res.end();")
    indentedPrintWriter.printLn("return next();")

    indentedPrintWriter.decreaseIndent()
    indentedPrintWriter.printLn("});")
    indentedPrintWriter.printLn()

    // END ROUTE
    indentedPrintWriter.decreaseIndent()
    indentedPrintWriter.printLn("});")
    indentedPrintWriter.printLn()
  }

  private def getRestifyRoute(verb: String): String = {
    verb match {
      case "DELETE" => "del"
      case _ => verb.toLowerCase
    }
  }


  private def writeMappingError(error: MappingError, indentedPrintWriter: IndentedPrintWriter) = {
    if(error != null) {
      indentedPrintWriter.printLn("// FIXME")
      indentedPrintWriter.printLn("throw new Error('%s');", error.message)
    }
  }

  private def require(writer: IndentedPrintWriter, module: String, local: Boolean = false, variable: String = null) = {
    val m =
      if(local){
        "./" + module
      }
      else {
        module
      }

    val v =
      if(variable != null )
        variable
      else
        module

    writer.printLn("var %s = require('%s');", v, m)
  }

  private def generateProxy(mapping: Mapping): CodeFile = {
    val pkg = new CodeFile()
    pkg.name = "index.js"

    val stringWriter = new StringWriter()
    val indentedWriter = new IndentedPrintWriter(stringWriter)

    require(indentedWriter, "request")
    require(indentedWriter, "restify")
    require(indentedWriter, "config")

    require(indentedWriter, "parserFactory", true)
    require(indentedWriter, "writerFactory", true)
    require(indentedWriter, "signature", true)
    require(indentedWriter, "formatUtils", true)

    indentedWriter.printLn()
    indentedWriter.printLn("var server = restify.createServer();")
    indentedWriter.printLn("server.use(restify.queryParser());")
    indentedWriter.printLn("server.use(restify.bodyParser({ mapParams: false }));")
    indentedWriter.printLn()

    mapping.routes.asScala.foreach(r => generateRoute(mapping, r, indentedWriter) )

    indentedWriter.printLn()
    indentedWriter.printLn("server.listen(config.port, function() {")
    indentedWriter.increaseIndent()
    indentedWriter.printLn("console.log('%s listening at %s', server.name, server.url);")
    indentedWriter.decreaseIndent()
    indentedWriter.printLn("});")

    pkg.code = stringWriter.toString

    return pkg
  }


}
