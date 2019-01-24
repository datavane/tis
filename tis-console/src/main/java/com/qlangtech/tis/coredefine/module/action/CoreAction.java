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
package com.qlangtech.tis.coredefine.module.action;

import static org.apache.solr.common.cloud.ZkStateReader.BASE_URL_PROP;
import static org.apache.solr.common.cloud.ZkStateReader.CORE_NAME_PROP;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.impl.XMLResponseParser;
import org.apache.solr.common.cloud.DocCollection;
import org.apache.solr.common.cloud.Replica;
import org.apache.solr.common.cloud.Slice;
import org.apache.solr.common.cloud.TISZkStateReader;
import org.apache.solr.common.cloud.ZkStateReader;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.zookeeper.data.Stat;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.qlangtech.tis.TisZkClient;
import com.qlangtech.tis.common.utils.TSearcherConfigFetcher;
import com.qlangtech.tis.coredefine.biz.FCoreRequest;
import com.qlangtech.tis.coredefine.module.control.SelectableServer;
import com.qlangtech.tis.coredefine.module.control.SelectableServer.CoreNode;
import com.qlangtech.tis.coredefine.module.control.SelectableServer.WrapperContext;
import com.qlangtech.tis.manage.PermissionConstant;
import com.qlangtech.tis.manage.biz.dal.pojo.ServerGroup;
import com.qlangtech.tis.manage.common.ConfigFileContext;
import com.qlangtech.tis.manage.common.ConfigFileContext.StreamProcess;
import com.qlangtech.tis.manage.common.HttpUtils;
import com.qlangtech.tis.manage.common.PostFormStreamProcess;
import com.qlangtech.tis.manage.common.SnapshotDomain;
import com.qlangtech.tis.manage.spring.aop.Func;
import com.qlangtech.tis.pubhook.common.RunEnvironment;
import com.qlangtech.tis.runtime.pojo.ServerGroupAdapter;
import com.qlangtech.tis.solrdao.SolrFieldsParser;
import com.qlangtech.tis.solrdao.SolrFieldsParser.ParseResult;
import com.qlangtech.tis.solrj.util.ZkUtils;

