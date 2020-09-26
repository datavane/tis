package com.qlangtech.tis.realtime.transfer.study

object TestCout {
  def main(args: Array[String]): Unit = {
    val count = TestCout[Long](1);
    println(count.value)
  }
}

case class TestCout[@specialized T](value: T);
