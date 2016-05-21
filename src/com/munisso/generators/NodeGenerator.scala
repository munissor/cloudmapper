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
    pkg.name = "config/config.json"

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
    indentedPrintWriter.printLn("var client = restify.createStringsClient({")
    indentedPrintWriter.increaseIndent()
    indentedPrintWriter.printLn("url: %s,", buildRequestUrl(route))
    indentedPrintWriter.printLn("signRequest: signature.buildSignature('%s')", mapping.signature )
    indentedPrintWriter.decreaseIndent()
    indentedPrintWriter.printLn("});")

    indentedPrintWriter.printLn(String.format("client.%s(TODO, function(cerr, creq, cres, cobj){", getRestifyRoute(route.remoteVerb)))
    indentedPrintWriter.increaseIndent()
    indentedPrintWriter.decreaseIndent()
    indentedPrintWriter.printLn("}")
    indentedPrintWriter.printLn()

    // END ROUTE
    indentedPrintWriter.decreaseIndent()
    indentedPrintWriter.printLn("}")
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
      .map( x => String.format(".replace('{%s}', %s)", x.name, this.requestVariable(x)))
      .mkString("")

    String.format("'%s'%s", route.remoteUrl, replacements)
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

    indentedWriter.printLn("var restify = require('restify');")
    indentedWriter.printLn("var config = require('config');")
    indentedWriter.printLn("var signature = require('./signature');")
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
    indentedWriter.printLn("}")

    pkg.code = stringWriter.toString

    return pkg

    /*
    var restify = require('restify');


    function respond(req, res, next) {
      res.send('hello ' + req.params.name);
      next();
    }

    var server = restify.createServer();
    server.get('/hello/:name', respond);
    server.head('/hello/:name', respond);

    server.listen(8080, function() {
      console.log('%s listening at %s', server.name, server.url);
    });

      */


  }


}
