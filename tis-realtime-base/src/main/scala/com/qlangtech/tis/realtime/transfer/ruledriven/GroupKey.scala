package com.qlangtech.tis.realtime.transfer.ruledriven
import com.qlangtech.tis.realtime.transfer.ruledriven.GroupKey._
import scala.collection.mutable.Map

object GroupKey {
  private var localKey = new ThreadLocal[GroupKey]() {

    override protected def initialValue(): GroupKey = {
      //val k: Array[Any] = null
      new GroupKey(null)
    }
  }

  /**
   * 创建聚合Key
   *
   * @param keys
   * @return
   */
  def createCacheKey(keys: String*): GroupKey = this.createCacheKeys(keys)

  private def createCacheKeys(keys: Seq[String]): GroupKey = localKey.get.setKeys(keys)


}

class GroupKey private(private var keys: Seq[String]) {

  private var keysMap = Map[String, String]()

  def main(args: Array[String]): Unit = {
    val gk = GroupKey.createCacheKey("1", "2", "3", "4");
    println(gk.getKeyVal("1"));
  }

  def getKeyVal(key: String): Object = {
    val result = this.keysMap.get(key)
    result match {
      case Some(v) => v
      case None => {
        val buffer = new StringBuilder
        for((key,v) <- keysMap){
          buffer.append(s"key:${key}->val:${v},")
        }
        throw new RuntimeException(s"key:${key} can not finde relevant val in 'keyMap',map detail:${buffer},keys:${keys.toString()}")
      }
    }
  }

  /**
   * 原有的key有N个，后续的聚合计算需要进一步聚合，那需要在原有的key上作一个mash操作
   *
   * @param keys
   * @return
   */
  def mask(keys: Seq[String]): GroupKey = {
    try {
      val newKeys: Array[String] = Array.ofDim[String](keys.length * 2)

      var i: Int = 0
      for (key <- keys) {
        newKeys(i) = key
        newKeys(i + 1) = this.getKeyVal(key).asInstanceOf[String]
        i += 2
      }
      createCacheKeys(newKeys.toSeq)
    } catch {
      case e: Any => {
        throw new IllegalArgumentException(keys.toString(), e);
      }
    }
  }


  override def clone(): GroupKey = {
    val key: GroupKey = new GroupKey(this.keys)
    key.keysMap.addAll(this.keysMap);
    key
  }

  def setKeys(keys: Seq[String]): GroupKey = {
    if (keys == null) {
      return this
    }
    if (keys.length % 2 != 0) {
      throw new IllegalArgumentException(
        "keys:" + keys.toString() + " length mush be even,length:" + keys.length)
    }

    this.keys = keys
    this.keysMap.clear();
    var i: Int = 0
    while (i < keys.length) {

      this.keysMap += (keys(i) -> keys(i + 1))

      // this.keysMap.put((keys(i)), keys(i + 1))
      i += 2
    }
    this
  }

  override def hashCode(): Int = {
    var resultHash: Int = 0
    for (i <- 0 until this.keys.length) {
      resultHash += this.keys(i).hashCode();
    }
    resultHash
  }

  override def equals(obj: Any): Boolean = {
    if (obj == null) false
    if (getClass != obj.getClass) false
    val other: GroupKey = obj.asInstanceOf[GroupKey]

    for (i <- 0 until this.keys.length) {
      if (!this.keys(i).equals(other.keys(i))) {
        return false
      }
    }
    true
  }

  override def toString(): String =
    "GroupKey{" + "keys=" + keys.toString() + '}'

}
