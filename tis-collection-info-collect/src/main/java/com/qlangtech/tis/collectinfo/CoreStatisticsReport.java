/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.collectinfo;

import com.qlangtech.tis.collectinfo.ReplicaStatisCount.ReplicaNode;
import com.qlangtech.tis.collectinfo.api.ICoreStatistics;
import com.qlangtech.tis.manage.common.ConfigFileContext;
import com.qlangtech.tis.manage.common.ConfigFileContext.StreamProcess;
import com.qlangtech.tis.manage.common.TisUTF8;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.impl.XMLResponseParser;
import org.apache.solr.common.cloud.Replica;
import org.apache.solr.common.cloud.Slice;
import org.apache.solr.common.cloud.SolrZkClient;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URL;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2013-10-17
 */
public class CoreStatisticsReport implements ICoreStatistics {
    static final String GET_METRIX_PATH = "admin/mbeans?stats=true&cat=QUERY&cat=CORE&cat=UPDATE&key=/select&key=/update&key=searcher&wt=xml";
    protected final SolrZkClient zookeeper;
    // 访问量请求统计
    final ReplicaStatisCount requestCount = new ReplicaStatisCount();
     final AtomicLong numDocs = new AtomicLong();

    // 更新量请求统计
    final ReplicaStatisCount updateCount = new ReplicaStatisCount();

    final ReplicaStatisCount updateErrorCount = new ReplicaStatisCount();

    final ReplicaStatisCount requestErrorCount = new ReplicaStatisCount();
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

    public CoreStatisticsReport(String collectionName, SolrZkClient zookeeper) {
        this.groupCount++;
        this.zookeeper = zookeeper;
        hosts = new HashSet<String>();
        appName = collectionName;
        //  addClusterCoreInfo(slice);
    }

    /**
     * 添加一个组的统计信息
     *
     * @param slice
     */
    public boolean addClusterCoreInfo(Slice slice) {
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
                // e.printStackTrace();
                return false;
            }
        }

        return true;
    }

    public static final XMLResponseParser RESPONSE_PARSER = new XMLResponseParser();


    private void getCoreStatus(final ReplicaNode replicaNode, final org.apache.solr.common.cloud.Replica replica) throws Exception {
        // 查看filtercache 的命中率
        // /admin/mbeans?stats=true&cat=CACHE&key=filterCache
        URL url = new URL(replica.getCoreUrl() + GET_METRIX_PATH);
        // 服务端处理类：SolrInfoMBeanHandler
        // http://192.168.28.200:8080/solr/search4totalpay4_shard1_replica_n1/admin/mbeans?stats=true7&wt=xml&cat=CORE&cat=QUERY

        ConfigFileContext.processContent(url, new StreamProcess<Object>() {
            @Override
            @SuppressWarnings("all")
            public Object p(int status, InputStream stream, Map<String, List<String>> headerFields) {
                SimpleOrderedMap result = (SimpleOrderedMap) RESPONSE_PARSER.processResponse(stream, TisUTF8.getName());
                Mbeans mbeans = new Mbeans(result);//.get("solr-mbeans");
                //final SimpleOrderedMap queryHandler = (SimpleOrderedMap) mbeans.get("QUERY");
                // SimpleOrderedMap stats = null;
                Long requestCount = null;
                if (replica.getStr(Slice.LEADER) != null) {
                    // leader节点才记录访问量
                    // SimpleOrderedMap update = (SimpleOrderedMap) queryHandler.get("/update");
                    // stats = (SimpleOrderedMap) update.get("stats");
                    requestCount = mbeans.getLong("UPDATE", "/update", "requests"); // (Long) stats.get("requests");
                    updateCount.add(replicaNode, requestCount);
                    CoreStatisticsReport.this.updateErrorCount.add(replicaNode //
                            , mbeans.getLong("UPDATE", "/update", "errors.count"));

//                    SimpleOrderedMap core = (SimpleOrderedMap) mbeans.get("CORE");
//                    SimpleOrderedMap searcher = (SimpleOrderedMap) core.get("searcher");
//                    stats = (SimpleOrderedMap) searcher.get("stats");
                    // 文档总数
                    numDocs.addAndGet(mbeans.getInt(new String[]{"CORE", "SEARCHER"}, "searcher", "numDocs"));//((Integer) stats.get("numDocs")).longValue());
                }
//                SimpleOrderedMap select = (SimpleOrderedMap) queryHandler.get("/select");
//                stats = (SimpleOrderedMap) select.get("stats");
                //QUERY./select.requests
                requestCount = mbeans.getLong("QUERY", "/select", "requests");// (Long) stats.get("QUERY./select.requests");
                CoreStatisticsReport.this.requestCount.add(replicaNode, requestCount);
                CoreStatisticsReport.this.requestErrorCount.add(replicaNode, mbeans.getLong("QUERY", "/select", "errors.count"));
                return null;
            }
        });
    }

    private static class Mbeans {
        private final SimpleOrderedMap mbeans;

        public Mbeans(SimpleOrderedMap result) {
            this.mbeans = (SimpleOrderedMap) result.get("solr-mbeans");
        }

        public Long getLong(String cat, String key, String metrix) {
            return getLong(new String[]{cat}, key, metrix);
        }

        public Long getLong(String[] cat, String key, String metrix) {
            return getVal(cat, key, metrix, Long.class);
        }

        public Integer getInt(String[] cat, String key, String metrix) {
            return getVal(cat, key, metrix, Integer.class);
        }

        private <T> T getVal(String[] cat, String key, String metrix, Class<T> clazz) {
            SimpleOrderedMap c = (SimpleOrderedMap) mbeans.get(cat[0]);
            SimpleOrderedMap k = (SimpleOrderedMap) c.get(key);

            SimpleOrderedMap stats = (SimpleOrderedMap) k.get("stats");
            return clazz.cast(stats.get((cat.length > 1 ? cat[1] : cat[0]) + "." + key + "." + metrix));
        }
    }

    // /**
    // * 新版IC版本中会有多个master节点，且master节点上不存数据所以需要将master节点过滤不作统计
    // *
    // * @return
    // */
    // private Set<String> getMasterNodes(int groupIndex) {
    // if (groupIndex < 0) {
    // throw new IllegalArgumentException(
    // "groupIndex can not small than 0");
    // }
    // final Set<String> masters = new HashSet<String>();
    // 
    // try {
    // String path = "/terminator-lock/mutex/Terminator"
    // + this.getAppName() + "-" + groupIndex + "/Dumper";
    // 
    // if (this.zookeeper.exists(path, false) == null) {
    // return masters;
    // }
    // 
    // List<String> childs = this.zookeeper.getChildren(path, false);
    // for (String p : childs) {
    // if (!"owner".equalsIgnoreCase(p)) {
    // masters.add(new String(this.zookeeper.getData(path + "/"
    // + p, false, new Stat()), "gbk"));
    // }
    // }
    // } catch (Exception e) {
    // throw new RuntimeException(e);
    // }
    // 
    // return masters;
    // }

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


    @Override
    public List<String> getAllServers() {
        List<String> servers = new ArrayList<String>();
        for (ReplicaNode replic : requestCount.keySet()) {
            servers.add(replic.getHost());
        }
        return servers;
    }



    @Override
    public long getNumDocs() {
        return numDocs.longValue();
    }
}
