package com.munisso

import java.io.PrintWriter

/**
  * Created by rmunisso on 26/04/2016.
  */
class IndentedPrintWriter(writer: PrintWriter) {

  private var indentLevel: Int = 0;
  private var indentString: String = "";
  private val indentChar: String = "\t";

  def increaseIndent(): Unit = {
    this.indentLevel += 1;
    this.indentString = this.buildIndentString();
  }

  def decreaseIndent(): Unit = {
    if(this.indentLevel == 0){
      throw new IllegalStateException();
    }

    this.indentLevel -= 1;
    this.indentString = this.buildIndentString();
  }

  private def buildIndentString(): String = {
    val builder = new StringBuilder()
    for(x <- 1 to this.indentLevel ) {
      builder.append (this.indentChar)
    }
    builder.toString()
  }

  def printLn(s: String): Unit = {
    this.writer.print(this.indentString);
    this.writer.println(s);
  }

  def printLn(s: String, args: Any*): Unit = {
    this.printLn(String.format(s, args));
  }
}
