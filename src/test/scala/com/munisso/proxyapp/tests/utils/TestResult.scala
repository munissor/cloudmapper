package com.munisso.proxyapp.tests.utils

/**
  * Created by riccardo on 26/06/16.
  */
class TestResult(val name: String, val valueComparer: ValueComparer, val expected: String, val actual: String, val message: String = null) {

  def compare(): Boolean = {
     valueComparer.compare(actual, expected)
  }

  override def toString: String = {
    String.format("%s [%s] Expected: %s, Actual: %s. %s",
      if(compare()) "OK" else "NO",
      name,
      expected,
      actual,
      if(message == null) "" else message )
  }
}