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
package com.qlangtech.tis.collectinfo;

import java.io.InputStream;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.impl.XMLResponseParser;
import org.apache.solr.common.cloud.Replica;
import org.apache.solr.common.cloud.Slice;
import org.apache.solr.common.cloud.SolrZkClient;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.qlangtech.tis.collectinfo.ReplicaStatisCount.ReplicaNode;
import com.qlangtech.tis.collectinfo.api.ICoreStatistics;
import com.qlangtech.tis.manage.common.ConfigFileContext;
import com.qlangtech.tis.manage.common.ConfigFileContext.StreamProcess;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class CoreStatisticsReport implements ICoreStatistics {

	protected final SolrZkClient zookeeper;

	private int groupCount = 0;

	private final String appName;

	// 应用机器数目
	private Set<String> hosts;

	private final Map<Integer, List<String>> groupServers = new HashMap<Integer, List<String>>();

	@Override
	public List<String> getReplicIps(Integer groupIndex) {
		return this.groupServers.get(groupIndex);
	}

	private static final Logger log = LoggerFactory.getLogger(CoreStatisticsReport.class);

	@Override
	public String getAppName() {
		return appName;
	}

	// public CoreStatisticsReport(int groupCount, String appName,
	// ZooKeeper zookeeper) {
	// this.groupCount = groupCount;
	// this.appName = appName;
	// this.zookeeper = zookeeper;
	// }
	@Override
	public int getHostsCount() {
		if (hosts == null) {
			throw new IllegalStateException("hosts can not be null");
		}
		return this.hosts.size();
	}

	@Override
	public Set<String> getHosts() {
		return this.hosts;
	}

	public CoreStatisticsReport(String collectionName, Slice slice, SolrZkClient zookeeper) {
		this.groupCount++;
		this.zookeeper = zookeeper;
		hosts = new HashSet<String>();
		appName = collectionName;
		addClusterCoreInfo(slice);
	}

	/**
	 * 添加一个组的统计信息
	 *
	 * @param slice
	 */
	public void addClusterCoreInfo(Slice slice) {
		this.groupCount++;
		int groupIndex = Integer.parseInt(StringUtils.substringAfter(slice.getName(), "shard")) - 1;
		List<String> replicaIps = new ArrayList<String>(slice.getReplicas().size());
		groupServers.put(groupIndex, replicaIps);
		for (Replica replic : slice.getReplicas()) {
			String host = replic.getNodeName();
			hosts.add(host);
			replicaIps.add(host);
			try {
				getCoreStatus(new ReplicaNode(groupIndex, host), replic);
			} catch (Exception e) {
			}
		}
	}

	public static final XMLResponseParser RESPONSE_PARSER = new XMLResponseParser();

	private void getCoreStatus(final ReplicaNode replicaNode, final org.apache.solr.common.cloud.Replica replica)
			throws Exception {
		// 查看filtercache 的命中率
		// /admin/mbeans?stats=true&cat=CACHE&key=filterCache
		URL url = new URL(replica.getCoreUrl()
				+ "admin/mbeans?stats=true&cat=QUERYHANDLER&cat=CORE&key=/select&key=/update&key=searcher");
		// 服务端处理类：SolrInfoMBeanHandler
		// http://10.1.5.37:8080/solr/search4totalpay_shard1_replica1/admin/mbeans?stats=true&cat=QUERYHANDLER&cat=CORE&key=/select&key=/update&key=searcher
		// http://120.55.195.132:8080/solr/search4totalpay_shard1_replica2/admin/mbeans?stats=true&cat=QUERYHANDLER&cat=CORE&key=/select&key=/update&key=searcher&key=core
		ConfigFileContext.processContent(url, new StreamProcess<Object>() {

			@Override
			@SuppressWarnings("all")
			public Object p(int status, InputStream stream, String md5) {
				SimpleOrderedMap result = (SimpleOrderedMap) RESPONSE_PARSER.processResponse(stream, "utf8");
				SimpleOrderedMap mbeans = (SimpleOrderedMap) result.get("solr-mbeans");
				final SimpleOrderedMap queryHandler = (SimpleOrderedMap) mbeans.get("QUERYHANDLER");
				SimpleOrderedMap stats = null;
				Long requestCount = null;
				if (replica.getStr(Slice.LEADER) != null) {
					// leader节点才记录访问量
					SimpleOrderedMap update = (SimpleOrderedMap) queryHandler.get("/update");
					stats = (SimpleOrderedMap) update.get("stats");
					requestCount = (Long) stats.get("requests");
					updateCount.add(replicaNode, requestCount);
					CoreStatisticsReport.this.updateErrorCount.add(replicaNode, ((Long) stats.get("errors")));
					SimpleOrderedMap core = (SimpleOrderedMap) mbeans.get("CORE");
					SimpleOrderedMap searcher = (SimpleOrderedMap) core.get("searcher");
					stats = (SimpleOrderedMap) searcher.get("stats");
					// 文档总数
					numDocs.addAndGet(((Integer) stats.get("numDocs")).longValue());
				}
				SimpleOrderedMap select = (SimpleOrderedMap) queryHandler.get("/select");
				stats = (SimpleOrderedMap) select.get("stats");
				requestCount = (Long) stats.get("requests");
				CoreStatisticsReport.this.requestCount.add(replicaNode, requestCount);
				CoreStatisticsReport.this.requestErrorCount.add(replicaNode, ((Long) stats.get("errors")));
				return null;
			}
		});
	}

	/**
	 * 查询错误增量
	 *
	 * @param newReport
	 * @return
	 */
	public long getQueryErrorCountIncreasement(CoreStatisticsReport newReport) {
		return this.requestErrorCount.getIncreasement(newReport.requestErrorCount);
	}

	/**
	 * 更新错误的增量
	 *
	 * @param newReport
	 * @return
	 */
	public long getUpdateErrorCountIncreasement(CoreStatisticsReport newReport) {
		return this.updateErrorCount.getIncreasement(newReport.updateErrorCount);
	}

	/**
	 * 更新增量
	 *
	 * @param newReport
	 * @return
	 */
	public long getUpdateCountIncreasement(CoreStatisticsReport newReport) {
		return this.updateCount.getIncreasement(newReport.updateCount);
	}

	/**
	 * 取得前后两次取样之间的增量值
	 *
	 * @param newReport
	 * @return
	 */
	public long getRequestIncreasement(CoreStatisticsReport newReport) {
		return this.requestCount.getIncreasement(newReport.requestCount);
		// long result = 0;
		// AtomicLong preReplicValue = null;
		// long increase = 0;
		// for (Map.Entry<ReplicaNode, AtomicLong> entry :
		// newReport.requestCount
		// .entrySet()) {
		// preReplicValue = this.requestCount.get(entry.getKey());
		// if (preReplicValue == null
		// || (increase = (entry.getValue().get() - preReplicValue
		// .get())) < 0) {
		// result += entry.getValue().get();
		// } else {
		// result += increase;
		// }
		// }
		//
		// return result;
	}

	@Override
	public int getGroupCount() {
		return groupCount;
	}

	@Override
	public long getRequests() {
		return this.requests;
	}

	@Override
	public String getFormatRequests() {
		return NumberFormat.getIntegerInstance().format(getRequests());
	}

	private long requests;

	// 更新量
	private long update;

	// 访问量请求统计
	private final ReplicaStatisCount requestCount = new ReplicaStatisCount();

	// 更新量请求统计
	private final ReplicaStatisCount updateCount = new ReplicaStatisCount();

	private final ReplicaStatisCount updateErrorCount = new ReplicaStatisCount();

	private final ReplicaStatisCount requestErrorCount = new ReplicaStatisCount();

	@Override
	public List<String> getAllServers() {
		List<String> servers = new ArrayList<String>();
		for (ReplicaNode replic : requestCount.keySet()) {
			servers.add(replic.getHost());
		}
		return servers;
	}

	private final AtomicLong numDocs = new AtomicLong();

	@Override
	public long getNumDocs() {
		return numDocs.longValue();
	}
}
