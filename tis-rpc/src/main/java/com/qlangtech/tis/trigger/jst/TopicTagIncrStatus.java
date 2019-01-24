/* 
 * The MIT License
 *
 * Copyright (c) 2018-2022, qinglangtech Ltd
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.qlangtech.tis.trigger.jst;

import static java.util.stream.Collectors.toSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import com.qlangtech.tis.trigger.jst.LogCollectorClientManager.FocusTags;
import com.qlangtech.tis.trigger.jst.LogCollectorClientManager.TisIncrStatus;
import com.qlangtech.tis.trigger.jst.LogCollectorClientManager.TopicTagStatus;

/* 
 * 增量任务执行MQ中TOPIC下个各个TAG流量统计建模
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TopicTagIncrStatus {

	private final Set<String> focusTags;

	private final LoadingCache<Long, TopicTagIncrSnapshotStatus> /* 秒的时间戳 */
	c = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.SECONDS)
			.build(new CacheLoader<Long, TopicTagIncrSnapshotStatus>() {

				@Override
				public TopicTagIncrSnapshotStatus load(Long key) throws Exception {
					return createTopicTagIncrSnapshotStatus();
				}
			});

	private TopicTagIncrSnapshotStatus lastCreate;

	private TopicTagIncrSnapshotStatus createTopicTagIncrSnapshotStatus() {
		return this.lastCreate = new TopicTagIncrSnapshotStatus();
	}

	public TopicTagIncrStatus(List<FocusTags> focusTags) {
		super();
		this.focusTags = //
				focusTags.stream().flatMap((t) -> t.getTags().stream()).collect(toSet());
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
		return new TisIncrStatus(process(LogCollectorClientManager.ALL_SUMMARY_KEYS, average, test),
				process(this.focusTags, average, test));
	}

	protected List<TopicTagIncr> process(Set<String> focusTags, boolean average, boolean test) {
		List<TopicTagIncr> result = new ArrayList<>();
		Map<Long, TopicTagIncrSnapshotStatus> /* 秒的时间戳 */
		timeRangeMap = c.asMap();
		int rangeSize = timeRangeMap.size();
		if (rangeSize < 1) {
			return Collections.emptyList();
		}
		int binlogIncrSum = 0;
		int trantransferIncrSum = 0;
		long binlogIncrLastUpdate = 0;
		TopicTagIncr tagIncrPair = null;
		for (String tab : focusTags) {
			binlogIncrSum = 0;
			trantransferIncrSum = 0;
			binlogIncrLastUpdate = 0;
			for (TopicTagIncrSnapshotStatus stat : timeRangeMap.values()) {
				tagIncrPair = stat.incrStatus.get(tab);
				binlogIncrSum += tagIncrPair.binlogIncr;
				trantransferIncrSum += tagIncrPair.trantransferIncr;
			}
			if (lastCreate != null) {
				tagIncrPair = lastCreate.incrStatus.get(tab);
				if (tagIncrPair != null) {
					binlogIncrLastUpdate = tagIncrPair.binlogIncrLastUpdate;
				}
			}
			// String tag, int binlogIncr, long binlogIncrLastUpdate, int
			// trantransferIncr
			result.add(new TopicTagIncr(tab, calculateTraffic(average, rangeSize, binlogIncrSum, test),
					binlogIncrLastUpdate, calculateTraffic(average, rangeSize, trantransferIncrSum, test)));
		}
		return result;
	}

	public Set<String> getFocusTags() {
		return this.focusTags;
	}

	/**
	 * 计算流量
	 *
	 * @param average
	 * @param rangeSize
	 * @param binlogIncrSum
	 * @param test
	 * @return
	 */
	private int calculateTraffic(boolean average, int rangeSize, int binlogIncrSum, boolean test) {
		if (test) {
			return (int) (Math.random() * 100);
		}
		return binlogIncrSum / (average ? rangeSize : 1);
	}

	private static final class TopicTagIncrSnapshotStatus {

		private Map<String, TopicTagIncr> incrStatus = Maps.newHashMap();
	}

	public static class TopicTagIncr {

		private static final ThreadLocal<SimpleDateFormat> format = new ThreadLocal<SimpleDateFormat>() {

			@Override
			protected SimpleDateFormat initialValue() {
				return new SimpleDateFormat("MM/dd HH:mm:ss");
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

		// public long getBinlogIncrLastUpdate() {
		// return binlogIncrLastUpdate;
		// }
		public String getLastUpdate() {
			return format.get().format(new Date(this.binlogIncrLastUpdate));
		}

		public int getTrantransferIncr() {
			return trantransferIncr;
		}

		public static TopicTagIncr create(String tag,
				Map<String, /* this.tag */
						TopicTagStatus> binlog,
				Map<String, /* this.tag */
						TopicTagStatus> transfer) {
			long binlogIncr = 0;
			long binlogIncrLastUpdate = 0;
			long trantransferIncr = 0;
			TopicTagStatus binlogTagStat = binlog.get(tag);
			if (binlogTagStat != null) {
				binlogIncr = binlogTagStat.getIncr();
				binlogIncrLastUpdate = binlogTagStat.getLastUpdateTime();
			}
			TopicTagStatus transferTagStat = transfer.get(tag);
			if (transferTagStat != null) {
				trantransferIncr = transferTagStat.getIncr();
			}
			return new TopicTagIncr(tag, (int) binlogIncr, binlogIncrLastUpdate, (int) trantransferIncr);
		}

		public TopicTagIncr(String tag, int binlogIncr, long binlogIncrLastUpdate, int trantransferIncr) {
			super();
			this.tag = tag;
			this.binlogIncr = binlogIncr;
			this.trantransferIncr = trantransferIncr;
			this.binlogIncrLastUpdate = binlogIncrLastUpdate;
		}
	}
}
