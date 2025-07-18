/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qlangtech.tis.manage.servlet;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qlangtech.tis.realtime.transfer.IIncreaseCounter;
import org.apache.commons.lang3.tuple.Pair;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2018年5月21日
 */
public class TopicTagIncrStatus {

  // public static final  String KEY_BINLOG_READ_OFFSET = "binlogOffset";
  // private static final String KEY_SOLR_CONSUME = "solrConsume";
  // public static final String KEY_TABLE_CONSUME_COUNT = "tableConsumeCount";
  public static final List<Pair<String, Function<TopicTagIncr, Integer>>> ALL_SUMMARY_KEYS_PAIR_LIST
    = Lists.newArrayList(
    //IIncreaseCounter.SOLR_CONSUME_COUNT,
    Pair.of(IIncreaseCounter.TABLE_CONSUME_COUNT, (tagIncr) -> tagIncr.getTrantransferIncr()),
    Pair.of(IIncreaseCounter.TABLE_INSERT_COUNT, (tagIncr) -> tagIncr.getBinlogIncr()),
    Pair.of(IIncreaseCounter.TABLE_UPDATE_COUNT, (tagIncr) -> tagIncr.getBinlogIncr()),
    Pair.of(IIncreaseCounter.TABLE_DELETE_COUNT, (tagIncr) -> tagIncr.getBinlogIncr()));

  public static final List<String> ALL_SUMMARY_KEYS = ALL_SUMMARY_KEYS_PAIR_LIST.stream().map((p) -> p.getKey()).collect(Collectors.toList());

  private final List<String> focusTags;

  private final LoadingCache<Long, TopicTagIncrSnapshotStatus> /* 秒的时间戳 */
    c = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.SECONDS).build(new CacheLoader<Long, TopicTagIncrSnapshotStatus>() {

    @Override
    public TopicTagIncrSnapshotStatus load(Long key) throws Exception {
      return createTopicTagIncrSnapshotStatus();
    }
  });

  private TopicTagIncrSnapshotStatus lastCreate;

  private TopicTagIncrSnapshotStatus createTopicTagIncrSnapshotStatus() {
    return this.lastCreate = new TopicTagIncrSnapshotStatus();
  }

  public TopicTagIncrStatus(Collection<FocusTags> focusTags) {
    super();

//    Function<String, Pair<String, Function<TopicTagIncr, Integer>>> cc = (tag) ->
//      Pair.of(tag, new Function<TopicTagIncr, Integer>() {
//        @Nullable
//        @Override
//        public Integer apply(TopicTagIncr tagInc) {
//          return tagInc.getTrantransferIncr();
//        }
//      });
    this.focusTags = //
      focusTags.stream().flatMap((t) -> t.getTags().stream()).map((tag) -> tag).collect(Collectors.toList());
  }

  public void add(Long timeSerialize, TopicTagIncr tagIncr) {
    try {
      TopicTagIncrSnapshotStatus incrStats = c.get(timeSerialize);
      if (incrStats == null) {
        throw new IllegalStateException("timeSerialize:" + timeSerialize + " relevant val can not be null");
      }
      incrStats.incrStatus.put(tagIncr.tag, tagIncr);
    } catch (ExecutionException e) {
      throw new IllegalStateException(e);
    }
  }

  /**
   * 取得 各个tag的平均值
   *
   * @return
   */
  public TisIncrStatus getAverageTopicTagIncr(boolean average, boolean test) {
    return new TisIncrStatus(process(ALL_SUMMARY_KEYS, average, test), process(this.focusTags, average, test));
  }

  public static class TisIncrStatus {

    private final Map<String, Integer> summary;

    private final List<TopicTagIncr> tags;

    TisIncrStatus(List<TopicTagIncr> summary, List<TopicTagIncr> tags) {
      this.summary = Maps.newHashMap();
      if (ALL_SUMMARY_KEYS_PAIR_LIST.size() != summary.size()) {
        throw new IllegalArgumentException(
          "ALL_SUMMARY_KEYS size:" + ALL_SUMMARY_KEYS_PAIR_LIST.size() + " must equal with:" + summary.size());
      }
      Pair<String, Function<TopicTagIncr, Integer>> p = null;
      TopicTagIncr tagIncr = null;
      for (int i = 0; i < summary.size(); i++) {
        p = ALL_SUMMARY_KEYS_PAIR_LIST.get(i);
        tagIncr = summary.get(i);
        this.summary.put(p.getKey(), p.getValue().apply(tagIncr));
      }
//      summary.forEach((r) -> {
//        this.summary.put(r.getTag(), r.getTrantransferIncr());
//      });
      this.tags = tags;
    }

    public Map<String, Integer> getSummary() {
      return this.summary;
    }

    public List<TopicTagIncr> getTags() {
      return this.tags;
    }
  }

  protected List<TopicTagIncr> process(List<String> focusTags, boolean average, boolean test) {
    List<TopicTagIncr> result = new ArrayList<>();
    Map<Long, TopicTagIncrSnapshotStatus> /* 秒的时间戳 */
      timeRangeMap = c.asMap();
    int rangeSize = timeRangeMap.size();
    if (rangeSize < 1) {
      return Collections.emptyList();
    }
    int binlogIncrSum = 0;
    int trantransferIncrSum = 0;
    TopicTagIncr lastTagIncr = null;
    TopicTagIncr tagIncrPair = null;
    for (String tag : focusTags) {
      binlogIncrSum = 0;
      trantransferIncrSum = 0;
      lastTagIncr = null;
      for (TopicTagIncrSnapshotStatus stat : timeRangeMap.values()) {
        tagIncrPair = stat.incrStatus.get(tag);
        if (tagIncrPair == null) {
          continue;
        }
        binlogIncrSum += tagIncrPair.binlogIncr;
        trantransferIncrSum += tagIncrPair.trantransferIncr;
        // System.out.println("hahah tab:" + tab + ",binlogIncrSum:" + binlogIncrSum);
      }
      if (lastCreate != null) {
        tagIncrPair = lastCreate.incrStatus.get(tag);
        if (tagIncrPair != null) {
          lastTagIncr = tagIncrPair;
        }
      }
      // String tag, int binlogIncr, long binlogIncrLastUpdate, int trantransferIncr
      result.add(new //
        TopicTagIncr(//
        tag, //
        // calculateTraffic(tag, average, rangeSize, binlogIncrSum, test), //
        /** binlogIncr，统计最终绝对值例如，增量程序启动后 update，delete，insert 各操作的总数统计 */
        lastTagIncr != null ? lastTagIncr.binlogIncr : 0,
        lastTagIncr != null ? lastTagIncr.binlogIncrLastUpdate : 0
        /** trantransferIncr */
        , calculateTraffic(tag, average, rangeSize, trantransferIncrSum, test)));
    }
    return result;
  }

