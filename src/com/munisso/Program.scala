package com.munisso

import java.io.File

import com.fasterxml.jackson.databind.ObjectMapper
import com.munisso.models.Model


object Program {

   def main(args: Array[String]): Unit = {
     val mapper: ObjectMapper = new ObjectMapper

     val azure = new File("./Resources/azure_storage.json")
     val aws = new File("./Resources/aws_storage.json")
     val azureModel = mapper.readValue(azure, classOf[Model])
     val awsModel = mapper.readValue(aws, classOf[Model])

     val modelMapper = new ModelMapper();

     val out = new File("./output.mapper")

     modelMapper.mapModels(azureModel, awsModel, out);


   }


}