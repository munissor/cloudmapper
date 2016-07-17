package com.munisso.proxyapp.tests

import java.net.URL

import com.munisso.proxyapp.tests.utils.{DateComparer, ExistsComparer}

/**
  * Created by riccardo on 16/07/16.
  */
class AzureCorrectnessTestsBase extends CorrectnessTestBase {

  protected val headerComparers = Map(
    "ETag" -> new ExistsComparer(),
    "x-ms-request-id" -> new ExistsComparer(),
    "Date" -> new DateComparer("EEE, dd MMM yyyy HH:mm:ss z", 3)
  )

  protected def generateProxyUrl(azureUrl: String): String = {
    val url = new URL(azureUrl)
    azureUrl.replace(url.getHost, "127.0.0.1:3000")
  }

}
