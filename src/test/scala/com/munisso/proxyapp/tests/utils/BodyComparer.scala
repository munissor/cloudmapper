package com.munisso.proxyapp.tests.utils

import java.io.InputStream

import org.apache.commons.io.IOUtils
import org.apache.http.HttpEntity
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.{XPathConstants, XPathFactory}

import org.apache.commons.io.input.BOMInputStream
import org.w3c.dom.{Document, Node, NodeList}

/**
  * Created by rmunisso on 17/07/2016.
  */
trait BodyComparer {
  def compare(actual: InputStream, expected: InputStream): List[TestResult]
}

class RawBodyComparer extends  BodyComparer {
  def compare(actual: InputStream, expected: InputStream): List[TestResult] = {
    List(new TestResult("$body(raw)", new PrecomputedComparer(IOUtils.contentEquals(actual, expected)), "", ""))
  }
}

class EmptyBodyComparer extends BodyComparer {
  def compare(actual: InputStream, expected: InputStream): List[TestResult] = {
    List(new TestResult("$body(empty)", new PrecomputedComparer(actual.read() < 0 && expected.read() < 0), "", ""))
  }
}

class XmlBodyComparer(comparer: (XmlBodyComparer, Document, Document) => List[TestResult]) extends BodyComparer {

  private class NodeListTraversable(val nodeList: NodeList) extends Traversable[Node] {
    def foreach[U](f: Node => U) = {
      for(i <- 0 until nodeList.getLength)
        f(nodeList.item(i))
    }
  }

  val builderFactory = DocumentBuilderFactory.newInstance()
  val xpathFactory = XPathFactory.newInstance()
  val xpath = xpathFactory.newXPath()

  def compare(actual: InputStream, expected: InputStream): List[TestResult] = {
    if(actual.available() > 0 && expected.available() > 0)
      comparer(this, getDocument(actual), getDocument(expected))
    else
      List(new TestResult("$body(xml)", new PrecomputedComparer(false), "", ""))
  }

  def xpathNodeList[U](expr: String, a:Document, b: Document): (List[U], List[U]) = {

    val e = xpath.compile(expr)

    val ra = e.evaluate(a, XPathConstants.NODESET).asInstanceOf[NodeList]
    val rb = e.evaluate(b, XPathConstants.NODESET).asInstanceOf[NodeList]

    (nodeListToList[U](ra), nodeListToList[U](rb))
  }

  private def nodeListToList[U](nodeList: NodeList): List[U] = {
    new NodeListTraversable(nodeList).map( x => x.asInstanceOf[U]).toList
  }

  private def getDocument(entity: InputStream): Document = {
    val builder = builderFactory.newDocumentBuilder()
    builder.parse(entity)
  }

}
