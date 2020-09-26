package com.qlangtech.tis.realtime.transfer.study

import java.io.Closeable

import scala.io.Source
//import scala.language.reflectiveCalls
import scala.util.control.NonFatal

object manage {

  def apply[R <: Closeable, T](resource: => R)(f: R => T) = {
    var res: Option[R] = None
    try {
     // println(resource.getClass)
      res = Some(resource)
      f(res.get)
    } catch {
      case NonFatal(ex) => {
        println(s"Non fatal exception! $ex")
        throw ex;
      }
    } finally {
      for (r <- res) {
        r.close
      }
    }
  }
}

object TryCatchARM {
  def main(args: Array[String]): Unit = {
    args foreach (arg => countLines(arg))
  }

  def countLines(filename: String) = {
    manage(Source.fromFile(filename)) {
      resource =>
        val size = resource.getLines.size
        println(s"file $filename has $size lines")
        if (size > 20) {
          throw new RuntimeException("Big file!")
        }
    }
  }
}
