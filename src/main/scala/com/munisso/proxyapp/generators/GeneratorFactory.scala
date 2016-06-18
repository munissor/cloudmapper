package com.munisso.proxyapp.generators

import com.munisso.proxyapp.generators.node._
import com.munisso.proxyapp.generators.scala._

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
