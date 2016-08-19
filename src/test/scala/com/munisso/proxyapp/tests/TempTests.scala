package com.munisso.proxyapp.tests

import java.io.File

import com.fasterxml.jackson.databind.ObjectMapper
import com.munisso.proxyapp.models.{Mapping, MappingParameter, Parameter, Types}
import com.munisso.proxyapp.tests.utils.AzureSignature

import scala.collection.JavaConverters._
import org.junit.runners.MethodSorters
import org.junit.{FixMethodOrder, Test}

import scala.collection.mutable.ListBuffer

/**
  * Created by rmunisso on 15/07/2016.
  */
//@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class TempTests {

  @Test
  def testCorrectness(): Unit = {
    val mapper = new ObjectMapper
    val inputData = new File("/Users/rmunisso/code/college/proxyapp/output.mapper")
    val mapping = mapper.readValue(inputData, classOf[Mapping])

    mapping.routes.asScala.foreach(r => {
      print(r.name)
      val parseReq = flattenArguments(r.parseRequest.asScala.toList)
      val buildReq = flattenArguments(r.buildRequest.asScala.toList)
      val parseResp = flattenArguments(r.parseResponse.asScala.toList)
      val buildResp = flattenArguments(r.buildResponse.asScala.toList)
      print("\t")
      print(parseReq.length)
      print("\t")
      print(buildReq.length)
      print("\t")
      print(parseResp.length)
      print("\t")
      print(buildResp.length)
      print("\t")
      print(r.requestErrors.size())
      print("\t")
      print(r.responseErrors.size())
      println()



    })
  }

  private def flattenArguments(params: List[MappingParameter]): List[MappingParameter] = {
    params.flatMap(x => List(
      Option(x.logicalName) match {
        case Some(ln) => List(x)
        case None => Nil
      },
      Option(x.properties) match {
        case Some(p) => flattenArguments(x.properties.toList)
        case None => Nil
      })).flatten
  }

  @Test
  def test(): Unit = {
    var hash = AzureSignature.HashRequestString("GET\n\n\n\n\n\nThu, 18 Aug 2016 13:08:12 GMT\n\n\n\n\n\nx-ms-version:2009-09-19\n/riccardonci/\ncomp:list?comp=list\ninclude:metadata",
    "B7wPXLWFU4BP62Z4fKBvQfiIRsMblRkzB49CaBGms8HMwj6X6q5a1CellQeSglRcmdtQz+bgxkC0reNmu9GxPQ==")
    println(hash)
  }

  @Test
  def test10_test(): Unit ={
    println("Test 10")
  }

  @Test
  def test09_test(): Unit ={
    println("Test 9")
  }

  @Test
  def test03_test(): Unit ={
    println("Test 3")
  }

  @Test
  def test01_test(): Unit ={
    println("Test 1")
  }


}