/*
 * core 应用定义
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class CoreAction extends CoreDefineModule {

    private static final long serialVersionUID = -6753169329484480543L;

    private static final Pattern PATTERN_IP = Pattern.compile("^((\\d+?).(\\d+?).(\\d+?).(\\d+?)):(\\d+)_solr$");

    // private final String CLIENT_ZK_PATH = "/terminator/dump-controller/";
    private static final int MAX_SHARDS_PER_NODE = 16;

    private static final Logger log = LoggerFactory.getLogger(CoreAction.class);

    private static final Pattern placehold_pattern = Pattern.compile("\\$\\{(.+)\\}");

    public static final String getYuntiTimePattern(String path) {
        Matcher m = placehold_pattern.matcher(path);
        if (m.find()) {
            SimpleDateFormat format = new SimpleDateFormat(m.group(1));
            return m.replaceAll(format.format(new Date()));
        }
        return path;
    }

    public static final String CREATE_CORE_SELECT_COREINFO = "selectCoreinfo";

    /**
     * 控制增量任务暂停继续，當增量任務需要重啟或者過載的情况下需要重启增量执行节点，需要先将管道中的数据全部排空
     *
     * @param context
     * @throws Exception
     */
    public void doIncrResumePause(Context context) throws Exception {
        boolean pause = this.getBoolean("pause");
        final String collection = this.getAppDomain().getAppName();
        URL applyUrl = new URL(TSearcherConfigFetcher.get().getAssembleHost() + "/incr-control");
        StringBuffer postParam = new StringBuffer("collection=");
        postParam.append(this.getAppDomain().getAppName()).append("&");
        postParam.append("stop=").append(pause).append("&action=JobRunning");
        log.info("incr pause command:" + applyUrl);
        RemoteCallResult callResult = HttpUtils.post(applyUrl, postParam.toString(), new PostFormStreamProcess<RemoteCallResult>() {

            public RemoteCallResult p(int status, InputStream stream, String md5) {
                RemoteCallResult result = new RemoteCallResult();
                JSONTokener tokener = new JSONTokener(stream);
                JSONObject j = new JSONObject(tokener);
                result.success = j.getBoolean("success");
                result.msg = j.getString("msg");
                return result;
            }
        });
        if (!callResult.success) {
            this.addErrorMessage(context, callResult.msg);
            return;
        }
        this.addActionMessage(context, collection + ":增量任务状态变为:" + (pause ? "暂停" : "启动"));
    }

    private static class RemoteCallResult {

        private boolean success;

        private String msg;
    }

    // 
    /**
     * 创建一个应用,选择组×组内副本数目
     */
    @Func(PermissionConstant.APP_INITIALIZATION)
    public void doCreateCoreSetp1(Context context) throws Exception {
        final Integer groupNum = this.getInt("group");
        final Integer repliation = this.getInt("replica");
        if (groupNum == null || groupNum < 1) {
            this.addErrorMessage(context, "组数不能小于1");
            return;
        }
        if (repliation == null || repliation < 1) {
            this.addErrorMessage(context, "组内副本数不能小于1");
            return;
        }
        context.put(CREATE_CORE_SELECT_COREINFO, new CreateCorePageDTO(groupNum, repliation, "y".equals(this.getString("shardSpecificNode"))));
        this.forward("coredefine.vm");
    }

    public static class CreateCorePageDTO {

        private final int groupNum;

        private final int replication;

        private final int assignGroupCount;

        private final boolean shardSpecificNode;

        /**
         * @param groupNum
         * @param replication
         * @param excludeHaveAppServers
         *            是否要排除已经部署应用的服务器
         */
        public CreateCorePageDTO(int groupNum, int replication, boolean shardSpecificNode) {
            this(0, groupNum, replication, shardSpecificNode);
        }

        public CreateCorePageDTO(int assignGroupCount, int groupNum, int replication, boolean shardSpecificNode) {
            super();
            this.groupNum = groupNum;
            this.replication = replication;
            this.assignGroupCount = assignGroupCount;
            // 是否使用组内节点设置
            this.shardSpecificNode = shardSpecificNode;
        }

        public int getAssignGroupCount() {
            return assignGroupCount;
        }

        public int getGroupNum() {
            return groupNum;
        }

        public int getReplication() {
            return replication;
        }

        public boolean isShardSpecificNode() {
            return shardSpecificNode;
        }

        public int[] getGroupArray() {
            int[] result = new int[groupNum];
            for (int i = 0; i < groupNum; i++) {
                result[i] = assignGroupCount + i;
            }
            return result;
        }
    }

    public static final XMLResponseParser RESPONSE_PARSER = new XMLResponseParser();

    /**
     * 创建一个应用,第二步
     */
    @SuppressWarnings("all")
    @Func(PermissionConstant.APP_INITIALIZATION)
    public void doCreateCore(final Context context) throws Exception {
        final Integer groupNum = this.getInt("group");
        // 最大组内副本数目 改过了
        final Integer repliation = this.getInt("replica");
        if (groupNum == null || groupNum < 1) {
            this.addErrorMessage(context, "组数不能小于1");
            return;
        }
        if (repliation == null || repliation < 1) {
            this.addErrorMessage(context, "组内副本数不能小于1");
            return;
        }
        // if (this.getBoolean("shardSpecificNode")) {
        // // 是否开启组内设置节点地址功能
        // final String rule = this.getString(DocCollection.RULE);
        // List<String> rules = Arrays.asList(StringUtils.split(rule, "|"));
        // }
        FCoreRequest request = createCoreRequest(context, groupNum, repliation, "创建", true, this.getBoolean("shardSpecificNode"));
        if (!request.isValid()) {
            return;
        }
        ServerGroupAdapter serverGroup = this.getServerGroup0();
        SnapshotDomain snapshotDomain = this.getSnapshotViewDAO().getView(serverGroup.getPublishSnapshotId());
        InputStream input = null;
        SolrFieldsParser parser = new SolrFieldsParser();
        ParseResult parseResult = null;
        try {
            input = new ByteArrayInputStream(snapshotDomain.getSolrSchema().getContent());
            parseResult = parser.parseSchema(input, false);
        } finally {
            IOUtils.closeQuietly(input);
        }
        String routerField = parseResult.getSharedKey();
        if (StringUtils.isBlank(routerField)) {
            this.addErrorMessage(context, "Schema中还没有设置‘sharedKey’");
            return;
        }
        final List<String> ips = request.getAllIps();
        Collections.shuffle(ips);
        Integer publishSnapshotId = serverGroup.getPublishSnapshotId();
        String nodeSet = request.getCreateNodeSet();
        // 
        this.sendCreateCollectionRequest(context, groupNum, repliation, request.getIndexName(), routerField, ips, publishSnapshotId, nodeSet, request.getRules());
    }

    private void sendCreateCollectionRequest(final Context context, final Integer groupNum, final Integer repliation, String collectionName, String routerField, final List<String> ips, Integer publishSnapshotId, String nodeSet, List<String> rules) throws Exception {
        // curl ''
        Map v = JSON.parseObject(this.getSolrZkClient().getData("/overseer_elect/leader", null, new Stat(), true), Map.class, Feature.AllowUnQuotedFieldNames);
        String id = (String) v.get("id");
        if (id == null) {
            throw new IllegalStateException("collection cluster overseer node has not launch");
        }
        String[] arr = StringUtils.split(id, "-");
        if (arr.length < 3) {
            throw new IllegalStateException("overseer ephemeral node id:" + id + " is illegal");
        }
        String routerName = "search4personas".equals(collectionName) ? "strhash" : "plain";
        StringBuffer urlBuffer = new StringBuffer("http://" + StringUtils.substringBefore(arr[1], "_") + "/solr/admin/collections?action=CREATE&name=" + collectionName + "&router.name=" + routerName + "&router.field=" + routerField + "&replicationFactor=" + repliation + "&numShards=" + groupNum + "&collection.configName=2dfire_test_config&maxShardsPerNode=" + MAX_SHARDS_PER_NODE + "&property.dataDir=data&createNodeSet=" + URLEncoder.encode(nodeSet, getEncode()) + "&property.configsnapshotid=" + publishSnapshotId);
        if (CollectionUtils.isNotEmpty(rules)) {
            for (String rule : rules) {
                urlBuffer.append("&").append(DocCollection.RULE).append("=").append(URLEncoder.encode(rule, getEncode()));
            }
        }
        URL url = new URL(urlBuffer.toString());
        log.info("create new cloud url:" + url);
        HttpUtils.processContent(url, new StreamProcess<Object>() {

            @Override
            public Object p(int status, InputStream stream, String md5) {
                if (processResponse(context, ips.get(0), stream)) {
                    addActionMessage(context, "成功触发了创建索引集群" + groupNum + "组,组内" + repliation + "个副本");
                }
                return null;
            }
        });
    }

    @SuppressWarnings("all")
    private boolean processResponse(final Context context, String serverip, InputStream stream) {
        NamedList<Object> nameList = RESPONSE_PARSER.processResponse(stream, getEncode());
        SimpleOrderedMap errors = (SimpleOrderedMap) nameList.get("error");
        if (errors != null) {
            for (int i = 0; i < errors.size(); i++) {
                addErrorMessage(context, serverip + "," + StringUtils.replace(String.valueOf(errors.getVal(i)), "\"", "'"));
            }
            return false;
        }
        SimpleOrderedMap failure = (SimpleOrderedMap) nameList.get("failure");
        if (failure != null) {
            for (int i = 0; i < failure.size(); i++) {
                addErrorMessage(context, serverip + "," + StringUtils.replace(String.valueOf(failure.getVal(i)), "\"", "'"));
            }
            return false;
        }
        return true;
    }

    /**
     * 更新全部schema
     *
     * @param context
     * @throws Exception
     */
    @Func(PermissionConstant.APP_SCHEMA_UPDATE)
    public void doUpdateSchemaAllServer(final Context context) throws Exception {
        final boolean needReload = this.getBoolean("needReload");
        DocCollection collection = getDocCollection(this.getServiceName());
        final ServerGroup group = getAppServerGroup();
        final AtomicInteger successUpdateNodeCount = new AtomicInteger();
        if (traverseCollectionReplic(collection, false, /* collection */
        new ReplicaCallback() {

            @Override
            public boolean process(boolean isLeader, final Replica replica) throws Exception {
                String baseUrl = replica.getStr(BASE_URL_PROP);
                Integer snapshotid = getCurrentSnapshotId(collection, baseUrl);
                if (group != null && snapshotid.equals(group.getPublishSnapshotId())) {
                    // 远端的配置文件已经同步，可以跳过
                    return true;
                }
                URL url = new URL(baseUrl + "/admin/cores?action=CREATEALIAS&execaction=updateconfig&core=" + replica.getStr(CORE_NAME_PROP) + "&" + ZkStateReader.COLLECTION_PROP + "=" + getServiceName() + "&needReload=" + needReload);
                return HttpUtils.processContent(url, new StreamProcess<Boolean>() {

                    @Override
                    public Boolean p(int status, InputStream stream, String md5) {
                        successUpdateNodeCount.incrementAndGet();
                        return processResponse(context, replica.getNodeName(), stream);
                    }
                });
            }
        })) {
            if (successUpdateNodeCount.get() > 0) {
                this.addActionMessage(context, "已经更新" + successUpdateNodeCount.get() + "个节点的配置文件");
            } else {
                this.addErrorMessage(context, "节点的配置已同步,无需重复操作");
            }
        }
    }

    /**
     * 取得远端core节点使用的
     *
     * @param url
     * @return
     */
    private Integer getCurrentSnapshotId(DocCollection collection, final String url) {
        try {
            return ConfigFileContext.processContent(new URL(url + "/" + collection.getName() + "/admin/file/?file=config.properties"), new StreamProcess<Integer>() {

                @Override
                public Integer p(int status, InputStream stream, String md5) {
                    try {
                        Properties p = new Properties();
                        p.load(stream);
                        return Integer.parseInt(p.getProperty("configsnapshotid"));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        } catch (Throwable e) {
            log.warn(collection.getName(), e);
            return -1;
        }
    }

    /**
     * 遍历所有的副本节点
     *
     * @param collection
     * @param firstProcessLeader
     * @param replicaCallback
     * @return
     * @throws Exception
     */
    public static boolean traverseCollectionReplic(final DocCollection collection, boolean firstProcessLeader, ReplicaCallback replicaCallback) throws Exception {
        Replica leader = null;
        // String requestId = null;
        for (Slice slice : collection.getSlices()) {
            leader = slice.getLeader();
            if (firstProcessLeader) {
                if (!replicaCallback.process(true, leader)) {
                    return false;
                }
            }
            for (Replica replica : slice.getReplicas()) {
                if (leader == replica) {
                    continue;
                }
                if (!replicaCallback.process(true, replica)) {
                    return false;
                }
            }
            if (!firstProcessLeader) {
                if (!replicaCallback.process(true, leader)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static interface ReplicaCallback {

        public boolean process(boolean isLeader, Replica replica) throws Exception;
    }

    // 
    // /**
    // * 设置这次操作的json描述
    // *
    // * @param request
    // * @throws Exception
    // */
    // // private void setOperationLogDesc(FCoreRequest request) throws
    // Exception {
    // // JSONObject json = new JSONObject();
    // // json.put("name", request.getRequest().getServiceName());
    // // json.put("terminatorUrl", request.getRequest().getTerminatorUrl());
    // // json.put("monopolized", request.getRequest().isMonopolized());
    // //
    // // JSONObject servers = new JSONObject();
    // //
    // // for (Map.Entry<Integer, Collection<String>> entry : request
    // // .getServersView().entrySet()) {
    // // JSONArray group = new JSONArray();
    // //
    // // for (String ip : entry.getValue()) {
    // // group.put(ip);
    // // }
    // // servers.put("group" + entry.getKey(), group);
    // // }
    // // json.put("servers", servers);
    // // request.getRequest().setOpDesc(json.toString());
    // // request.getRequest().setLoggerContent(StringUtils.EMPTY);
    // //
    // // }
    // 
    // private Map<String, CoreNode> getCoreNodeMap() {
    // return getCoreNodeMap(/* getCoreNodeMap */false);
    // 
    // }
    // 
    // /**
    // * 取得当前应用
    // *
    // * @return
    // */
    private Map<String, CoreNode> getCoreNodeMap(boolean isAppNameAware) {
        CoreNode[] nodelist = SelectableServer.getCoreNodeInfo(this.getRequest(), this, false, isAppNameAware);
        Map<String, CoreNode> result = new HashMap<String, CoreNode>();
        for (CoreNode node : nodelist) {
            result.put(node.getName(), node);
        }
        return result;
    }

    // 
    // /**
    // * @param context
    // * @param serverSuffix
    // * @param isAppNameAware
    // * @param mustSelectMoreOneReplicAtLeast
    // * 每一组至少选一个副本（在创建core的时候，每组至少要选一个以上的副本， <br/>
    // * 但是在减少副本的时候每组可以一个副本都不选）
    // * @return
    // */
    private ParseIpResult parseIps(Context context, String serverSuffix, boolean isAppNameAware, boolean mustSelectMoreOneReplicAtLeast) {
        ParseIpResult result = new ParseIpResult();
        result.valid = false;
        List<String> parseips = new ArrayList<String>();
        String[] ips = this.getRequest().getParameterValues("selectedServer" + StringUtils.trimToEmpty(serverSuffix));
        if (ips == null) {
            return result;
        }
        if (mustSelectMoreOneReplicAtLeast && ips.length < 1) {
            addErrorMessage(context, "请" + (StringUtils.isNotEmpty(serverSuffix) ? "为第" + serverSuffix + "组" : StringUtils.EMPTY) + "选择服务器");
            return result;
        }
        // StringBuffer ipLiteria = new StringBuffer();
        result.ipLiteria.append("[");
        Matcher matcher = null;
        final Map<String, CoreNode> serverdetailMap = getCoreNodeMap(isAppNameAware);
        CoreNode nodeinfo = null;
        CoreNode current = null;
        for (String ip : ips) {
            matcher = this.isValidIpPattern(ip);
            if (!matcher.matches()) {
                this.addErrorMessage(context, "IP:" + ip + "不符合格式规范");
                return result;
            }
            // ▼▼▼ 校验组内服务器lucene版本是否一致
            current = serverdetailMap.get(ip);
            if (current == null) {
                this.addErrorMessage(context, "服务器" + ip + "，不在可选集合之内");
                return result;
            }
            if (nodeinfo != null) {
                if (!StringUtils.equalsIgnoreCase(current.getLuceneSpecVersion(), nodeinfo.getLuceneSpecVersion())) {
                    this.addErrorMessage(context, (StringUtils.isNotEmpty(serverSuffix) ? "第" + serverSuffix + "组" : StringUtils.EMPTY) + "服务器Lucene版本不一致");
                    return result;
                }
            }
            nodeinfo = current;
            // ▲▲▲ 校验组内服务器lucene版本是否一致
            // matcher.group(group)
            parseips.add(matcher.group(0));
            result.ipLiteria.append(ip).append(",");
        }
        result.ipLiteria.append("]");
        result.ips = parseips.toArray(new String[parseips.size()]);
        result.valid = true;
        return result;
    }

    // 
    private static class ParseIpResult {

        private boolean valid;

        private String[] ips;

        private final StringBuffer ipLiteria = new StringBuffer();
    }

    private CoreRequest createIps(Context context, final String appName, String[] ips) {
        CoreRequest request = new CoreRequest();
        request.setIncludeIps(ips);
        request.setServiceName(appName);
        // request.setTerminatorUrl(Config.getTerminatorRepository());
        // 是否独占机器？ 百岁 add 2013/04/12 start
        // request.setMonopolized("true".equalsIgnoreCase(this.getString("excludeHaveAppsServer")));
        // 是否独占机器？ 百岁 add 2013/04/12 end
        Assert.notNull(this.getAppDomain().getRunEnvironment(),"this.getAppDomain().getRunEnvironment() can not be null");
        // request.setRunEnv(this.getAppDomain().getRunEnvironment().getId());
        // request.setConfigFiles(new String[] { ConfigConstant.FILE_SCHEMA,
        // ConfigConstant.FILE_SOLOR, ConfigConstant.FILE_CORE_PROPERTIES });
        log.debug("request.getRunEnv():" + request.getRunEnv());
        return request;
    }

    public static class CoreRequest {

        private String[] ips;

        private String serviceName;

        // private boolean monopolized;
        private RunEnvironment runtime;

        public void addNodeIps(String groupIndex, String ip) {
        }

        public void setIncludeIps(String[] ips) {
            this.ips = ips;
        }

        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }

        public String getServiceName() {
            return serviceName;
        }

        // public void setMonopolized(boolean monopolized) {
        // this.monopolized = monopolized;
        // }
        public RunEnvironment getRunEnv() {
            return this.runtime;
        }
    }

    // 
    // /**
    // * 取得配置文件是否与远端配置文件相同
    // */
    // @Func(PermissionConstant.APP_SCHEMA_UPDATE)
    // public void doGetCoreConfigIsConsist(Context context) throws Exception {
    // Integer snid = this.getInt("snapshotid");
    // Assert.assertNotNull(snid);
    // final AppDomainInfo domain = this.getAppDomain();
    // SnapshotDomain snapshot = this.getSnapshotViewDAO().getView(snid,
    // domain.getRunEnvironment());
    // 
    // final Map<Short, List<ServerJoinGroup>> allServers = getAllServers();
    // 
    // final StringBuffer serverSummary = new StringBuffer();
    // 
    // for (Map.Entry<Short, List<ServerJoinGroup>> entry : allServers
    // .entrySet()) {
    // serverSummary.append("组" + entry.getKey() + ":");
    // for (ServerJoinGroup server : entry.getValue()) {
    // 
    // boolean configConsist = true;
    // 
    // for (int i = 0; i < ConfigFileReader.getAry.length; i++) {
    // PropteryGetter getter = ConfigFileReader.getAry[i];
    // if (ConfigConstant.FILE_APPLICATION.equals(getter
    // .getFileName())
    // || ConfigConstant.FILE_DATA_SOURCE.equals(getter
    // .getFileName())
    // || ConfigConstant.FILE_JAR.equals(getter
    // .getFileName())) {
    // continue;
    // }
    // byte[] localContent = null;
    // try {
    // localContent = getter.getContent(snapshot);
    // } catch (Throwable e) {
    // log.error(e);
    // continue;
    // }
    // if (localContent == null || localContent.length < 1) {
    // continue;
    // }
    // 
    // String md5local = ConfigFileReader.md5file(localContent);
    // 
    // // 取得远端的文件
    // String md5Remote = null;
    // try {
    // md5Remote = ConfigFileReader
    // .md5file(BasicRemoteServerConfig
    // .getFileContent(domain.getAppName(),
    // new Integer(entry.getKey()),
    // server.getIpAddress(),
    // server.getPort(),
    // getter.getFileName(),
    // domain.getRunEnvironment()));
    // } catch (RemoteNotReachableException e) {
    // this.addErrorMessage(context, "第" + e.getGroup()
    // + "组的服务器:" + e.getIpaddress()
    // + "不可用,请确认服务器是否挂了");
    // return;
    // }
    // 
    // if (!StringUtils.equals(md5local, md5Remote)) {
    // this.addErrorMessage(context, "组" + entry.getKey()
    // + "ip:" + server.getIpAddress() + ",file:"
    // + getter.getFileName() + " 还没有同步成功");
    // configConsist = false;
    // }
    // }
    // 
    // if (configConsist) {
    // serverSummary.append(server.getIpAddress());
    // }
    // }
    // serverSummary.append("<br/>");
    // }
    // 
    // if (allServers.size() > 0) {
    // this.addActionMessage(context,
    // "以下服务器上的配置文件已经成功同步<br/>" + String.valueOf(serverSummary));
    // } else {
    // this.addErrorMessage(context, "还没有为应用初始化,请先初始化应用之后再同步配置文件");
    // }
    // 
    // }
    // 
    // /**
    // * 取得所有的服务器
    // *
    // * @return
    // */
    // private Map<Short, List<ServerJoinGroup>> getAllServers() {
    // final Map<Short, List<ServerJoinGroup>> answer = new HashMap<Short,
    // List<ServerJoinGroup>>();
    // final QueryResutStrategy queryResutStrategy = QueryResutStrategy
    // .create(this.getAppDomain(),
    // new ServerConfigView.SolrQueryModuleCreatorAllAdapter() {
    // @Override
    // public void setQuerySelectServerCandiate(
    // Map<Short, List<ServerJoinGroup>> servers) {
    // answer.putAll(servers);
    // }
    // 
    // }, this);
    // queryResutStrategy.queryProcess();
    // return answer;
    // }
    // 
    // @Func(PermissionConstant.APP_SOLRCONFIG_UPDATE)
    // public void doUpdateServerByServer(Context context) throws Exception {
    // 
    // if (isDumpWorking(context)) {
    // return;
    // }
    // 
    // String[] servers =
    // this.getRequest().getParameterValues("updateServer");//
    // .getParameters();
    // Map<Integer, MachinePair> pair = new HashMap<Integer, MachinePair>();
    // 
    // String[] args = null;
    // StringBuffer summary = new StringBuffer();
    // for (String server : servers) {
    // 
    // args = StringUtils.split(server, "_");
    // Integer groupIndex = Integer.parseInt(args[1]);
    // MachinePair p = pair.get(groupIndex);
    // if (p == null) {
    // p = new MachinePair();
    // p.groupIndex = groupIndex;
    // p.machineAddress = new ArrayList<String>();
    // pair.put(groupIndex, p);
    // }
    // String serverIp = StringUtils.substringBefore(args[0], ":");
    // summary.append("group:").append(groupIndex).append(
    // ",serverAddress:").append(serverIp).append("<br/>");
    // p.machineAddress.add(serverIp);
    // }
    // 
    // getClientProtocol().repairGroupIndex(
    // this.getAppDomain().getAppName(),
    // (new ArrayList<MachinePair>(pair.values()))
    // .toArray(new MachinePair[pair.size()]));//
    // (this.getAppDomain().getAppName());
    // 
    // this.addActionMessage(context, "已经恢复了以下几台机器：<br>" +
    // summary.toString());
    // }
    // 2012 0901102222
    // private ZooKeeperGetter zooKeeperGetter;
    // private static final Pattern userpointPattern = Pattern
    // .compile(".+?#201\\d{11}");
    // 
    // @Autowired
    // public void setZooKeeperGetter(ZooKeeperGetter zooKeeperGetter) {
    // this.zooKeeperGetter = zooKeeperGetter;
    // }
    // 
    // private static final MessageFormat CLIENT_VERSION = new MessageFormat(
    // "/terminator/client-version/{0}/{1}");
    // // 与客户端进行socket通信
    // private void subscribeClientSocketService() throws Exception {
    // final Stat stat = new Stat();
    // String zkPath = CLIENT_ZK_PATH + this.getServiceName();
    // IKeeper zkClient = getZooKeeper();
    // String data = new String(zkClient.getData(zkPath, false, stat));
    // String[] ip = data.split(":");
    // log.info("get client socketService for zkPath : " + zkPath
    // + " data : " + data);
    // int port = Integer.parseInt(ip[1]);
    // log.info("send request 2 client ip:" + ip[0] + " port: " + port);
    // applyFulldumpRequest(ip[0], port);
    // }
    // 
    // /**
    // * @param ip
    // * @param port
    // * @throws UnknownHostException
    // * @throws IOException
    // * @throws Exception
    // */
    // protected static void applyFulldumpRequest(String ip, int port)
    // throws UnknownHostException, IOException, Exception {
    // final Socket socket = new Socket(ip, port);
    // 
    // ObjectOutputStream os = null;
    // BufferedReader buf = new BufferedReader(new InputStreamReader(
    // socket.getInputStream()));
    // os = new ObjectOutputStream(socket.getOutputStream());
    // TriggerImportJob triggerImportJob = new TriggerImportJob(
    // "triggerFullImportJob");
    // os.writeObject(triggerImportJob);
    // os.flush();
    // String msg = buf.readLine();
    // socket.close();
    // if (msg == null) {
    // throw new Exception("触发全量失败");
    // }
    // }
    // 
    // /**
    // * 取得触发中心服务端地址
    // *
    // * @return
    // */
    // private List<String> getTriggerServerAddress() {
    // return getTriggerServerAddress(this.getZooKeeper());
    // }
    // 
    // public static List<String> getTriggerServerAddress(IKeeper zk) {
    // List<String> result = new ArrayList<String>();
    // try {
    // String ppath = TRIGGER_SERVER + Config.getProjectName();
    // List<String> child = zk.getChildren(ppath, false);
    // if (child.isEmpty()) {
    // throw new IllegalStateException("zk path:" + ppath
    // + " is empty");
    // }
    // Stat stat = new Stat();
    // for (String c : child) {
    // result.add(new String(zk.getData(ppath + "/" + c, false, stat)));
    // }
    // 
    // } catch (Exception e) {
    // throw new TerminatorInitException(e);
    // }
    // return result;
    // }
    // 
    // /**
    // * 通过触发中心来触发客户端全量
    // *
    // * @param context
    // * @return 是否成功触发
    // * @throws MalformedURLException
    // */
    // private LaunchResult launchFullDumpByTriggerCenter(final Context context)
    // throws MalformedURLException {
    // final LaunchResult result = new LaunchResult();
    // // 取得触发中心的地址
    // final List<String> ips = getTriggerServerAddress();
    // boolean triggerSuccess = false;
    // for (final String ip : ips) {
    // // 触发trigger中心
    // final URL url = new URL("http://" + ip
    // + ":8199/trigger_full_dump?serviceName=" + getServiceName());
    // 
    // triggerSuccess = ConfigFileContext.processContent(url,
    // new StreamProcess<Boolean>() {
    // @Override
    // public Boolean p(int status, InputStream stream,
    // String md5) {
    // try {
    // 
    // TriggerDumpResult json = TriggerDumpResult
    // .deserialize(IOUtils.toString(stream));
    // 
    // // JSONTokener tokener = new JSONTokener(IOUtils
    // // .toString(stream));
    // // JSONObject json = new JSONObject(tokener);
    // if ("success".equals(json.getStatus())) {
    // 
    // result.taskId = json.getTaskId();
    // result.triggerSuccess = true;
    // // 创建一个监听taskid的进程
    // 
    // addActionMessage(
    // context,
    // "向触发服务器："
    // + ip
    // + "，发送全量触发命令，<a target='_blank' href='/jsp/feedbacklog.jsp?appname="
    // + getServiceName()
    // + "&runtime="
    // + getAppDomain()
    // .getRunEnvironment()
    // .getKeyName()
    // + "'>查看实时日志</a>");
    // return true;
    // }
    // 
    // } catch (Exception e) {
    // log.error(url.toString(), e);
    // }
    // 
    // return false;
    // }
    // });
    // 
    // if (triggerSuccess) {
    // break;
    // }
    // }
    // 
    // if (!triggerSuccess) {
    // StringBuffer ipsContent = new StringBuffer();
    // for (String ip : ips) {
    // ipsContent.append(ip).append(",");
    // }
    // this.addErrorMessage(context, "尝试了以下triggerServer" + ipsContent
    // + " 没有触发成功");
    // }
    // 
    // return result;
    // 
    // }
    // 
    // private class LaunchResult {
    // boolean triggerSuccess = false;
    // private Long taskId;
    // 
    // }
    // 
    /**
     * 主动触发全量dump
     *
     * @param context
     * @throws Exception
     */
    @Func(PermissionConstant.APP_TRIGGER_FULL_DUMP)
    public void doTriggerDump(final Context context) throws Exception {
        // final String versionPath = CLIENT_VERSION.format(new Object[] {
        // getServiceName(), clientIp });
        // byte[] versionContent = ;
        // String zkPath = CLIENT_ZK_PATH + this.getServiceName();
        // if (clientIp.contains(":")) {
        // try {
        // subscribeClientSocketService();
        // } catch (Exception e) {
        // this.addActionMessage(context, "调用客户端 Socket 服务失败");
        // log.error(e.getMessage(), e);
        // return;
        // }
        // 
        // } else {
        // this.getClientProtocol().triggerServiceFullDump(
        // this.getServiceName());
        // }
        // curl
        // 'http://localhost:14844/trigger?component.start=indexBackflow&ps=20151215155124'
        boolean success = sendRequest2FullIndexSwapeNode(context, new AppendParams() {

            @Override
            String getParam() {
                return StringUtils.EMPTY;
            }
        });
        if (success) {
            this.addActionMessage(context, "已经触发了全量DUMP(triggerServiceFullDump)");
        }
    }

    /**
     * @param context
     * @param clientIp
     * @return
     * @throws MalformedURLException
     */
    private boolean sendRequest2FullIndexSwapeNode(final Context context, AppendParams appendParams) throws Exception {
        // final Stat stat = new Stat();
        // final String ipLockPath = GRANTED_LOCK_CLIENT_IP
        // .format(new Object[] { this.getServiceName() });
        TisZkClient zk = this.getSolrZkClient();
        final String incrStateCollectAddress = ZkUtils.getFirstChildValue(zk, // reConnect
        "/tis/incr-transfer-group/incr-state-collect", // reConnect
        null, // reConnect
        true);
        String assembleNodeIp = StringUtils.substringBefore(incrStateCollectAddress, ":") + ":8080";
       
        boolean success = HttpUtils.processContent(new URL("http://" + assembleNodeIp + "/trigger?appname=" + this.getServiceName() + appendParams.getParam()), new StreamProcess<Boolean>() {

            @Override
            public Boolean p(int status, InputStream stream, String md5) {
                JSONTokener token = new JSONTokener(stream);
                JSONObject result = new JSONObject(token);
                String successKey = "success";
                if (result.isNull(successKey)) {
                    return false;
                }
                if (result.getBoolean(successKey)) {
                    return true;
                }
                addErrorMessage(context, result.getString("msg"));
                return false;
            }
        });
        return success;
    // }
    // final String clientIp = new String(lock.childValus.get(0));//
    // StringUtils.substringBeforeLast(,
    // // ":");// ;
    // return false;
    // return success;
    }

    private abstract static class AppendParams {

        abstract String getParam();
    }

    /**
     * 刷新可选节点
     *
     * @param context
     * @throws Exception
     */
    public void doFreshSelectableNode(Context ctx) throws Exception {
        // long curr = System.currentTimeMillis();
        TISZkStateReader zkReader = this.getZkStateReader();
        zkReader.clearSelectTableNodes();
        WrapperContext context = new WrapperContext(ctx);
        ArrayList<CoreNode> selectableNodes = new ArrayList<>(zkReader.getSelectTableNodes());
        context.pubCoreNodeList(selectableNodes);
        this.setBizResult(ctx, selectableNodes);
    }

    /**
     * 索引回流
     *
     * @param context
     * @throws Exception
     */
    @Func(PermissionConstant.APP_TRIGGER_FULL_DUMP)
    public void doTriggerSynIndexFile(Context context) throws Exception {
        final String userpoint = this.getString("userpoint");
        if (StringUtils.isBlank(userpoint)) {
            this.addErrorMessage(context, "请填写userpoint，格式：admin#yyyyMMddHHmmss");
            return;
        }
        boolean success = triggerBuild("indexBackflow", context, userpoint);
        if (success) {
            this.addActionMessage(context, "已经触发了userpoint回流" + StringUtils.defaultIfEmpty(userpoint, "-1"));
        }
    }

    /**
     * @param context
     * @param userpoint
     * @return
     * @throws Exception
     */
    private boolean triggerBuild(final String startPhrase, Context context, final String userpoint) throws Exception {
        String ps = null;
        String user = null;
        if (StringUtils.indexOf(userpoint, "#") > -1) {
            ps = StringUtils.substringAfter(userpoint, "#");
            user = StringUtils.substringBefore(userpoint, "#");
        } else {
            ps = userpoint;
        }
        // TODO 应该索引数据是否存在
        final String pps = ps;
        final String fuser = user;
        boolean success = sendRequest2FullIndexSwapeNode(context, new AppendParams() {

            @Override
            String getParam() {
                StringBuffer param = new StringBuffer("&component.start=" + startPhrase + "&ps=" + pps);
                if (StringUtils.isNotBlank(fuser)) {
                    param.append("&user=").append(fuser);
                }
                return param.toString();
            }
        });
        return success;
    }

    /**
     * 触发索引build（不需要dump）
     *
     * @param context
     * @throws Exception
     */
    @Func(PermissionConstant.APP_TRIGGER_FULL_DUMP)
    public void doTriggerFullDumpFile(Context context) throws Exception {
        final String userpoint = this.getString("userpoint");
        if (StringUtils.isBlank(userpoint)) {
            this.addErrorMessage(context, "请填写userpoint，格式：admin#yyyyMMddHHmmss");
            return;
        }
        boolean success = triggerBuild("indexBuild", context, userpoint);
        if (success) {
            addActionMessage(context, "已经触发了triggerFullDumpFile,userpoint:" + StringUtils.defaultIfEmpty(userpoint, "-1"));
        }
    }

    // 
    // // private TriggerJobConsole triggerJobConsole;
    // 
    // // @Autowired
    // // public final void setTriggerJobConsole(TriggerJobConsole
    // // triggerJobConsole) {
    // // this.triggerJobConsole = triggerJobConsole;
    // // }
    // 
    // /**
    // * 校验dump是否正在执行
    // *
    // * @return
    // */
    // private boolean isDumpWorking(Context context) {
    // // try {
    // //
    // // String coreName = this.getAppDomain().getAppName();
    // //
    // // boolean pause = triggerJobConsole.isPause(coreName);
    // //
    // // if (!pause) {
    // // this.addErrorMessage(context, "当前应用：" + coreName
    // // + "有Dump Job 正在执行，请确认已经停止后再执行该操作");
    // // }
    // //
    // // return !pause;
    // //
    // // } catch (RemoteException e) {
    // // throw new RuntimeException(e);
    // // }
    // return false;
    // }
    // 
    private String getServiceName() {
        return this.getAppDomain().getAppName();
    }

    // 
    // /**
    // * 更新某一组的schema文件
    // *
    // * @param context
    // * @throws Exception
    // */
    // @Func(PermissionConstant.APP_SCHEMA_UPDATE)
    // public void doUpdateSchemaByGroup(Context context) throws Exception {
    // 
    // Integer group = this.getInt("group");
    // // Assert.assertNotNull(group);
    // if (group == null || group < 0) {
    // this.addErrorMessage(context, "请设置组");
    // return;
    // }
    // 
    // if (isDumpWorking(context)) {
    // return;
    // }
    // 
    // this.getClientProtocol().schemaChange(getServiceName(), group);
    // this.addActionMessage(context, "触发第" + group + "组Schema文件更新成功");
    // }
    // 
    // /**
    // * 更新所有的solrconfig
    // *
    // * @param context
    // * @throws Exception
    // */
    // @Func(PermissionConstant.APP_SOLRCONFIG_UPDATE)
    // public void doUpdateSolrconfigAllServer(Context context) throws Exception
    // {
    // if (isDumpWorking(context)) {
    // return;
    // }
    // this.getClientProtocol().coreConfigChange(this.getServiceName());
    // 
    // this.addActionMessage(context, "已经成功触发了更新全部Solrconfig");
    // }
    // 
    // /**
    // * 更新solrconfig
    // *
    // * @param context
    // * @throws Exception
    // */
    // // doUpdateSchemaByGroup
    // @Func(PermissionConstant.APP_SOLRCONFIG_UPDATE)
    // public void doUpdateSolrconfigByGroup(Context context) throws Exception {
    // 
    // Integer group = this.getInt("group");
    // if (group == null || group < 0) {
    // this.addErrorMessage(context, "请设置组");
    // return;
    // }
    // 
    // if (isDumpWorking(context)) {
    // return;
    // }
    // 
    // this.getClientProtocol().coreConfigChange(this.getServiceName(), group);
    // this.addActionMessage(context, "触发第" + group + "组solrconfig文件更新成功");
    // }
    // 
    // private static final Pattern serverPattern = Pattern
    // .compile("(.+?)_(\\d+)");
    // 
    // /**
    // * 具体更新某一个服务器
    // *
    // * @param context
    // * @throws Exception
    // */
    // @Func(PermissionConstant.APP_SOLRCONFIG_UPDATE)
    // public void doUpdateSolrConfigByServer(Context context) throws Exception
    // {
    // // Integer group = this.getInt("group");
    // // Assert.assertNotNull(group);
    // //
    // // String server = this.getString("server");
    // // Assert.assertNotNull(group);
    // 
    // if (isDumpWorking(context)) {
    // return;
    // }
    // 
    // updateResourceByServer(context, new ResourceConfigRefesh() {
    // 
    // @Override
    // public void update(String ip, Integer group, Context context)
    // throws Exception {
    // getClientProtocol().coreConfigChange(getServiceName(), group,
    // ip);
    // addActionMessage(context, "已经触发了服务器：" + ip + ",第" + group
    // + " 组的 solrconfig更新");
    // }
    // 
    // });
    // 
    // }
    // 
    // private void updateResourceByServer(Context context,
    // ResourceConfigRefesh resourceRefesh) throws Exception {
    // String[] servers = this.getRequest().getParameterValues("updateServer");
    // 
    // if (servers == null || servers.length < 1) {
    // this.addErrorMessage(context, "请选择服务器");
    // return;
    // }
    // Matcher matcher = null;
    // String ip = null;
    // Integer group = null;
    // for (String server : servers) {
    // matcher = serverPattern.matcher(server);
    // 
    // if (!matcher.matches()) {
    // this.addErrorMessage(context, "传递参数updateServer 不合法：" + server);
    // return;
    // }
    // 
    // group = Integer.parseInt(matcher.group(2));
    // 
    // matcher = this.isValidIpPattern(matcher.group(1));
    // if (!matcher.matches()) {
    // this.addErrorMessage(context, "IP:" + ip + "不符合格式规范");
    // return;
    // }
    // 
    // ip = matcher.group(1);
    // 
    // resourceRefesh.update(ip, group, context);
    // 
    // }
    // }
    // 
    // private interface ResourceConfigRefesh {
    // public void update(String ip, Integer group, Context context)
    // throws Exception;
    // 
    // }
    // 
    // // do_update_hsf_all_server
    // @Func(PermissionConstant.APP_HSF_UPDATE)
    // public void doUpdateHsfAllServer(Context context) throws Exception {
    // 
    // this.getClientProtocol().rePublishHsf(this.getServiceName());
    // 
    // this.addActionMessage(context, "已经成功触发了所有hsf重新发布");
    // }
    // 
    // // doUpdateSolrconfigByGroup
    // @Func(PermissionConstant.APP_HSF_UPDATE)
    // public void doUpdateHsfByGroup(Context context) throws Exception {
    // 
    // Integer group = this.getInt("group");
    // if (group == null || group < 0) {
    // this.addErrorMessage(context, "请设置组号");
    // return;
    // }
    // 
    // this.getClientProtocol().rePublishHsf(this.getServiceName(), group);
    // this.addActionMessage(context, "触发第" + group + "组Hsf服务更新成功");
    // }
    // 
    // /**
    // * @param context
    // * @throws Exception
    // */
    // @Func(PermissionConstant.APP_HSF_UPDATE)
    // public void doUpdateHsfByServer(Context context) throws Exception {
    // updateResourceByServer(context, new ResourceConfigRefesh() {
    // @Override
    // public void update(String ip, Integer group, Context context)
    // throws Exception {
    // getClientProtocol().republishHsf(getServiceName(), group, ip);
    // addActionMessage(context, "已经触发服务器[" + ip + "]HSF服务重新发布");
    // }
    // });
    // // Integer group = this.getInt("group");
    // // // Assert.assertNotNull(group);
    // // if (group == null || group < 1) {
    // // this.addErrorMessage(context, "请设置组号");
    // // return;
    // // }
    // //
    // // final String server = this.getString("server");
    // // // Matcher m = PATTERN_IP.matcher(StringUtils.trimToEmpty(server));
    // // if (isValidIpPattern(server)) {
    // // this.addErrorMessage(context, "请设置组号");
    // // return;
    // // }
    // //
    // // this.getClientProtocol().republishHsf(this.getServiceName(), group,
    // // server);
    // //
    // // this.addActionMessage(context, "已经触发服务器[" + server + "]HSF服务重新发布");
    // 
    // }
    // 
    // /**
    // *
    // * @return
    // */
    private Matcher isValidIpPattern(String server) {
        return PATTERN_IP.matcher(StringUtils.trimToEmpty(server));
    // return (m.matches());
    }

    // 
    // /**
    // * 减少组内副本数目
    // *
    // * @param context
    // * @throws Exception
    // */
    // @Func(PermissionConstant.APP_REPLICA_MANAGE)
    // public void doDecreaseReplica(Context context) throws Exception {
    // 
    // if (isDumpWorking(context)) {
    // return;
    // }
    // 
    // // this.getClientProtocol().desCoreReplication(request, replication);
    // updateReplica(context, new ReplicaUpdate() {
    // @Override
    // public String getExecuteLiteria() {
    // return "减少";
    // }
    // 
    // @Override
    // public void update(CoreRequest request, short[] replicCount)
    // throws IOException {
    // getClientProtocol().desCoreReplication(request, replicCount);
    // }
    // 
    // // @Override
    // // public void update(CoreRequest corerequest, Integer replica)
    // // throws IOException {
    // //
    // // }
    // 
    // @Override
    // public boolean shallContinueProcess(Context context,
    // FCoreRequest request) {
    // // 删除的副本中如果有master节点的话就不能删除
    // boolean canReduceReplica = !getClientProtocol()
    // .hasRealTimeLeaderServer(
    // CoreAction.this.getServiceName(),
    // request.getIps());
    // if (!canReduceReplica) {
    // CoreAction.this.addErrorMessage(context, "因为被删除的机器"
    // + request.getIps()
    // + "中有实时模式的master节点，master节点不能被删除");
    // }
    // 
    // return canReduceReplica;
    // }
    // 
    // });
    // }
    // 
    // /**
    // * 添加组内副本数目
    // *
    // * @param context
    // * @throws Exception
    // */
    // @Func(PermissionConstant.APP_REPLICA_MANAGE)
    // public void doAddReplica(Context context) throws Exception {
    // 
    // if (isDumpWorking(context)) {
    // return;
    // }
    // 
    // updateReplica(context, new ReplicaUpdate() {
    // @Override
    // public String getExecuteLiteria() {
    // return "添加";
    // }
    // 
    // @Override
    // public void update(CoreRequest request, short[] replicCount)
    // throws IOException {
    // getClientProtocol().addCoreReplication(request, replicCount);
    // }
    // });
    // }
    // 
    // /**
    // * 更新组内副本数，用于增加或者减少副本
    // *
    // * @param context
    // * @param replicaUpdate
    // * @throws Exception
    // */
    // private void updateReplica(Context context, ReplicaUpdate replicaUpdate)
    // throws Exception {
    // final Integer replica = this.getInt("replica");
    // Integer group = this.getInt("groupcount");
    // 
    // if (replica == null || replica < 1) {
    // this.addErrorMessage(context, "请设置合法的组内副本数");
    // return;
    // }
    // if (group == null || group < 1) {
    // this.addErrorMessage(context, "请设置组数");
    // return;
    // }
    // 
    // if (!this.getCoreManager().isCreateNewServiceSuc(this.getServiceName()))
    // {
    // this.addErrorMessage(context, "该Solr应用正在创建索引中，请等待创建成功之后再设置");
    // return;
    // }
    // 
    // FCoreRequest request = createCoreRequest(context, 0, group, replica,
    // replicaUpdate.getExecuteLiteria(),/* isAppNameAware */true, /*
    // mustSelectMoreOneReplicAtLeast */
    // false);
    // 
    // // FCoreRequest request = createCoreRequest(context, group, replica,
    // // replicaUpdate.getExecuteLiteria(), false/*
    // // mustSelectMoreOneReplicAtLeast */);
    // 
    // if (!request.isValid()
    // && replicaUpdate.shallContinueProcess(context, request)) {
    // return;
    // }
    // 
    // // 编辑操作日志
    // // setOperationLogDesc(request);
    // 
    // replicaUpdate.update(request.getRequest(), request.getReplicCount());
    // this.addActionMessage(context, replicaUpdate.getExecuteLiteria()
    // + "副本成功!");
    // }
    // 
    private FCoreRequest createCoreRequest(Context context, Integer group, Integer replica, String setVerbs, boolean mustSelectMoreOneReplicAtLeast, boolean shardSpecificNode) {
        return createCoreRequest(context, 0, group, replica, setVerbs, mustSelectMoreOneReplicAtLeast, shardSpecificNode);
    }

    private FCoreRequest createCoreRequest(Context context, int assignGroupCount, int group, int replica, String setVerbs, boolean mustSelectMoreOneReplicAtLeast, boolean shardSpecificNode) {
        return createCoreRequest(context, assignGroupCount, group, replica, setVerbs, /* isAppNameAware */
        false, mustSelectMoreOneReplicAtLeast, shardSpecificNode);
    }

    /**
     * @param context
     * @param assignGroupCount
     *            已经分配到的group数量
     * @param group
     *            添加的组数
     * @param replica
     * @param setVerbs
     * @param shardSpecificNode
     *            组内设置节点
     * @return
     */
    private FCoreRequest createCoreRequest(Context context, int assignGroupCount, int group, int replica, String setVerbs, boolean isAppNameAware, boolean mustSelectMoreOneReplicAtLeast, boolean shardSpecificNode) {
        // boolean valid = false;
        // CoreRequest request = ;
        CoreRequest coreRequest = createIps(context, this.getServiceName(), null);
        FCoreRequest result = new FCoreRequest(coreRequest, assignGroupCount + group, assignGroupCount);
        // StringBuffer addserverSummary = new StringBuffer();
        // 不用为单独的一个组设置服务ip地址
        final int GROUP_SIZE = shardSpecificNode ? group : 1;
        for (int i = 0; i < GROUP_SIZE; i++) {
            ParseIpResult parseResult = parseIps(context, String.valueOf(assignGroupCount + i), isAppNameAware, mustSelectMoreOneReplicAtLeast);
            if (parseResult.valid && parseResult.ips.length > 0) {
                if ((parseResult.ips.length * MAX_SHARDS_PER_NODE) < (group * replica)) {
                    this.addErrorMessage(context, "您选择的机器节点不足,至少要" + ((group * replica) / MAX_SHARDS_PER_NODE) + "台机器");
                // if (replica < parseResult.ips.length) {
                // this.addErrorMessage(context, "第" + (assignGroupCount +
                // i)
                // + "组，" + setVerbs + "副本数目最大为" + replica + "台");
                } else {
                    // 每组有N个节点
                    for (int j = 0; j < parseResult.ips.length; j++) {
                        result.addNodeIps((assignGroupCount + i), parseResult.ips[j]);
                        // "replica:*,shard:shard1,node:10.1.21.233\\:8080_solr"
                        if (shardSpecificNode) {
                            result.addRule("replica:" + (j + 1) + ",shard:shard" + (i + 1) + ",tisnode:" + StringUtils.replace(parseResult.ips[j], ":", "\\:"));
                        }
                    }
                    // addserverSummary.append()
                    // this.addActionMessage(context, "第" + (assignGroupCount +
                    // i)
                    // + "组，" + setVerbs + "副本机为" + parseResult.ipLiteria);
                    this.addActionMessage(context, "选择的机器为:" + parseResult.ipLiteria);
                    result.setValid(true);
                }
            }
        }
        if (!result.isValid()) {
            this.addErrorMessage(context, "请至少为任何一组添加一个以上副本节点");
        }
        return result;
    }
    // private static abstract class ReplicaUpdate {
    // /**
    // * @param request
    // * @param replicCount
    // * 每组添加的副本数目
    // * @throws IOException
    // */
    // public abstract void update(CoreRequest request, short[] replicCount)
    // throws IOException;
    // 
    // public abstract String getExecuteLiteria();
    // 
    // public boolean shallContinueProcess(Context context, FCoreRequest
    // request) {
    // return true;
    // }
    // 
    // }
    // 
    // /**
    // * 更新组内数目
    // *
    // * @param context
    // * @throws Exception
    // */
    // @Func(PermissionConstant.APP_REPLICA_MANAGE)
    // public void doUpdateGroupCountSetp1(Context context) throws Exception {
    // Integer groupcount = this.getInt("addgroup");
    // if (groupcount == null || groupcount < 1) {
    // this.addErrorMessage(context, "请设置合法的Group数");
    // return;
    // }
    // if (isDumpWorking(context)) {
    // return;
    // }
    // 
    // // 校验是否是实时模式
    // if (isRealTimeModel(context)) {
    // return;
    // }
    // 
    // this.forward("addgroup");
    // 
    // LocatedCores locateCore = this.getClientProtocol().getCoreLocations(
    // this.getAppDomain().getAppName());
    // 
    // context.put("assignGroupCount", locateCore.getCores().size());
    // int serverSum = 0;
    // for (LocatedCore localtedCore : locateCore.getCores()) {
    // serverSum += localtedCore.getCoreNodeInfo().length;
    // }
    // int replica = 0;
    // try {
    // replica = (serverSum / locateCore.getCores().size());
    // } catch (Throwable e) {
    // 
    // }
    // 
    // if (replica < 1) {
    // this.addErrorMessage(context, "应用有异常，组内副本数部门小于1");
    // return;
    // }
    // 
    // context.put("assignreplica", replica);
    // 
    // context.put(CoreAction.CREATE_CORE_SELECT_COREINFO,
    // new CreateCorePageDTO(locateCore.getCores().size(), groupcount,
    // replica, false// locateCore.isMonopy()
    // ));
    // 
    // }
    // 
    // /**
    // * 更新组内数目
    // *
    // * @param context
    // * @throws Exception
    // */
    // @Func(PermissionConstant.APP_REPLICA_MANAGE)
    // public void doUpdateGroupCount(Context context) throws Exception {
    // Integer groupcount = this.getInt("group");
    // if (groupcount == null || groupcount < 1) {
    // this.addErrorMessage(context, "请设置合法的Group数");
    // return;
    // }
    // 
    // Integer assignGroupCount = this.getInt("assigngroup");
    // 
    // if (assignGroupCount == null) {
    // throw new IllegalArgumentException(
    // "assignGroupCount can not be null");
    // }
    // 
    // final Integer replica = this.getInt("replica");
    // 
    // if (replica == null) {
    // throw new IllegalArgumentException("replica can not be null");
    // }
    // 
    // if (isRealTimeModel(context)) {
    // return;
    // }
    // 
    // if (isDumpWorking(context)) {
    // return;
    // }
    // 
    // FCoreRequest result = createCoreRequest(context, assignGroupCount,
    // groupcount, replica, "添加",/* isAppNameAware */true);
    // 
    // if (!result.isValid()) {
    // return;
    // }
    // 
    // this.getClientProtocol().addCoreNums(result.getRequest(),
    // groupcount.shortValue(), result.getReplicCount());
    // 
    // this.addActionMessage(context, "添加" + groupcount + "组服务器成功!!!!!");
    // }
    // 
    // private boolean isRealTimeModel(Context context) throws Exception {
    // LocatedCores locateCore = this.getClientProtocol().getCoreLocations(
    // this.getAppDomain().getAppName());
    // 
    // if (Corenodemanage.isRealTime(locateCore)) {
    // this.addErrorMessage(context, "实时应用不能添加组");
    // return true;
    // }
    // 
    // return false;
    // }
    // 
    // /**
    // * 从应用core中删除一个服务器
    // *
    // * @param context
    // * @throws Exception
    // */
    // @Func(PermissionConstant.APP_SERVER_SET)
    // public void doDeleteServerFromCore(Context context) throws Exception {
    // 
    // Integer group = this.getInt("group");
    // 
    // if (group == null || group < 0) {
    // this.addErrorMessage(context, "请选择合法的Group");
    // return;
    // }
    // 
    // final String ip = this.getString("ip");
    // if (StringUtils.isEmpty(ip)) {
    // this.addErrorMessage(context, "请选择需要删除的服务IP地址");
    // return;
    // }
    // 
    // Matcher matcher = this.isValidIpPattern(ip);
    // if (!matcher.matches()) {
    // this.addErrorMessage(context, "IP:" + ip + "不符合格式规范");
    // return;
    // }
    // 
    // if (isDumpWorking(context)) {
    // return;
    // }
    // 
    // // 将正在运行的副本删除
    // this.getClientProtocol().unprotectProcessExcessReplication(
    // this.getServiceName(), group, matcher.group(1));
    // 
    // this.addActionMessage(context, "已经触发删除server为：" + ip + " 的第" + group
    // + "组服务器，页面状态需要稍后才能同步");
    // }
    // 
    // public void doStopOne(Context context) throws Exception {
    // int groupNum = this.getInt("groupNum");
    // int replicaNum = this.getInt("replicaNum");
    // boolean success = false;
    // try {
    // success = this.getClientProtocol().stopOne(
    // this.getAppDomain().getAppName(), groupNum, replicaNum);
    // } catch (Exception e) {
    // }
    // if (success) {
    // this.addActionMessage(context, "成功触发停止副本 : "
    // + this.getAppDomain().getAppName() + "-" + groupNum + "-"
    // + replicaNum + ", 正在结束进程 ,请稍后刷新页面查看副本状态");
    // } else {
    // this.addErrorMessage(context, "触发停止副本"
    // + this.getAppDomain().getAppName() + "-" + groupNum + "-"
    // + replicaNum + " 失败 , 中心节点有异常");
    // }
    // }
    // 
    // public void doStartOne(Context context) throws Exception {
    // int groupNum = this.getInt("groupNum");
    // int replicaNum = this.getInt("replicaNum");
    // boolean success = false;
    // try {
    // success = this.getClientProtocol().startOne(
    // this.getAppDomain().getAppName(), groupNum, replicaNum);
    // } catch (Exception e) {
    // }
    // if (success) {
    // this.addActionMessage(context, "成功触发开启副本 : "
    // + this.getAppDomain().getAppName() + "-" + groupNum + "-"
    // + replicaNum + ", 正在拉起进程 ,请稍后刷新页面查看副本状态");
    // } else {
    // this.addErrorMessage(context, "触发开启副本"
    // + this.getAppDomain().getAppName() + "-" + groupNum + "-"
    // + replicaNum + " 失败,中心节点有异常");
    // }
    // }
    // 
    // public void setCoreReplication(Context context) throws Exception {
    // Integer groupNum = this.getInt("groupNum");
    // Integer numReplica = this.getInt("numReplica");
    // CoreRequest request = createIps(context, this.getServiceName(), null);
    // boolean success = false;
    // try {
    // success = this.getClientProtocol().setCoreReplication(request,
    // groupNum.shortValue(), numReplica.shortValue());
    // } catch (Exception e) {
    // }
    // if (success) {
    // this.addActionMessage(context, "成功触发 : "
    // + this.getAppDomain().getAppName() + "-" + groupNum
    // + "设置副本数 : " + numReplica);
    // } else {
    // this.addErrorMessage(context, "触发"
    // + this.getAppDomain().getAppName() + "-" + groupNum
    // + "设置副本数 : " + numReplica + " 失败 , 请先检查是否成功执行全量等");
    // }
    // }
    // 
    // public void doCheckingCoreCompletion(Context context) throws Exception {
    // CheckingResponse response = this.getClientProtocol()
    // .checkingCoreCompletion(this.getServiceName());
    // if (response.isFinished()) {
    // this.addActionMessage(context, "成功创建应用 : "
    // + this.getAppDomain().getAppName() + ", 您可以在应用更新页进行操作了");
    // } else if (response.isFailed()) {
    // this.addErrorMessage(context, "创建应用 : "
    // + this.getAppDomain().getAppName() + " 失败,请联系终搜同学");
    // } else {
    // this.addErrorMessage(context, "processing");
    // }
    // }
}
