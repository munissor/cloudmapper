package com.munisso.proxyapp.tests.utils

import org.apache.commons.io.IOUtils
import org.apache.http.{HttpEntity, HttpHost, HttpResponse}
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.{HttpUriRequest, RequestBuilder}
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.conn.DefaultProxyRoutePlanner
import org.apache.http.util.EntityUtils
import org.xmlunit.builder.DiffBuilder
import org.apache.commons.io.input.BOMInputStream
import org.xmlunit.diff.{DefaultNodeMatcher, ElementSelectors}

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

    proxyRequest = RequestBuilder.create(method).setUri(proxyUri)
    customizeRequest(proxyRequest)
  }

  def execute(body: Option[String] = None): Unit = {
    val providerReq = buildRequest(body, providerRequest)
    providerResponse = httpClient.execute(providerReq)
    val e = providerResponse.getEntity
    if (e != null)
      providerResponseEntity = getEncoding(e).trim
    EntityUtils.consume(e)

    val proxyReq = buildRequest(body, proxyRequest)
    proxyResponse = httpClient.execute(proxyReq)
    val pe = proxyResponse.getEntity
    if (pe != null)
      proxyResponseEntity = getEncoding(pe).trim
    EntityUtils.consume(pe)
  }

  def customizeBuilder(builder: HttpClientBuilder): Unit

  def customizeRequest(requestBuilder: RequestBuilder): Unit

  def addHeaders(name: String, value: String): Unit = {
    providerRequest.addHeader(name, value)
    proxyRequest.addHeader(name, value)
  }

  def testHeader(name: String): TestResult = {
    val providerHeader = providerResponse.getFirstHeader(name)
    val proxyHeader = proxyResponse.getFirstHeader(name)

    if (providerHeader != null && proxyHeader != null)
      new TestResult(name, providerHeader.getValue, proxyHeader.getValue)
    else
      new TestResult(name, providerHeader.getValue, null, "Header missing from the proxy response")
  }

  def testResponseCode: TestResult = {
    new TestResult("$status", providerResponse.getStatusLine.getStatusCode.toString, proxyResponse.getStatusLine.getStatusCode.toString)
  }

  private def getEncoding(entity: HttpEntity): String = {
    val bis = new BOMInputStream(entity.getContent)
    val encoding = if (bis.hasBOM)
      bis.getBOMCharsetName
    else
      "utf-8"

    IOUtils.toString(bis, encoding)
  }

  def compare(): List[TestResult] = {

    compareResponse() ::: compareHeaders() ::: compareBody()
  }

  def compareResponse(): List[TestResult] = {
    List(testResponseCode)
  }

  def compareHeaders(): List[TestResult] = {
    providerResponse.getAllHeaders.map( h => testHeader(h.getName) ).toList
  }

  def compareBody(): List[TestResult] = {
//    val d = DiffBuilder.compare(providerResponseEntity)
//       .withTest(proxyResponseEntity)
//      .ignoreWhitespace()
//      .withNodeMatcher(new DefaultNodeMatcher(ElementSelectors.byNameAndText))
//      .build()
    List()
  }


  private def buildRequest(body: Option[String], request: RequestBuilder): HttpUriRequest = {
    body match {
      case Some(i) => request.setEntity(new StringEntity(i))
      case None =>
    }

    request.build
  }



}
