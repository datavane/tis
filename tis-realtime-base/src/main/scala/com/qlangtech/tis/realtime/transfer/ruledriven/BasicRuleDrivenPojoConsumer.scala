package com.qlangtech.tis.realtime.transfer.ruledriven

import java.util.Date
import java.util.concurrent.Callable

import com.qlangtech.tis.realtime.transfer._
import com.qlangtech.tis.realtime.transfer.impl.CompositePK
import com.qlangtech.tis.solrj.extend.TisCloudSolrClient
import com.qlangtech.tis.wangjubao.jingwei.AliasList
import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.response.QueryResponse
import org.apache.solr.common.{SolrDocument, SolrDocumentList}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.Map
import scala.collection.mutable.Set

//remove if not needed
//import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

object BasicRuleDrivenPojoConsumer {

  private val log: Logger =
    LoggerFactory.getLogger(classOf[BasicRuleDrivenPojoConsumer])

  // 6秒时间
  val Time_Window_15000: Long = 6000

  def realtimeGetNest(collection: String,
                      entityid: String,
                      id: String,
                      solrClient: TisCloudSolrClient): SolrDocument = {
    var doc: SolrDocument = null
    val query: SolrQuery = new SolrQuery()
    query.setParam("nestgetWithRootId", true)
    query.setParam("id", id)
    query.setQuery("id:" + id)
    val r: QueryResponse = solrClient.query(collection, entityid, query)
    val doclist: SolrDocumentList = r.getResults

    for (solrDoc <- doclist.asScala) {
      doc = solrDoc
      // break
    }
    doc
  }

}


abstract class BasicRuleDrivenPojoConsumer(
                                            onsListener: BasicRMListener,
                                            private val tabColumnMetaMap: Map[String, AliasList])
  extends BasicPojoConsumer(onsListener) {

  //
  protected override def getTimeWindow(): Long = BasicRuleDrivenPojoConsumer.Time_Window_15000

  override def indexIsExist(pojo: IPojo,
                            addDoc: TisSolrInputDocument): Boolean = {
    val pk: CompositePK = pojo.getPK.asInstanceOf[CompositePK]
    var colsMeta: AliasList = null;
    for ((k: String, v: AliasList) <- tabColumnMetaMap) {
      if (v.isPrimaryTable) {
        colsMeta = v;
      }
    }
    //val colsMeta: AliasList = tabColumnMetaMap.apply(pojo.getPrimaryTableName)
    if (colsMeta == null) {
      throw new IllegalStateException(
        "primary table is not in tabColumnMetaMap")
    }
    val primaryTabRow: IRowPack = pojo.getRowPack(colsMeta.getTableName)
    if (primaryTabRow == null) {
      for (getter <- colsMeta.getRowsFromOuterPersistence(null, null, pk).asScala) {
        colsMeta.copy2TisDocument(getter, addDoc, true)
        return true
      }
    }

    false
  }

  protected override def rectifiedVersion(pk: IPk,
                                          preVersion: Long,
                                          version: Long): Long = {
    var nowVer: Date = null
    nowVer =
      if (version < 1) new Date()
      else
        BasicRMListener.formatYyyyMMddHHmmss.get
          .parse(String.valueOf(version))
    val rectified: Long = java.lang.Long.parseLong(
      BasicRMListener.formatYyyyMMddHHmmss.get
        .format(new Date(nowVer.getTime + 1800 * 1000)))
    // 如果版本时间相差一个小时以内，如果相差太大了，也沒有辦法挽救了
    if (rectified > preVersion) {
      return (preVersion + 1)
    }
    version
  }

  /**
   * @param pojo
   * @param addDoc
   * @throws Exception
   */
  protected override def processPojo(pojo: IPojo,
                                     addDoc: TisSolrInputDocument): Boolean = {
    var mediaResultKey: MediaResultKey = null
    var callable: Callable[Any] = null
    var pack: IRowPack = null
    for ((key, value) <- tabColumnMetaMap) {
      pack = pojo.getRowPack(key)
      if (pack != null) {
        val columnMeta: AliasList = value
        //pack.vistRow()
        //pack.vistAllRow((t) =>
        //TODO 现在相同的表接收到多条记录也只处理一次，使用visitRow，将来有变化再说
        pack.vistRow((t) => {
          if (columnMeta.isNestChildRow) {
            addDoc.mergeOrCreateChild(t, value)
          } else {
            value.copy2TisDocument(t, addDoc, false)
          }
          true
        }
        )

      }
    }
    // 最终处理中间值 ================================================
    //
    val mediaResultMap: Map[MediaResultKey, Callable[Any]] = // 将map不可变
    MediaResultKey.getThreadLocalMediaResultMap

    val keys = Set[String]();
    for ((key, value) <- mediaResultMap) {
      mediaResultKey = key
      callable = value
      if (key.isFinalResult && keys.add(key.getColKey)) {
        addDoc.setField(mediaResultKey.getColKey, callable.call())
      }
    }
    true
  }


}
