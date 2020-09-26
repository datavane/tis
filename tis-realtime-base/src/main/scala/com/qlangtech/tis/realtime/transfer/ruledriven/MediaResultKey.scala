package com.qlangtech.tis.realtime.transfer.ruledriven

import java.io.{File, PrintStream}
import java.util.concurrent.Callable
import java.util.concurrent.atomic.AtomicInteger

import com.qlangtech.tis.realtime.transfer.IRowValueGetter
import org.apache.commons.io.FileUtils
import com.qlangtech.tis.realtime.transfer.ruledriven.AllThreadLocal.addThreadLocalVal
import com.qlangtech.tis.realtime.transfer.ruledriven.MediaResultKey._
import scala.beans.{BeanProperty, BooleanBeanProperty}
import scala.collection.mutable.Map

//remove if not needed
//import scala.collection.JavaConverters._

object MediaResultKey {


  // 最终迭代Map元素中需要将 order 大的元素先出来
  implicit object AOrding extends Ordering[MediaResultKey] {
    def compare(o1: MediaResultKey, o2: MediaResultKey) = o2.order - o1.order
  }

  def main(args: Array[String]): Unit = {

    putMediaResult("testKey3", () => 3)
    putMediaResult("testKey1", () => 0)
    putMediaResult("testKey1", () => 1)

    println(getMediaResult("testKey1", null).call())


    var testMap = scala.collection.mutable.Map[MediaResultKey, Int]()

    var key = MediaResultKey.getKey("test1", false)

    testMap.put(key.copy(), 1)
    key = MediaResultKey.getKey("test1", false)
    testMap.put(key.copy(), 2);
    println("size:" + testMap.size)
    println("key:" + key + ",val:" + testMap.get(key))
  }

  private val mediaResultKeyOrderThreadLocal: ThreadLocal[AtomicInteger] =
    addThreadLocalVal(true, () => new AtomicInteger())

  private val mediaResultThreadLocal: ThreadLocal[
    Map[MediaResultKey, Callable[Any]]] = addThreadLocalVal()

  private val mediaResultKeyThreadLocal: ThreadLocal[MediaResultKey] =
    addThreadLocalVal(false, () => null)


  def getKey(key: String, finalResult: Boolean): MediaResultKey = {
    var k: MediaResultKey = mediaResultKeyThreadLocal.get
    if (k == null) {
      k = new MediaResultKey(key, 0)
      mediaResultKeyThreadLocal.set(k)
    } else {
      k.colKey = key
    }
    k.finalResult = finalResult
    k
  }

  //def getKey(key: String): MediaResultKey = getKey(key, false)

  def getThreadLocalMediaResultMap(): Map[MediaResultKey, Callable[Any]] = {
    var mediaResultMap: Map[MediaResultKey, Callable[Any]] =
      mediaResultThreadLocal.get
    if (mediaResultMap == null) {
      mediaResultMap = scala.collection.mutable.Map[MediaResultKey, Callable[Any]]() // Maps.newTreeMap(mediaResultMapKeyComparator)
      mediaResultThreadLocal.set(mediaResultMap)
    }
    mediaResultMap
  }

  def putMediaResult(colKey: String, callable: Callable[Any]): Unit = {
    //println(s"putMediaResult.colKey:$colKey,val:${callable.call}--------------------------------------")
    putMediaResult(colKey, false, callable);
  }

  def putMediaResult(colKey: String, finalResult: Boolean, callable: Callable[Any]): Unit = {

    // println(s"llllllllllcolKey:$colKey finalResult:$finalResult ${callable.call()} =====================================");
    val key: MediaResultKey = MediaResultKey.getKey(colKey, finalResult)
    val localMediaMap: Map[MediaResultKey, Callable[Any]] = getThreadLocalMediaResultMap

    if ("is_enterprise_card_pay".equals(colKey)) {
      var output: PrintStream = null
      try {
        output = new PrintStream(FileUtils.openOutputStream(new File("/opt/misc/incrtest.log"), true), true)
        (new Exception(s"xxx==========finalResult:$finalResult======================================================")).printStackTrace(output)
      } finally {
        output.close();
      }


    }

    localMediaMap.get(key) match {
      case Some(v) => {
        if (finalResult) {
          // 有重复且是最终输出值，可以将之前的callable值替换掉
          // TODO 可以确认此处是否可以不执行call
          // v.call
          localMediaMap.update(key.copy(), callable)

        } else {
          // 直接抛异常了，禁止有相同的key在整个处理流程中
          throw new IllegalStateException(s"'${key}' has duplication val in entire process,please check it");
        }
      }
      case None => localMediaMap += (key.copy() -> callable)
    }

  }

  private val nullCallable = new Callable[Any] {
    override def call(): Any = null
  }

  def getMediaResult(colKey: String, row: IRowValueGetter): Callable[Any] = {

    val mediaResultMap: Map[MediaResultKey, Callable[Any]] =
      MediaResultKey.getThreadLocalMediaResultMap
    val key: MediaResultKey = MediaResultKey.getKey(colKey, false)

    val mediaResult: Option[Callable[Any]] = mediaResultMap.get(key)
    val r = mediaResult match {
      case Some(v) => v
      case None => nullCallable
    }
    // println(s"getMediaResult.colKey:$colKey,result:${r.call}")
    r
  }

}

class MediaResultKey(@BeanProperty var colKey: String,
                     @BeanProperty val order: Int) {

  // 是否是最终值
  @BooleanBeanProperty
  var finalResult: Boolean = _

  def copy(): MediaResultKey = {
    val k: MediaResultKey = new MediaResultKey(
      this.colKey,
      mediaResultKeyOrderThreadLocal.get.getAndIncrement)
    k.finalResult = this.finalResult;
    k
  }

  override def hashCode(): Int = (this.colKey + "_" + this.finalResult).hashCode

  override def toString: String = s"key:$colKey"

  override def equals(obj: Any): Boolean = {
    return this.hashCode() == obj.hashCode()
  }
}
