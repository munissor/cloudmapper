package com.munisso.generators

import java.io.StringWriter
import java.net.URI

import com.munisso.models._
import com.munisso.util.IndentedPrintWriter

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer

/**
  * Created by rmunisso on 02/05/2016.
  */
class NodeGenerator extends Generator {

  private val LOCATION_HEADER: String = "header"
  private val LOCATION_URL: String = "url"
  private val LOCATION_QUERY: String = "query"
  private val LOCATION_BODY: String = "body"


  def generate(mapping: Mapping): List[CodeFile] = {
    val l = new ListBuffer[CodeFile]

    l.append(readResource("package.json"))
    l.append(generateConfig())

    l.append(generateProxy(mapping))

    l.append(readResource("signature.js"))
    l.append(readResource("formatUtils.js"))

    l.append(readResource("xmlParser.js"))
    l.append(readResource("jsonParser.js"))
    l.append(readResource("parserFactory.js"))

    l.append(readResource("xmlWriter.js"))
    l.append(readResource("jsonWriter.js"))
    l.append(readResource("writerFactory.js"))

    l.toList
  }

  private def readResource(resource: String): CodeFile = {
    val pkg = new CodeFile()
    pkg.name = resource
    pkg.code = scala.io.Source.fromFile("./Resources/NodeGenerator/" + resource ).mkString

    return pkg
  }


  private def generateConfig(): CodeFile = {
    val pkg = new CodeFile()
    pkg.name = "config/default.json"

    val stringWriter = new StringWriter()
    val indentedWriter = new IndentedPrintWriter(stringWriter)

    indentedWriter.printLn("{")
    indentedWriter.increaseIndent()

    indentedWriter.printLn("\"port\": 8080")

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

    indentedPrintWriter.printLn("server.%s('%s', function(req, res, next){", getRestifyRoute(route.verb), path)
    indentedPrintWriter.increaseIndent()

    writeMappingError(route.routeError, indentedPrintWriter)

    // extracts url parameters
    val reader = new NodeGeneratorRestifyPropertyReader(indentedPrintWriter)
    reader.extractProperties(route.parseRequest.asScala)

    /*val parseRequest = route.parseRequest.asScala
    parseRequest.filter( x => x.location == LOCATION_URL).foreach( x => {
      val varName = this.requestVariable(x)

      indentedPrintWriter.printLn("var %s = %s;", varName, formatExtractParameter("req.params.%s", x))
    })

    parseRequest.filter( x => x.location == LOCATION_QUERY).foreach( x => {
      val varName = this.requestVariable(x)
      indentedPrintWriter.printLn("var %s = %s;", varName, formatExtractParameter("req.query.%s", x))
    })

    parseRequest.filter( x => x.location == LOCATION_HEADER).foreach( x => {
      val varName = this.requestVariable(x)
      indentedPrintWriter.printLn("var %s = %s;", varName, formatExtractParameter("req.header('%s')", x))
    })

    parseRequest.filter( x => x.location == null).foreach( x => {
      val varName = this.requestVariable(x)
      indentedPrintWriter.printLn("var %s = '%s';", varName, x.value)
    })*/


    // BUILD request
    indentedPrintWriter.printLn()
    // TODO: don't hardcode protocol
    indentedPrintWriter.printLn("var urlString = 'https://%s';", route.remoteUrl)
    indentedPrintWriter.printLn("var rHeaders = {};");

    var reqWriter = new NodeGeneratorRequestPropertyWriter(indentedPrintWriter)
    reqWriter.writeProperties(route.buildRequest.asScala)

/*
    indentedPrintWriter.printLn()
    route.buildRequest.asScala.filter( p => p.location == LOCATION_HEADER)
      .foreach( x => indentedPrintWriter.printLn("rHeaders['%s'] = %s;", x.name, formatValue(x) ))
*/

    var body = null
    indentedPrintWriter.printLn()
    indentedPrintWriter.printLn("var body = '';")
    indentedPrintWriter.printLn()

    indentedPrintWriter.printLn("var options = {method: '%s', url: urlString, body: body, headers: rHeaders};", route.remoteVerb )
    indentedPrintWriter.printLn()

    indentedPrintWriter.printLn("signature.buildSignature('%s', options);", mapping.signature)
    indentedPrintWriter.printLn()
    indentedPrintWriter.printLn("request(options, function(error, response, body){")
    indentedPrintWriter.increaseIndent()


    //val parseResponse = route.parseResponse.asScala
    var resReader = new NodeGeneratorRequestPropertyReader(indentedPrintWriter)
    resReader.extractProperties(route.parseResponse.asScala)

    indentedPrintWriter.printLn()

    // Build response
    var resWriter = new NodeGeneratorRestifyPropertyWriter(indentedPrintWriter)
    resWriter.writeProperties(route.buildResponse.asScala)

    /*val buildResponse = route.buildResponse.asScala
    buildResponse.filter( x => x.location == LOCATION_HEADER).foreach( x => {
      val varName = this.responseVariable(x)
      indentedPrintWriter.printLn("var %s = %s;", varName, formatExtractParameter("req.headers['%s']", x))
      indentedPrintWriter.printLn("res.header('%s', %s);", x.name, formatValue(x))
    })*/

    // TODO: map status ?
    indentedPrintWriter.printLn("var status = response.statusCode;")

    // TODO: parse response body
    indentedPrintWriter.printLn("var rBody = dstResWriter.toString();")

    indentedPrintWriter.printLn("res.send(status, rBody);")
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

  private def buildRequestUrl(route: Route): String = {
    val replacements = route.buildRequest.asScala
      .filter( x => x.location == LOCATION_URL)
      .map( x => String.format(".replace('{%s}', %s)", x.name, this.formatValue(x)))
      .mkString("")

    // TODO: don't hardcode protocol
    String.format("'https://%s'%s", route.remoteUrl, replacements)
  }

  private def formatValue(parameter: MappingParameter): String = {
    if(parameter.format != null && parameter.format.length() > 0 ){
      return String.format("formatUtils.format%s(%s, '%s')", parameter.kind, this.requestVariable(parameter), parameter.format)
    }

    this.requestVariable(parameter)
  }

  private def formatExtractParameter(format: String, parameter: MappingParameter) = getNames(parameter).map( String.format(format, _)).mkString(" || ")

  private def writeMappingError(error: MappingError, indentedPrintWriter: IndentedPrintWriter) = {
    if(error != null) {
      indentedPrintWriter.printLn("// FIXME")
      indentedPrintWriter.printLn("throw new Error(%s)", error.message)
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
