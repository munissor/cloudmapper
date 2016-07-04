package com.munisso.proxyapp.generators

import com.munisso.proxyapp.models._

/**
  * Created by rmunisso on 02/05/2016.
  */
trait Generator {
  def generate(mapping: Mapping): List[CodeFile]
}
