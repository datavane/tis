package com.qlangtech.tis.realtime.transfer.study

object Cleanser {
  def apply(x: String): Cleanser = {
    println("execute apply")
    new Cleanser(x)
  }

  def main(args: Array[String]): Unit = {
    val x = Cleanser("hello")
    println(x.s)
  }
}

class Cleanser {

  var s = "Cleanser"

  def this(x: String) = {
    this()
    s = x
  }
}

