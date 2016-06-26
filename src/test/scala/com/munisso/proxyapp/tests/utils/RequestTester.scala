package com.munisso.proxyapp.tests.utils

import org.apache.http.{HttpHost, HttpResponse}
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.RequestBuilder
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.conn.DefaultProxyRoutePlanner
import org.apache.http.util.EntityUtils

/**
  * Created by riccardo on 26/06/16.
  */
abstract class RequestTester(val method: String, val providerUri: String, val proxyUri: String) {

  var useProxy: Boolean = false

  private var httpClient: HttpClient = null

  private var providerRequest: RequestBuilder = null
  private var providerResponse: HttpResponse = null
  private var providerResponseEntity: String = null

  private var proxyRequest: RequestBuilder = null
  private var proxyResponse: HttpResponse = null
  private var proxyResponseEntity: String = null


  def create(): Unit = {

    val useProxy: Boolean = false
    var builder: HttpClientBuilder = HttpClientBuilder.create
    builder = builder.disableContentCompression.disableConnectionState

    if (useProxy) {
      val p: HttpHost = new HttpHost("localhost", 8888, "http")
      val routePlanner: DefaultProxyRoutePlanner = new DefaultProxyRoutePlanner(p)
      builder = builder.setRoutePlanner(routePlanner)
    }

    customizeBuilder(builder)

    httpClient = builder.build

    providerRequest = RequestBuilder.create(method).setUri(providerUri)
    customizeRequest(providerRequest)

    proxyRequest =  RequestBuilder.create(method).setUri(proxyUri)
    customizeRequest(proxyRequest)
  }

  def execute(): Unit = {
    providerResponse = httpClient.execute(providerRequest.build)
    val e = providerResponse.getEntity()
    if( e != null )
      providerResponseEntity = EntityUtils.toString(e)
    EntityUtils.consume(e)

    proxyResponse = httpClient.execute(proxyRequest.build)
    val pe = proxyResponse.getEntity()
    if( pe != null )
      proxyResponseEntity = EntityUtils.toString(pe)
    EntityUtils.consume(pe)
  }

  def customizeBuilder(builder: HttpClientBuilder): Unit

  def customizeRequest(requestBuilder: RequestBuilder): Unit

  def addHeaders(name: String, value: String): Unit = {
    providerRequest.addHeader(name, value)
    proxyRequest.addHeader(name, value)
  }

  def testHeader(name: String): TestResult = {
    return new TestResult(name, providerResponse.getFirstHeader(name).getValue, proxyResponse.getFirstHeader(name).getValue)
  }

  def testResponseCode: TestResult = {
      new TestResult("$status", providerResponse.getStatusLine.getStatusCode.toString, proxyResponse.getStatusLine.getStatusCode.toString)
   }



}
