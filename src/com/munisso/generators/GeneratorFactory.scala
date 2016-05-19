package com.munisso.generators

/**
  * Created by rmunisso on 02/05/2016.
  */
object GeneratorFactory {

  def getGenerator(name: String): Option[Generator] = {
      name match {
        case "nodejs" => Some(new NodeGenerator())
        case _ => None
      }
  }
}
