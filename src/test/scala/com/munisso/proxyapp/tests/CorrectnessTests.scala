package com.munisso.proxyapp.tests


import java.text.SimpleDateFormat
import java.util.{Date, TimeZone}

import com.munisso.proxyapp.tests.utils._
import org.apache.http.{HttpHost, HttpRequestInterceptor}
import org.apache.http._
import org.apache.http.client.HttpClient
import org.apache.http.client.methods._
import org.apache.http.client.utils.URLEncodedUtils
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.conn.DefaultProxyRoutePlanner
import org.apache.http.protocol.HttpContext
import org.apache.http.util.EntityUtils
import org.junit.{Before, Test}

/**
  * Created by riccardo on 26/06/16.
  */
class CorrectnessTests {


  var httpClient: HttpClient = null


  @Test def testListContainer(): Unit = {

    val tester = new AzureRequestTester("GET", "http://riccardonci.blob.core.windows.net/?comp=list", "http://localhost:8080/?comp=list")
    tester.create()
    tester.execute()

 }




}
