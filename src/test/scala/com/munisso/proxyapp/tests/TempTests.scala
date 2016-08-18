package com.munisso.proxyapp.tests

import com.munisso.proxyapp.tests.utils.AzureSignature
import org.junit.runners.MethodSorters
import org.junit.{FixMethodOrder, Test}

/**
  * Created by rmunisso on 15/07/2016.
  */
//@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class TempTests {

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