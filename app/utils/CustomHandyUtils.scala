package utils

object CustomHandyUtils {

  def toDefiniteDouble(value: String): Double = {
    try {
      value.toDouble
    } catch {
      case e:Exception=>0
    }
  }
}