package com.munisso.proxyapp.tests




import java.net.URL

import com.munisso.proxyapp.tests.utils._
import org.junit.{Before, Test}

/**
  * Created by riccardo on 26/06/16.
  */
class CorrectnessTests {

  private def generateProxyUrl(azureUrl: String): String = {
    val url = new URL(azureUrl)
    azureUrl.replace(url.getHost, "127.0.0.1:3000")
  }

  @Test def testListContainer(): Unit = {

    val url = "http://riccardonci.blob.core.windows.net/?comp=list"
    val proxyUrl = generateProxyUrl(url)
    val tester = new AzureRequestTester("GET", url, proxyUrl)
    tester.create()
    tester.execute()
  }

  @Test def testCreateContainer(): Unit = {

    val url = "http://riccardonci.blob.core.windows.net/unittestcontainer3?restype=container"
    val proxyUrl = generateProxyUrl(url)
    val tester = new AzureRequestTester("PUT", url, proxyUrl)
    tester.create()
    tester.addHeaders("Content-Length", 0.toString)
    tester.execute()


    val testerDel = new AzureRequestTester("DELETE", url, proxyUrl)
    testerDel.create()
    testerDel.execute()
  }

  private def body = "<data><item>value</item><data>"

  @Test def testPutBlob(): Unit = {
    val url = "http://riccardonci.blob.core.windows.net/testapicontainer/blob.xml"
    val proxyUrl = generateProxyUrl(url)
    val tester = new AzureRequestTester("PUT", url, proxyUrl)
    tester.create()
    tester.addHeaders("x-ms-blob-type", "BlockBlob")
    tester.addHeaders("Content-Type", "application/xml")
    tester.execute(Some(body))
  }


  @Test def testGetBlob(): Unit = {
    val url = "http://riccardonci.blob.core.windows.net/testapicontainer/blob.xml"
    val proxyUrl = generateProxyUrl(url)
    val tester = new AzureRequestTester("GET", url, proxyUrl)
    tester.create()
    tester.execute()
  }

  @Test def testDeleteBlob(): Unit = {
    val url = "http://riccardonci.blob.core.windows.net/testapicontainer/blob.xml"
    val proxyUrl = generateProxyUrl(url)
    val tester = new AzureRequestTester("DELETE", url, proxyUrl)
    tester.create()
    tester.execute()
  }


  @Test def testListQueues(): Unit = {
    val url = "http://riccardonci.queue.core.windows.net/?comp=list"
    val proxyUrl = generateProxyUrl(url)
    val tester = new AzureRequestTester("GET", url, proxyUrl)
    tester.create()
    tester.execute()
  }

  @Test def testCreateQueue(): Unit = {
    val url = "http://riccardonci.queue.core.windows.net/unittestqueue"
    val proxyUrl = generateProxyUrl(url)
    val tester = new AzureRequestTester("PUT", url, proxyUrl)
    tester.create()
    tester.addHeaders("Content-Length", 0.toString)
    tester.execute()
  }

  @Test def testDeleteQueue(): Unit = {
    val url = "http://riccardonci.queue.core.windows.net/unittestqueue"
    val proxyUrl = generateProxyUrl(url)
    val tester = new AzureRequestTester("DELETE", url, proxyUrl)
    tester.create()
    tester.addHeaders("Content-Length", 0.toString)
    tester.execute()
  }

  private def message = "<QueueMessage><MessageText>test-message</MessageText></QueueMessage>"

  @Test def testCreateMessage(): Unit = {
    val url = "http://riccardonci.queue.core.windows.net/unittestqueue/messages"
    val proxyUrl = generateProxyUrl(url)
    val tester = new AzureRequestTester("POST", url, proxyUrl)
    tester.create()
    tester.addHeaders("Content-Type", "application/xml")
    tester.execute(Some(message))
  }

  @Test def testGetMessage(): Unit = {
    val url = "http://riccardonci.queue.core.windows.net/unittestqueue/messages"
    val proxyUrl = generateProxyUrl(url)
    val tester = new AzureRequestTester("GET", url, proxyUrl)
    tester.create()
    tester.addHeaders("Content-Length", 0.toString)
    tester.execute()
  }

  @Test def testPurgeMessages(): Unit = {
    val url = "http://riccardonci.queue.core.windows.net/unittestqueue/messages"
    val proxyUrl = generateProxyUrl(url)
    val tester = new AzureRequestTester("DELETE", url, proxyUrl)
    tester.create()
    tester.addHeaders("Content-Length", 0.toString)
    tester.execute()
  }
}
