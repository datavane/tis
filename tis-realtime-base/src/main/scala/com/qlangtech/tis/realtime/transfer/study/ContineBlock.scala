package com.qlangtech.tis.realtime.transfer.study

import java.util.concurrent.Callable

import com.qlangtech.tis.realtime.transfer.ruledriven.FunctionUtils.{Case, caseIfFunc}

object contine {

  def apply(conditional: => Boolean)(body: => Unit): Unit = {
    if (conditional) {
      body
      apply(conditional)(body)
    }
  }
}

object TestObj {

  protected implicit def convert2CallableBoolean(data: Boolean): Callable[java.lang.Boolean] = {
    () => data
  }

  def main(args: Array[String]): Unit = {
//    var count = 0;
//    contine(count < 5) {
//      println(count)
//      count += 1
//    }

    println("ddd")
    var tmpSum = 1
    println( caseIfFunc(0,
      new Case((tmpSum>0),1)))
  }
}
