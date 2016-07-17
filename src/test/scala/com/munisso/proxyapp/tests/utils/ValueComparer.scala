package com.munisso.proxyapp.tests.utils

import org.joda.time.Seconds
import org.joda.time.format.DateTimeFormat

/**
  * Created by rmunisso on 17/07/2016.
  */
trait ValueComparer {
  def compare(actual: String, expected: String): Boolean
}


class DefaultComparer extends ValueComparer {
  override def compare(actual: String, expected: String): Boolean = actual == expected
}

class PrecomputedComparer(result: Boolean) extends ValueComparer {
  override def compare(actual: String, expected: String): Boolean = result
}

// Checks that both values are not empty, to be used for client opaque values
class ExistsComparer extends ValueComparer {
  override def compare(actual: String, expected: String): Boolean = actual != null && actual.length > 0 && expected != null && expected.length > 0
}

// Compares that the difference between two dates is less than allowedSeconds
class DateComparer(format: String, allowedSeconds: Int) extends ValueComparer {

  override def compare(actual: String, expected: String): Boolean = {
    try {
      val formatter = DateTimeFormat.forPattern(format)
      val actualDate = formatter.parseDateTime(actual)
      val expectedDate = formatter.parseDateTime(expected)

      Seconds.secondsBetween(actualDate, expectedDate).isLessThan(Seconds.seconds(allowedSeconds))
    }
    catch {
      case e: Exception => false
    }
  }
}