/**
 *
 */
package com.qlangtech.tis.realtime.transfer.ruledriven

import java.util
import java.util.concurrent.Callable

import com.google.common.collect.Maps
import com.google.common.util.concurrent.RateLimiter
import com.qlangtech.tis.realtime.transfer.BasicRMListener
import com.qlangtech.tis.realtime.transfer.impl.DefaultPojo
import com.qlangtech.tis.sql.parser.er.JoinerKey
import com.qlangtech.tis.wangjubao.jingwei.Alias.Builder
import com.qlangtech.tis.wangjubao.jingwei.Alias.Builder.alias
import com.qlangtech.tis.wangjubao.jingwei.AliasList

import scala.collection.Map

object BasicRuleDrivenListener {
  val NUM_CONSUME_NUM_5: Int = 1
}

/**
 * 基于规则驱动的消費MQ終端
 *

 */
abstract class BasicRuleDrivenListener[T] extends BasicRMListener[T]() {

  /**
   *  子类中在设置 <br>
   *  empNo：string <br>
   *  andEmpNoEqualTo 需要的是int <br>
   *  deptEmpCriteria.createCriteria().andEmpNoEqualTo(empNo) <br>
   * @param data
   * @return
   */
  protected implicit def convert2Integer(data: String): Integer = {
    data.toInt
  }

  protected implicit def convert2Double(data: Any): Double = {

    // if(v.isInstanceOf[Double]){
    //   return v.asInstanceOf[Double]
    // }

    // if(v.isInstanceOf[Integer]){
    //   return v.asInstanceOf[Integer].toDouble
    // }

    // throw new IllegalArgumentException(v.toString)

    data match {
      case data: Double => data.asInstanceOf[Double]
      case data: Integer => data.asInstanceOf[Integer].toDouble
      case data: String => data.asInstanceOf[String].toDouble
      case null => 0
      case _ => throw new IllegalArgumentException(s"data:${data.toString},class:${data.getClass}")
    }
  }


  protected implicit def convert2Callable(data: Any): Callable[Any] = {
    () => data
  }

  protected implicit def convert2CallableBoolean(data: Boolean): Callable[java.lang.Boolean] = {
    () => data
  }

  // 给getMediaResult 取得结果用
  protected implicit def callable2Val(call: Callable[Any]): Double = {
    val r = call.call
    //    println(s"xxxxxxxxxxxxxxxxxxxxxxxxxcall:$r")

    convert2Double(r)
  }

  protected implicit def tuple2Builder(p: Tuple2[String, String]): Builder = {
    alias(p._1, p._2)
  }

  protected implicit def tuple2JoinerKey(p: Tuple2[String /*parent key*/ , String /*child key*/ ]): JoinerKey = {
    // alias(p._1, p._2)
    new JoinerKey(p._1, p._2);
  }


  protected implicit def string2Builder(v: String): Builder = {
    alias(v)
  }

  protected override def createProcessRate(): RateLimiter =
    RateLimiter.create(600)

  protected override def createRowsWrapper(): DefaultPojo =
    new BasicRuleDrivenWrapper(this)

  protected override def getConsumeNum(): Int = BasicRuleDrivenListener.NUM_CONSUME_NUM_5

  /**
   * 取得索引主表
   *
   * @return
   */
  override protected def createPrimaryTables(): util.Map[String, AliasList] = {
    // FIXME 需要写具体实现
    Maps.newHashMap()
  }

  /**
   * 将数据进行一次归并操作
   *
   * @param mediaResult
   * @param
   * 重新分組使用key的排位
   * @return
   */
  protected def reduceData(mediaResult: Map[GroupKey, GroupValues],
                           reduceKey: String*): scala.collection.mutable.Map[GroupKey, GroupValues] = {
    var groupKey: GroupKey = null
    var v: Option[GroupValues] = null
    val result = scala.collection.mutable.Map[GroupKey, GroupValues]()

    for ((key, value) <- mediaResult) {
      groupKey = key.mask(reduceKey)
      v = result.get(groupKey)
      if (v.isEmpty) {
        result += (groupKey.clone() -> new GroupValues(value.data))
      } else {
        v.get.addVal(value.data)
      }
    }
    result
  }

}
