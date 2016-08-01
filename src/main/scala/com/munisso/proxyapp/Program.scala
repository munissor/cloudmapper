package com.munisso.proxyapp

import java.io.{File, FileReader, FileWriter}
import java.nio.file.Paths

import com.fasterxml.jackson.databind.{ObjectMapper, SerializationFeature}
import com.munisso.proxyapp.generators.GeneratorFactory
import com.munisso.proxyapp.mappers.ModelMapper
import com.munisso.proxyapp.models.{Mapping, Model}



object Program {

  def main(args: Array[String]): Unit = {
     val cmdParser = new scopt.OptionParser[Config]("proxyapp") {
       head("proxyapp", "1.x")

       cmd("map").action((_, c) => c.copy(operation = "map"))
         .text("Generate the mapping between two providers")
         .children(
           opt[String]('v', "service").required().action((x, c) => c.copy(service = x)).text("The service to map"),
           opt[String]('s', "source").required().action((x, c) => c.copy(source = x)).text("The name of the source provider"),
           opt[String]('d', "destination").required().action((x, c) => c.copy(destination = x)).text("The name of the destination provider"),
           opt[String]('o', "output").required().action((x, c) => c.copy(output = x)).text("Name of the ouput file")
         )

       cmd("check").action((_, c) => c.copy(operation = "check"))
         .text("Check the mapping between two providers")
         .children(
           opt[String]('v', "service").required().action((x, c) => c.copy(service = x)).text("The service to map"),
           opt[String]('s', "source").required().action((x, c) => c.copy(source = x)).text("The name of the source provider"),
           opt[String]('d', "destination").required().action((x, c) => c.copy(destination = x)).text("The name of the destination provider")
         )

       cmd("generate").action((_, c) => c.copy(operation = "generate"))
         .text("Generate the proxy in the specified programming language")
         .children(
           opt[String]('i', "input").required().action((x, c) => c.copy(input = x)).text("The mapping program"),
           opt[String]('o', "output").required().action((x, c) => c.copy(output = x)).text("The output folder"),
           opt[String]('l', "language").required().action((x, c) => c.copy(language = x)).text("The programming language to use when generating the proxy")
         )

       checkConfig(x =>
         if (x.operation == "") {
           failure("You must specify an operation")
         }
         else if(x.operation == "map" || x.operation == "check" ){
           val src = modelFile(x.service, x.source)
           val dst = modelFile(x.service, x.destination)

           if(src.exists() && dst.exists() )
             success
           else if(!src.exists())
             failure("The model for the source provider is not available for the specified service")
           else
             failure("The model for the destination provider is not available for the specified service")
         }
         else if(x.operation == "generate" ){
           val mapping = new File(x.input)
           if(mapping.exists())
             success
           else
             failure("The input file doesn't exist")
         }
         else {
           success
         }
       )
     }

    cmdParser.parse(args, Config()) match {
      case Some(config) => config.operation match {
        case "map" => {
          val src = modelFile(config.service, config.source)
          val dst = modelFile(config.service, config.destination)
          val mapping = generateMapping(src, dst)
          saveMapping(mapping, config.output)
        }
        case "check" => {
          val src = modelFile(config.service, config.source)
          val dst = modelFile(config.service, config.destination)
          val mapping = generateMapping(src, dst)
          checkMapping(mapping)
        }
        case "generate" => {
          generateProxy(config.input, config.language, config.output)
        }
      }
      case None =>
    }

     // AZURE => AWS Storage
//     createProxy(
//       "./src/main/resources/azure_storage.json",
//       "./src/main/resources/aws_storage.json",
//       "./output.mapper",
//       "nodeproxy")

     // AZURE => AWS Queue
//     createProxy(
//       "./src/main/resources/azure_queue.json",
//       "./src/main/resources/aws_queue.json",
//       "./output.mapper",
//       "nodeproxy")

     // AZURE => GOOGLE Storage
//     createProxy(
//       "./src/main/resources/azure_storage.json",
//       "./src/main/resources/google_storage.json",
//       "./output.mapper",
//       "nodeproxy")
   }

  private def modelFile(service: String, provider: String): File = new File(String.format("./src/main/resources/%s_%s.json", provider, service))

  private def generateMapping(srcData: File, dstData: File): Mapping = {
    val mapper: ObjectMapper = new ObjectMapper

    val srcModel = mapper.readValue(srcData, classOf[Model])
    val dstModel = mapper.readValue(dstData, classOf[Model])

    val modelMapper = new ModelMapper()

    modelMapper.mapModels(srcModel, dstModel)
  }

  private def saveMapping(mapping: Mapping, outPath: String): Unit = {
    val mapper: ObjectMapper = new ObjectMapper
    mapper.configure(SerializationFeature.INDENT_OUTPUT, true)

    val out = new FileWriter(outPath)
    mapper.writeValue(out, mapping)
    out.close()
  }

  private def checkMapping(mapping: Mapping): Unit = {
    // TODO
  }

  def generateProxy(input: String, language: String, basePath: String): Unit = {
    val mapper: ObjectMapper = new ObjectMapper
    val inputData = new File(input)
    val mapping = mapper.readValue(inputData, classOf[Mapping])

    val generator = GeneratorFactory.getGenerator(language)
    generator match {
      case Some(g) => {
        val code = g.generate(mapping)

        code.foreach( x => {
          val file = Paths.get(basePath, x.name).toFile
          val dir = file.getParentFile

          dir.mkdirs()
          val w = new FileWriter(file)
          w.write(x.code)
          w.close()
        })
      }
      case None => println("Language not supported")
    }
  }

  def xgenerateMapping(srcData: File, trgData: File, outPath: String): Unit = {
    val mapper: ObjectMapper = new ObjectMapper

    val srcModel = mapper.readValue(srcData, classOf[Model])
    val dstModel = mapper.readValue(trgData, classOf[Model])

    val modelMapper = new ModelMapper()

    val mapping = modelMapper.mapModels(srcModel, dstModel)

    val out = new FileWriter(outPath)
    mapper.writeValue(out, mapping)
    out.close()
  }

   def createProxy(srcData: String, trgData: String, outName: String, proxyName: String): Unit = {
     val mapper: ObjectMapper = new ObjectMapper
     mapper.configure(SerializationFeature.INDENT_OUTPUT, true)





     val generator = GeneratorFactory.getGenerator("nodejs").get




   }


}