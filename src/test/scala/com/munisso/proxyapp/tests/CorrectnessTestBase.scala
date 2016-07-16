package com.munisso.proxyapp.tests

import com.munisso.proxyapp.tests.utils.TestResult
import org.junit.{AfterClass, BeforeClass}

import scala.collection.mutable.ListBuffer

/**
  * Created by riccardo on 16/07/16.
  */
class CorrectnessTestBase {

}

object CorrectnessTestBase {

  private val testResults = ListBuffer[(String, List[TestResult])] ()

  def addResults(method: String, results: List[TestResult]) = {
    testResults += Tuple2(method, results)
  }

  @AfterClass
  def teardown(): Unit = {
    testResults.foreach( m => {
      println(m._1)
      m._2.foreach( r => println(r.toString))
    })
  }
}