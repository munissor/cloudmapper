package com.munisso.proxyapp.tests


import java.net.URL

import com.munisso.proxyapp.tests.utils._
import org.junit.runners.MethodSorters
import org.junit.{Before, FixMethodOrder, Test}

object AzureStorageCorrectnessTests {
  val containerName = "testapicontainer" + scala.util.Random.nextInt(100000).toString
}
/**
  * Created by riccardo on 26/06/16.
  */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class AzureStorageCorrectnessTests extends AzureCorrectnessTestsBase {


  private def generateAzureUrl(url: String, requiresContainer: Boolean = true) = {
    String.format("http://riccardonci.blob.core.windows.net/%s%s",
      if (requiresContainer) AzureStorageCorrectnessTests.containerName else "",
      url
    )
  }

  @Test def Test90_ListContainer(): Unit = {

    val url = generateAzureUrl("?comp=list", false)
    val proxyUrl = generateProxyUrl(url)
    val tester = new AzureRequestTester("GET", url, proxyUrl)
    tester.create()
    tester.execute()

    val results = tester.compare()
    CorrectnessTestBase.addResults("List Containers", results)
  }

  @Test def Test01_CreateContainer(): Unit = {

    val url = generateAzureUrl("?restype=container")
    val proxyUrl = generateProxyUrl(url)
    val tester = new AzureRequestTester("PUT", url, proxyUrl)
    tester.create()
    tester.addHeaders("Content-Length", 0.toString)
    tester.execute()

    val results = tester.compare()
    CorrectnessTestBase.addResults("Create Container", results)
  }

  @Test def Test99_DeleteContainer(): Unit = {
    val url = generateAzureUrl("?restype=container")
    val proxyUrl = generateProxyUrl(url)

    val tester = new AzureRequestTester("DELETE", url, proxyUrl)
    tester.create()
    tester.addHeaders("Content-Length", 0.toString)
    tester.execute()
    tester.compare()

    val results = tester.compare()
    CorrectnessTestBase.addResults("Delete Container", results)
  }

  private def body = "<data><item>value</item><data>"

  @Test def Test02_PutBlob(): Unit = {
    val url = generateAzureUrl("/blob.xml")
    val proxyUrl = generateProxyUrl(url)
    val tester = new AzureRequestTester("PUT", url, proxyUrl)
    tester.create()
    tester.addHeaders("x-ms-blob-type", "BlockBlob")
    tester.addHeaders("Content-Type", "application/xml")
    tester.execute(Some(body))

    val results = tester.compare()
    CorrectnessTestBase.addResults("Add Blob", results)
  }


  @Test def Test03_GetBlob(): Unit = {
    val url = generateAzureUrl("/blob.xml")
    val proxyUrl = generateProxyUrl(url)
    val tester = new AzureRequestTester("GET", url, proxyUrl)
    tester.create()
    tester.execute()

    val results = tester.compare()
    CorrectnessTestBase.addResults("Get Blob", results)
  }

  @Test def Test04_DeleteBlob(): Unit = {
    val url = generateAzureUrl("/blob.xml")
    val proxyUrl = generateProxyUrl(url)
    val tester = new AzureRequestTester("DELETE", url, proxyUrl)
    tester.create()
    tester.execute()

    val results = tester.compare()
    CorrectnessTestBase.addResults("Delete Blob", results)
  }
}