//  public Collection<String> getFocusTags() {
//    return this.focusTags;
//  }

  /**
   * 计算流量
   *
   * @param average
   * @param rangeSize
   * @param binlogIncrSum
   * @param test
   * @return
   */
  private int calculateTraffic(String tag, boolean average, int rangeSize, int binlogIncrSum, boolean test) {
    if (test) {
      return (int) (Math.random() * 100);
    }
    rangeSize = rangeSize < 2 ? 1 : (rangeSize - 1);
    int result = binlogIncrSum / (average ? rangeSize : 1);
    // System.out.println("tag:" + tag + ",binlogIncrSum:" + binlogIncrSum + ", result:" + result + "rangeSize:" + rangeSize);
    return result;
  }

  private static final class TopicTagIncrSnapshotStatus {

    private Map<String, TopicTagIncr> incrStatus = Maps.newHashMap();
  }

  public static class TopicTagIncr {

    private static final ThreadLocal<SimpleDateFormat> format = new ThreadLocal<SimpleDateFormat>() {

      @Override
      protected SimpleDateFormat initialValue() {
        return new SimpleDateFormat("HH:mm:ss");
      }
    };

    private final String tag;

    private final int binlogIncr;

    private final long binlogIncrLastUpdate;

    private final int trantransferIncr;

    public String getTag() {
      return tag;
    }

    public int getBinlogIncr() {
      return binlogIncr;
    }

    public String getLastUpdate() {
      return format.get().format(new Date(this.binlogIncrLastUpdate));
    }

    public int getTrantransferIncr() {
      return trantransferIncr;
    }

    public static TopicTagIncr create(
      String tag
      , Map<String, /* this.tag */TopicTagStatus> transfer) {
      long accumulateCount = 0;
      long binlogIncrLastUpdate = 0;
      long trantransferIncr = 0;
      //TopicTagStatus binlogTagStat = binlog.get(tag);
//      if (binlogTagStat != null) {
//        binlogIncr = binlogTagStat.getIncr();
//        binlogIncrLastUpdate = binlogTagStat.getLastUpdateTime();
//      }
      TopicTagStatus transferTagStat = transfer.get(tag);
      if (transferTagStat != null) {
        binlogIncrLastUpdate = transferTagStat.getLastUpdateTime();
        trantransferIncr = transferTagStat.getIncr();
        accumulateCount = transferTagStat.getCount();
      }
      return new TopicTagIncr(tag, (int) accumulateCount, binlogIncrLastUpdate, (int) trantransferIncr);
    }

    public TopicTagIncr(String tag, int binlogIncr, long binlogIncrLastUpdate, int trantransferIncr) {
      super();
      this.tag = tag;
      this.binlogIncr = binlogIncr;
      this.binlogIncrLastUpdate = binlogIncrLastUpdate;
      this.trantransferIncr = trantransferIncr;
    }
  }

  public static class FocusTags {

    private static final Joiner joiner = Joiner.on(",").skipNulls();

    private final String topic;

    private Collection<String> tags = new ArrayList<String>();

    public FocusTags(String topic, Collection<String> tags) {
      if (tags.size() < 1) {
        throw new IllegalArgumentException("topic:" + tags + " relevant tags can not be empty");
      }
      this.topic = topic;
      this.tags = tags;
    }

    public Collection<String> getTags() {
      return tags;
    }

    public void setTags(List<String> tags) {
      this.tags = tags;
    }

    public String getTopic() {
      return topic;
    }

    @Override
    public String toString() {
      return this.createParams();
    }

    private String createParams() {
      List<String> result = Lists.asList("topic=" + topic, tags.toArray(new String[]{}));
      return joiner.join(result);
    }
  }
}
