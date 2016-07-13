package com.munisso.proxyapp

import java.io.{File, FileReader, FileWriter}
import java.nio.file.Paths

import com.fasterxml.jackson.databind.{ObjectMapper, SerializationFeature}
import com.munisso.proxyapp.generators.GeneratorFactory
import com.munisso.proxyapp.mappers.ModelMapper
import com.munisso.proxyapp.models.Model



object Program {

   def main(args: Array[String]): Unit = {
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
     createProxy(
       "./src/main/resources/azure_storage.json",
       "./src/main/resources/google_storage.json",
       "./output.mapper",
       "nodeproxy")
   }

   def createProxy(srcData: String, trgData: String, outName: String, proxyName: String): Unit = {
     val mapper: ObjectMapper = new ObjectMapper
     mapper.configure(SerializationFeature.INDENT_OUTPUT, true)

     val azure = new File(srcData)
     val aws = new File(trgData)
     val azureModel = mapper.readValue(azure, classOf[Model])
     val awsModel = mapper.readValue(aws, classOf[Model])

     val modelMapper = new ModelMapper()

     val mapping = modelMapper.mapModels(azureModel, awsModel)

     val out = new FileWriter(outName)
     mapper.writeValue(out, mapping)
     out.close()

     val generator = GeneratorFactory.getGenerator("nodejs").get

     val code = generator.generate(mapping)

     var basePath = Paths.get(System.getProperty("user.dir"), proxyName).toString

     code.foreach( x => {
       val file = Paths.get(basePath, x.name).toFile
       val dir = file.getParentFile

       dir.mkdirs()
       val w = new FileWriter(file)
       w.write(x.code)
       w.close()
     })


   }


}