package com.munisso.proxyapp.tests.utils

/**
  * Created by riccardo on 26/06/16.
  */
class TestResult(val name: String, val expected: String, val actual: String, val message: String = null) {
  def compare(): Boolean = {
     expected == actual
  }

  override def toString(): String = {
    String.format("[%s] Expected: %s, Actual: %s. %s", name, expected, actual, message)
  }
}