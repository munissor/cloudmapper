package com.munisso.proxyapp.tests

import java.net.URL

/**
  * Created by riccardo on 16/07/16.
  */
class AzureCorrectnessTestsBase extends CorrectnessTestBase {

  protected def generateProxyUrl(azureUrl: String): String = {
    val url = new URL(azureUrl)
    azureUrl.replace(url.getHost, "127.0.0.1:3000")
  }

}
