package com.munisso.proxyapp.tests.utils

/**
  * Created by riccardo on 26/06/16.
  */
class TestResult(val name: String, val expected: String, val actual: String) {
  def compare(): Boolean = {
     expected == actual
  }
}
