package com.munisso

import java.io.{File, FileReader, FileWriter}
import java.nio.file.Paths

import com.fasterxml.jackson.databind.{ObjectMapper, SerializationFeature}
import com.munisso.generators.GeneratorFactory
import com.munisso.mappers.ModelMapper
import com.munisso.models.Model


object Program {

   def main(args: Array[String]): Unit = {
     val mapper: ObjectMapper = new ObjectMapper
     mapper.configure(SerializationFeature.INDENT_OUTPUT, true);

     val azure = new File("./Resources/azure_storage.json")
     val aws = new File("./Resources/aws_storage.json")
     val azureModel = mapper.readValue(azure, classOf[Model])
     val awsModel = mapper.readValue(aws, classOf[Model])

     val modelMapper = new ModelMapper()




     val mapping = modelMapper.mapModels(azureModel, awsModel)

     val out = new FileWriter("./output.mapper")
     mapper.writeValue(out, mapping)
     out.close()

     val generator = GeneratorFactory.getGenerator("nodejs").get

     val code = generator.generate(mapping)

     var basePath = Paths.get(System.getProperty("user.dir"), "nodeproxy").toString

     code.foreach( x => {
       var file = Paths.get(basePath, x.name).toFile
       var dir = file.getParentFile()

       dir.mkdirs()
       val w = new FileWriter(file)
       w.write(x.code)
       w.close()
     })


   }


}