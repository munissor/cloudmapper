package com.munisso.proxyapp.tests

import com.munisso.proxyapp.tests.utils.AzureRequestTester
import org.junit.FixMethodOrder
import org.junit.runners.MethodSorters

/**
  * Created by riccardo on 16/07/16.
  */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class AzureQueueCorrectnessTests extends AzureCorrectnessTestsBase {
  def testListQueues(): Unit = {
    val url = "http://riccardonci.queue.core.windows.net/?comp=list"
    val proxyUrl = generateProxyUrl(url)
    val tester = new AzureRequestTester("GET", url, proxyUrl)
    tester.create()
    tester.execute()
  }

  def testCreateQueue(): Unit = {
    val url = "http://riccardonci.queue.core.windows.net/unittestqueue"
    val proxyUrl = generateProxyUrl(url)
    val tester = new AzureRequestTester("PUT", url, proxyUrl)
    tester.create()
    tester.addHeaders("Content-Length", 0.toString)
    tester.execute()
  }

  def testDeleteQueue(): Unit = {
    val url = "http://riccardonci.queue.core.windows.net/unittestqueue"
    val proxyUrl = generateProxyUrl(url)
    val tester = new AzureRequestTester("DELETE", url, proxyUrl)
    tester.create()
    tester.addHeaders("Content-Length", 0.toString)
    tester.execute()
  }

  private def message = "<QueueMessage><MessageText>test-message</MessageText></QueueMessage>"

  def testCreateMessage(): Unit = {
    val url = "http://riccardonci.queue.core.windows.net/unittestqueue/messages"
    val proxyUrl = generateProxyUrl(url)
    val tester = new AzureRequestTester("POST", url, proxyUrl)
    tester.create()
    tester.addHeaders("Content-Type", "application/xml")
    tester.execute(Some(message))
  }

  def testGetMessage(): Unit = {
    val url = "http://riccardonci.queue.core.windows.net/unittestqueue/messages"
    val proxyUrl = generateProxyUrl(url)
    val tester = new AzureRequestTester("GET", url, proxyUrl)
    tester.create()
    tester.addHeaders("Content-Length", 0.toString)
    tester.execute()
  }

  def testPurgeMessages(): Unit = {
    val url = "http://riccardonci.queue.core.windows.net/unittestqueue/messages"
    val proxyUrl = generateProxyUrl(url)
    val tester = new AzureRequestTester("DELETE", url, proxyUrl)
    tester.create()
    tester.addHeaders("Content-Length", 0.toString)
    tester.execute()
  }
}
