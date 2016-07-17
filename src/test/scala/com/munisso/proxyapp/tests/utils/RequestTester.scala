package com.munisso.proxyapp.tests.utils

import java.io.{ByteArrayInputStream, InputStream}
import javax.xml.bind.ValidationEvent

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
  private var providerResponseStream: InputStream = null

  private var proxyRequest: RequestBuilder = null
  private var proxyResponse: HttpResponse = null
  private var proxyResponseStream: InputStream = null


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

    val proxyReq = buildRequest(body, proxyRequest)
    proxyResponse = httpClient.execute(proxyReq)

    providerResponseStream = cloneEntityStream(providerResponse)
    proxyResponseStream = cloneEntityStream(proxyResponse)
  }

  def customizeBuilder(builder: HttpClientBuilder): Unit

  def customizeRequest(requestBuilder: RequestBuilder): Unit

  def addHeaders(name: String, value: String): Unit = {
    providerRequest.addHeader(name, value)
    proxyRequest.addHeader(name, value)
  }

  def testHeader(name: String, valueComparer: ValueComparer): TestResult = {
    val providerHeader = providerResponse.getFirstHeader(name)
    val proxyHeader = proxyResponse.getFirstHeader(name)

    if (providerHeader != null && proxyHeader != null)
      new TestResult(name, valueComparer, providerHeader.getValue, proxyHeader.getValue)
    else
      new TestResult(name, valueComparer, providerHeader.getValue, null, "Header missing from the proxy response")
  }

  def testResponseCode: TestResult = {
    new TestResult("$status", new DefaultComparer(), providerResponse.getStatusLine.getStatusCode.toString, proxyResponse.getStatusLine.getStatusCode.toString)
  }

  def cloneEntityStream(response: HttpResponse): InputStream = {
    val pe = response.getEntity
    val stream = if (pe != null)
      new ByteArrayInputStream(IOUtils.toByteArray(pe.getContent))

    else
      new ByteArrayInputStream(Array[Byte]())

    EntityUtils.consume(pe)

    stream
  }

//  private def getEncoding(entity: HttpEntity): String = {
//    val bis = new BOMInputStream(entity.getContent)
//    val encoding = if (bis.hasBOM)
//      bis.getBOMCharsetName
//    else
//      "utf-8"
//
//    IOUtils.toString(bis, encoding)
//  }

  def compare(headersComparers: Map[String,ValueComparer], bodyComparer: BodyComparer): List[TestResult] = {

    compareResponse() ::: compareHeaders(headersComparers) ::: compareBody(bodyComparer)
  }

  def compareResponse(): List[TestResult] = {
    List(testResponseCode)
  }

  def compareHeaders(comparers: Map[String,ValueComparer]): List[TestResult] = {
    providerResponse.getAllHeaders.map( h => {
      val opt = comparers.get(h.getName)
      opt match {
        case Some(i) => testHeader(h.getName, i)
        case None => testHeader(h.getName, new DefaultComparer)
      }
    }).toList
  }

  def compareBody(bodyComparer: BodyComparer): List[TestResult] = {
//    val d = DiffBuilder.compare(providerResponseEntity)
//       .withTest(proxyResponseEntity)
//      .ignoreWhitespace()
//      .withNodeMatcher(new DefaultNodeMatcher(ElementSelectors.byNameAndText))
//      .build()
    bodyComparer.compare(providerResponseStream, proxyResponseStream)
  }


  private def buildRequest(body: Option[String], request: RequestBuilder): HttpUriRequest = {
    body match {
      case Some(i) => request.setEntity(new StringEntity(i))
      case None =>
    }

    request.build
  }



}
