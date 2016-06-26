package com.munisso.proxyapp.tests.utils

import java.text.SimpleDateFormat
import java.util.{Date, TimeZone}

import org.apache.http.client.methods.RequestBuilder
import org.apache.http.{HttpRequest, HttpRequestInterceptor}
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.protocol.HttpContext

/**
  * Created by riccardo on 26/06/16.
  */
class AzureRequestTester(method: String, providerUri: String, proxyUri: String)
  extends RequestTester(method, providerUri, proxyUri) {

  class AzureSignatureInterceptor extends HttpRequestInterceptor {
    val secret: String = "B7wPXLWFU4BP62Z4fKBvQfiIRsMblRkzB49CaBGms8HMwj6X6q5a1CellQeSglRcmdtQz+bgxkC0reNmu9GxPQ=="

    def process(request: HttpRequest, context: HttpContext): Unit = {
      try {
        AzureSignature.SignRequest(request, "riccardonci", secret)
      }
      catch {
        case e: Exception => {
        }
      }
    }
  }

  val utcTime = getAzureDate()

  private def getAzureDate(): String = {

    val DATEFORMAT: String = "EEE, dd MMM yyyy HH:mm:ss z"

    val sdf: SimpleDateFormat = new SimpleDateFormat(DATEFORMAT)
    sdf.setTimeZone(TimeZone.getTimeZone("GMT"))
    sdf.format(new Date)
  }


  def customizeBuilder(builder: HttpClientBuilder): Unit = {
    builder.addInterceptorLast(new AzureSignatureInterceptor)
  }

  def customizeRequest(requestBuilder: RequestBuilder): Unit = {
    requestBuilder.addHeader("x-ms-date", utcTime)
    requestBuilder.addHeader("x-ms-version", "2015-04-05")
  }


}
