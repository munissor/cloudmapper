package com.munisso.generators

import java.io.StringWriter
import java.net.URI

import com.munisso.models.{Mapping, MappingError, MappingParameter, Route}
import com.munisso.util.IndentedPrintWriter

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer

/**
  * Created by rmunisso on 02/05/2016.
  */
class NodeGenerator extends Generator {
  def generate(mapping: Mapping): List[CodeFile] = {
    val l = new ListBuffer[CodeFile]

    l.append(generatePackage())
    l.append(generateConfig())

    l.append(generateProxy(mapping))

    l.append(readResource("signature.js"))
    l.append(readResource("formatUtils.js"))

    l.toList
  }

  private def readResource(resource: String): CodeFile = {
    val pkg = new CodeFile()
    pkg.name = resource
    pkg.code = scala.io.Source.fromFile("./Resources/NodeGenerator/" + resource ).mkString

    return pkg
  }

  private def generatePackage(): CodeFile = {
    val pkg = new CodeFile()
    pkg.name = "package.json"

    val stringWriter = new StringWriter()
    val indentedWriter = new IndentedPrintWriter(stringWriter)

    indentedWriter.printLn("{")
    indentedWriter.increaseIndent()

    indentedWriter.printLn("\"name\": \"proxy\",")
    indentedWriter.printLn("\"version\": \"1.0.0\",")
    indentedWriter.printLn("\"main\": \"index.js\",")
    indentedWriter.printLn("\"dependencies\": {")

    indentedWriter.increaseIndent()

    indentedWriter.printLn("\"restify\": \"*\",")
    indentedWriter.printLn("\"config\": \"*\",")
    indentedWriter.printLn("\"moment\": \"*\",")
    indentedWriter.printLn("\"request\": \"*\",")
    // TODO: make these dependency optional depending on whether they are needed or not
    indentedWriter.printLn("\"aws-signer-v4\": \"*\"")
    indentedWriter.decreaseIndent()
    indentedWriter.printLn("}")

    indentedWriter.decreaseIndent()
    indentedWriter.printLn("}")

    pkg.code = stringWriter.toString

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
    val parseRequest = route.parseRequest.asScala
    parseRequest.filter( x => x.location == "url").foreach( x => {
      val varName = this.requestVariable(x)

      indentedPrintWriter.printLn("var %s = %s;", varName, formatExtractParameter("req.params.%s", x))
    })

    parseRequest.filter( x => x.location == "query").foreach( x => {
      val varName = this.requestVariable(x)
      indentedPrintWriter.printLn("var %s = %s;", varName, formatExtractParameter("req.query.%s", x))
    })

    parseRequest.filter( x => x.location == "header").foreach( x => {
      val varName = this.requestVariable(x)
      indentedPrintWriter.printLn("var %s = %s;", varName, formatExtractParameter("req.header('%s')", x))
    })

    // TODO: body

    // TODO: type conversion

    // BUILD request
    indentedPrintWriter.printLn()
    indentedPrintWriter.printLn("var urlString = %s;", buildRequestUrl(route))

    indentedPrintWriter.printLn()
    indentedPrintWriter.printLn("var rHeaders = {};");
    route.buildRequest.asScala.filter( p => p.location == "header")
      .foreach( x => indentedPrintWriter.printLn("rHeaders['%s'] = %s;", x.name, formatValue(x) ))

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
    indentedPrintWriter.printLn("console.log('x');")
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
      .filter( x => x.location == "url")
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

  private def generateProxy(mapping: Mapping): CodeFile = {
    val pkg = new CodeFile()
    pkg.name = "index.js"

    val stringWriter = new StringWriter()
    val indentedWriter = new IndentedPrintWriter(stringWriter)

    indentedWriter.printLn("var request = require('request');")
    indentedWriter.printLn("var restify = require('restify');")
    indentedWriter.printLn("var config = require('config');")
    indentedWriter.printLn("var signature = require('./signature');")
    indentedWriter.printLn("var formatUtils = require('./formatUtils');")
    indentedWriter.printLn("")
    indentedWriter.printLn("var server = restify.createServer();")
    indentedWriter.printLn("server.use(restify.queryParser());")
    indentedWriter.printLn("")

    mapping.routes.asScala.foreach(r => generateRoute(mapping, r, indentedWriter) )

    indentedWriter.printLn("")
    indentedWriter.printLn("server.listen(config.port, function() {")
    indentedWriter.increaseIndent()
    indentedWriter.printLn("console.log('%s listening at %s', server.name, server.url);")
    indentedWriter.decreaseIndent()
    indentedWriter.printLn("});")

    pkg.code = stringWriter.toString

    return pkg
  }


}
