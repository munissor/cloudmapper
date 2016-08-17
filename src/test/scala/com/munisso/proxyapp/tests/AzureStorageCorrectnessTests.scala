package com.munisso.proxyapp.tests


import java.net.URL
import javax.xml.xpath.{XPathConstants, XPathFactory}

import com.munisso.proxyapp.tests.utils._
import org.junit.runners.MethodSorters
import org.junit.{Before, FixMethodOrder, Test}
import org.w3c.dom.{Document, Element}

object AzureStorageCorrectnessTests {
  val containerName = "ncipictures2"
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

    val results = tester.compare(headerComparers, new XmlBodyComparer((p: XmlBodyComparer, a: Document, e: Document) => {
      val containers = p.xpathNodeList[Element]("/EnumerationResults/Containers/Container", a, e)

      val testContainerNames = List("testapicontainer", "testapicontainer2", "testapicontainer3")
      val testContainersA = containers._1.filter( el =>
        testContainerNames.exists(x => el.getElementsByTagName("Name").item(0).getTextContent() == x)
      )
      val testContainersE = containers._2.filter( el =>
        testContainerNames.exists(x => el.getElementsByTagName("Name").item(0).getTextContent() == x)
      )

      List(new TestResult("$body, num containers", new PrecomputedComparer(testContainersA.length == 3 && testContainersE.length == 3), testContainersA.length.toString, testContainersE.length.toString))
    }))
    CorrectnessTestBase.addResults("List Containers", results)
  }

  @Test def Test01_CreateContainer(): Unit = {

    val url = generateAzureUrl("?restype=container")
    val proxyUrl = generateProxyUrl(url)
    val tester = new AzureRequestTester("PUT", url, proxyUrl)
    tester.create()
    tester.addHeaders("Content-Length", 0.toString)
    tester.execute()

    val results = tester.compare(headerComparers, new EmptyBodyComparer())
    CorrectnessTestBase.addResults("Create Container", results)
  }

  @Test def Test99_DeleteContainer(): Unit = {
    val url = generateAzureUrl("?restype=container")
    val proxyUrl = generateProxyUrl(url)

    val tester = new AzureRequestTester("DELETE", url, proxyUrl)
    tester.create()
    tester.addHeaders("Content-Length", 0.toString)
    tester.execute()


    val results = tester.compare(headerComparers, new EmptyBodyComparer())
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

    val results = tester.compare(headerComparers, new EmptyBodyComparer())
    CorrectnessTestBase.addResults("Add Blob", results)
  }


  @Test def Test03_GetBlob(): Unit = {
    val url = generateAzureUrl("/blob.xml")
    val proxyUrl = generateProxyUrl(url)
    val tester = new AzureRequestTester("GET", url, proxyUrl)
    tester.create()
    tester.execute()

    val results = tester.compare(headerComparers, new RawBodyComparer())
    CorrectnessTestBase.addResults("Get Blob", results)
  }

  @Test def Test04_DeleteBlob(): Unit = {
    val url = generateAzureUrl("/blob.xml")
    val proxyUrl = generateProxyUrl(url)
    val tester = new AzureRequestTester("DELETE", url, proxyUrl)
    tester.create()
    tester.execute()

    val results = tester.compare(headerComparers, new EmptyBodyComparer())
    CorrectnessTestBase.addResults("Delete Blob", results)
  }
}
