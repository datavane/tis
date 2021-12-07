/**
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.qlangtech.tis.solrj.extend;

import com.google.common.base.Joiner;
import com.qlangtech.tis.solrj.extend.router.HashcodeRouter;
import com.qlangtech.tis.solrj.tracker.ISpan;
import com.qlangtech.tis.solrj.tracker.ITracker;
import com.qlangtech.tis.solrj.tracker.impl.DefaultTracker;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.HttpClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest.METHOD;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.beans.DocumentObjectBinder;
import org.apache.solr.client.solrj.impl.ExtendCloudSolrClient;
import org.apache.solr.client.solrj.impl.HttpClientUtil;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.cloud.DocRouter;
import org.apache.solr.common.cloud.SolrZkClient;
import org.apache.solr.common.cloud.ZkCoreNodeProps;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.ShardParams;
import org.apache.solr.common.params.UpdateParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public abstract class AbstractTisCloudSolrClient {

    private static final Logger log = LoggerFactory.getLogger(TisCloudSolrClient.class);

    static {
        // AbstractTisCloudSolrClient.initHashcodeRouter();
    }

    // private static ExtendCloudSolrClient solrClient;
    private static final ITracker tracker = DefaultTracker.create();

    protected AbstractTisCloudSolrClient(HttpClient httpClient, String... zkHost) {
        this.createClient(httpClient, zkHost);
    }

    public static void main(String[] args) {
        AbstractTisCloudSolrClient.initHashcodeRouter();
    }

    @SuppressWarnings("all")
    public static void initHashcodeRouter() {
        log.info("start init hashcode router ... ");
        System.out.println("start init hashcode router ... ");
        try {
            Field routeMapField = null;
            routeMapField = DocRouter.class.getDeclaredField("routerMap");
            routeMapField.setAccessible(true);
            Map<String, DocRouter> routerMap = (Map<String, DocRouter>) routeMapField.get(null);
            routerMap.put(HashcodeRouter.NAME, new HashcodeRouter());
            log.info("load hashcodeRouter success...");
            System.out.println("load hashcodeRouter success...");
            // routeMapField.set(null, Collections.unmodifiableMap(routerMap));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 创建一个客户端
     *
     * @param zkHost
     * @param httpClient
     * @return
     */
    protected abstract void createClient(HttpClient httpClient, String... zkHost);

    /**
     * 获取一个之前的客户端
     *
     * @return
     */
    protected abstract ExtendCloudSolrClient getClient();

    /**
     * @param zkHost                socket timeout measured in ms, closes a socket if read takes
     *                              longer than x ms to complete. throws
     *                              java.net.SocketTimeoutException: Read timed out exception
     * @param socketTimeout         connection timeout measures in ms, closes a socket if
     *                              connection cannot be established within x ms. with a
     *                              java.net.SocketTimeoutException: Connection timed out
     * @param connTimeout           Maximum connections allowed per host
     * @param maxConnectionsPerHost Maximum total connections allowed
     * @param maxConnections
     */
    public AbstractTisCloudSolrClient(int socketTimeout, int connTimeout, int maxConnectionsPerHost, int maxConnections, String... zkHost) {
        this(createHttpClient(socketTimeout, connTimeout, maxConnectionsPerHost, maxConnections), zkHost);
    }

    @SuppressWarnings("all")
    private static HttpClient createHttpClient(int socketTimeout, int connTimeout, int maxConnectionsPerHost, int maxConnections) {
        ModifiableSolrParams params = new ModifiableSolrParams();
        params.set(HttpClientUtil.PROP_SO_TIMEOUT, socketTimeout);
        params.set(HttpClientUtil.PROP_CONNECTION_TIMEOUT, connTimeout);
        params.set(HttpClientUtil.PROP_MAX_CONNECTIONS_PER_HOST, maxConnectionsPerHost);
        params.set(HttpClientUtil.PROP_MAX_CONNECTIONS, maxConnections);
        return HttpClientUtil.createClient(params);
        // CloseableHttpClient httpClient = HttpClientUtil.createClient(params);
        // return httpClient;
    }

    public ExtendCloudSolrClient getExtendSolrClient() {
        return getClient();
    }

    public static StringBuffer addUnderline(String value) {
        StringBuffer parsedName = new StringBuffer();
        char[] nameAry = value.toCharArray();
        boolean firstAppend = true;
        for (int i = 0; i < nameAry.length; i++) {
            if (Character.isUpperCase(nameAry[i])) {
                if (firstAppend) {
                    parsedName.append(Character.toLowerCase(nameAry[i]));
                    firstAppend = false;
                } else {
                    parsedName.append('_').append(Character.toLowerCase(nameAry[i]));
                }
            } else {
                parsedName.append(nameAry[i]);
                firstAppend = false;
                // .append(Character.toLowerCase());
            }
        }
        return parsedName;
    }

    /**
     * 反序列化bean
     *
     * @param clazz
     * @param doc
     * @return
     */
    public <T> T transferBean(final Class<T> clazz, SolrDocument doc) {
        return getClient().getBinder().getBean(clazz, doc);
    }

    public QueryResponse mergeQuery(String collection, SolrQuery query) throws Exception {
        return mergeQuery(collection, query, true);
    }

    /**
     * @param collection
     * @param query
     * @param setShards  是否要客户端准备好shared参数？
     * @return
     * @throws Exception
     */
    public QueryResponse mergeQuery(String collection, SolrQuery query, boolean setShards) throws Exception {
        ISpan span = null;
        try {
            span = tracker.start(collection, query);
            if (setShards) {
                List<ZkCoreNodeProps> nodes = getClient().getRequestShards(collection, null);
                StringBuffer shards = new StringBuffer();
                for (ZkCoreNodeProps node : nodes) {
                    shards.append(node.getCoreUrl()).append(",");
                }
                if (log.isDebugEnabled()) {
                    log.debug("shard:" + shards);
                }
                // 为了让客户端查询数据的时候数据不抖动
                query.set(ShardParams.SHARDS, shards.toString());
            }
            QueryResponse response = getClient().query(collection, query, METHOD.POST);
            return response;
        } catch (Exception e) {
            span.error(e);
            throw e;
        } finally {
            // this.finishTracer(span, null);
            span.finish();
        }
    }

    /**
     * 走merge查询（慎用）
     *
     * @param collection
     * @param query
     * @param clazz
     * @return
     * @throws Exception
     */
    public <T> SimpleQueryResult<T> mergeQuery(String collection, SolrQuery query, Class<T> clazz) throws Exception {
        QueryResponse response = mergeQuery(collection, query);
        return new SimpleQueryResult<T>(response, response.getBeans(clazz), response.getResults().getNumFound());
    }

    public SolrZkClient getZkClient() {
        getClient().connect();
        return getClient().getZkStateReader().getZkClient();
    }

    // public TisZkClient getTisZkClient() {
    // // long start = System.currentTimeMillis();
    // try {
    // // System.out.println("start init TisZkClient");
    // getClient().connect();
    // return getClient().getTisZkClient();
    // } finally {
    // // System.out.println("init TisZkClient consume:" +
    // (System.currentTimeMillis()
    // // - start) + "ms");
    // }
    // }
    // public void addOnReconnect(OnReconnect onReconnectEvent) {
    // //  getClient()
    // }
    public UpdateResponse add(String collection, SolrInputDocument doc, long timeVersion) throws SolrServerException {
        try {
            UpdateRequest req = new TisUpdateRequest();
            req.add(doc);
            req.setCommitWithin(-1);
            doc.setField("_version_", String.valueOf(timeVersion));
            req.setParam("_version_", String.valueOf(timeVersion));
            return req.process(getClient(), collection);
        } catch (IOException e) {
            throw new SolrServerException(e);
        }
    }

    /**
     * 添加一个列表
     *
     * @param collection
     * @param docs
     * @return
     * @throws SolrServerException
     */
    public UpdateResponse addList(String collection, Collection<SolrInputDocument> docs) throws SolrServerException {
        try {
            if (docs.size() > 2000) {
                throw new IllegalStateException("doc size can not big than 2000,size:" + docs.size());
            }
            UpdateRequest req = new TisUpdateRequest();
            req.add(docs);
            req.setCommitWithin(-1);
            return req.process(getClient(), collection);
        } catch (IOException e) {
            throw new SolrServerException(e);
        }
    }

    /**
     * @param collection
     * @param routerId   路由键
     * @param query
     * @param clazz
     * @return
     * @throws Exception
     */
    public <T> SimpleQueryResult<T> query(String collection, String routerId, SolrQuery query, Class<T> clazz) throws Exception {
        QueryResponse response = query(collection, routerId, query);
        return new SimpleQueryResult<T>(response, response.getBeans(clazz), response.getResults().getNumFound());
    }

    public QueryResponse query(String collection, String routerId, SolrQuery query) throws Exception {
        if (StringUtils.isBlank(routerId)) {
            throw new IllegalStateException("routerid can not be null");
        }
        ISpan span = null;
        try {
            span = tracker.start(collection, query);
            // span = startTracer(collection, query);
            // query.set(ShardParams.SHARDS, this
            // .getRequestShards(collection, routerId).toString());
            // 标示是单组查询
            query.set(ExtendCloudSolrClient.SINGLE_SLICE_QUERY, true);
            query.setDistrib(false);
            query.setParam(ShardParams._ROUTE_, routerId);
            QueryResponse response = getClient().query(collection, query, METHOD.POST);
            return response;
        } catch (Exception e) {
            // this.finishTracer(span, e);
            span.error(e);
            throw e;
        } finally {
            // this.finishTracer(span, null);
            span.finish();
        }
    }

    /**
     * 取得刚刚加入到服务端的更新记录
     *
     * @param collection
     * @param id
     * @return
     * @throws SolrServerException
     */
    public SolrDocument getById(String collection, String id, String routeValue) throws SolrServerException {
        ISpan span = null;
        try {
            SolrQuery query = new SolrQuery();
            query.setQuery("id:" + id);
            // span = startTracer(collection, query);
            span = tracker.start(collection, query);
            ModifiableSolrParams params = new ModifiableSolrParams();
            params.set(CommonParams.DISTRIB, false);
            params.set(ShardParams._ROUTE_, routeValue);
            params.set(UpdateParams.COLLECTION, collection);
            return getClient().getById(collection, id, params);
        } catch (SolrServerException e) {
            // this.finishTracer(span, e);
            span.error(e);
            throw e;
        } catch (IOException e) {
            span.error(e);
            // this.finishTracer(span, e);
            throw new SolrServerException(e);
        } finally {
            span.finish();
            // this.finishTracer(span, null);
        }
    }

    private static final Joiner IDS_JOINER = Joiner.on(",").skipNulls();

    /**
     * 取得多个document
     *
     * @param collection
     * @param ids
     * @param routeValue
     * @return
     * @throws SolrServerException
     */
    public SolrDocumentList getByIds(String collection, Collection<String> ids, String routeValue) throws SolrServerException {
        ISpan span = null;
        try {
            SolrQuery query = new SolrQuery();
            query.setQuery("id:" + IDS_JOINER.join(ids));
            span = tracker.start(collection, query);
            ModifiableSolrParams params = new ModifiableSolrParams();
            params.set(ShardParams._ROUTE_, routeValue);
            params.set(UpdateParams.COLLECTION, collection);
            return getClient().getById(collection, ids, params);
        } catch (SolrServerException e) {
            // this.finishTracer(span, e);
            span.error(e);
            throw e;
        } catch (IOException e) {
            span.error(e);
            // this.finishTracer(span, e);
            throw new SolrServerException(e);
        } finally {
            span.finish();
            // this.finishTracer(span, null);
        }
    }

    /**
     * 从多组上合并取得结果
     *
     * @param collection
     * @param ids
     * @return
     * @throws SolrServerException
     */
    public SolrDocumentList getMergeByIds(String collection, Collection<String> ids) throws SolrServerException {
        ISpan span = null;
        try {
            SolrQuery query = new SolrQuery();
            query.setQuery("ids:" + IDS_JOINER.join(ids));
            span = tracker.start(collection, query);
            return getClient().getById(collection, ids);
        } catch (SolrServerException e) {
            // this.finishTracer(span, e);
            span.error(e);
            throw e;
        } catch (IOException e) {
            span.error(e);
            // this.finishTracer(span, e);
            throw new SolrServerException(e);
        } finally {
            span.finish();
            // this.finishTracer(span, null);
        }
    }

    public void deleteById(String collection, String id, String shareid) throws SolrServerException {
        this.deleteById(collection, id, shareid, 0);
    }

    public void deleteById(String collection, String id, String shareid, long version) throws SolrServerException {
        if (StringUtils.isEmpty(collection)) {
            throw new IllegalStateException("param collection can not be null");
        }
        if (StringUtils.isBlank(id)) {
            throw new IllegalStateException("param id can not be null");
        }
        if (StringUtils.isBlank(shareid)) {
            throw new IllegalStateException("param shareid can not be null");
        }
        try {
            // String collection, String id, int commitWithinMs
            getClient().deleteById(collection, id, shareid, version);
        } catch (SolrServerException e) {
            throw e;
        } catch (IOException e) {
            new SolrServerException(e);
        }
    }

    public void deleteById(String collection, String id) throws SolrServerException {
        if (StringUtils.isEmpty(collection)) {
            throw new IllegalStateException("param collection can not be null");
        }
        if (StringUtils.isBlank(id)) {
            throw new IllegalStateException("param id can not be null");
        }
        try {
            getClient().deleteById(collection, id);
        } catch (SolrServerException e) {
            throw e;
        } catch (IOException e) {
            new SolrServerException(e);
        }
    }

    public <T> T getBeanById(String collection, String id, String routeValue, Class<T> clazz) throws SolrServerException {
        SolrDocument doc = getById(collection, id, routeValue);
        if (doc == null) {
            return null;
        }
        return getClient().getBinder().getBean(clazz, doc);
    }

    public DocumentObjectBinder getDocumentObjectBinder() {
        return getClient().getBinder();
    }

    /**
     * 提交commit生效
     *
     * @param collection
     * @return
     * @throws SolrServerException
     * @throws IOException
     */
    public UpdateResponse commit(String collection) throws SolrServerException, IOException {
        // boolean waitFlush, boolean waitSearcher,
        return getClient().commit(collection, true, true, true);
    }

    public UpdateResponse deleteByQuery(String collection, String shareid, String query) throws SolrServerException, IOException {
        // return solrClient.deleteByQuery(collection, query, 500/*ms*/);
        if (StringUtils.isBlank(shareid)) {
            throw new IllegalArgumentException("param shareid can not be null");
        }
        UpdateRequest req = new UpdateRequest();
        req.deleteByQuery(query);
        req.setCommitWithin(500);
        // req.setParam(param, value);
        req.setParam(ShardParams._ROUTE_, shareid);
        return req.process(getClient(), collection);
    }

    /**
     * 更新记录
     *
     * @param collection
     * @param doc
     * @param timeVersion
     * @return
     * @throws SolrServerException
     */
    public UpdateResponse update(String collection, SolrInputDocument doc, long timeVersion) throws SolrServerException {
        return add(collection, doc, timeVersion);
    }

    public interface ResponseCallback<T> {

        public void process(T pojo);

        public void lististInfo(long numFound, long start);
    }

    public static class SimpleQueryResult<T> {

        private final List<T> result;

        private final long numberFound;

        private final QueryResponse response;

        public SimpleQueryResult(QueryResponse response, List<T> result, long numberFound) {
            super();
            this.result = result;
            this.numberFound = numberFound;
            this.response = response;
        }

        public QueryResponse getResponse() {
            return response;
        }

        public List<T> getResult() {
            return result;
        }

        public long getNumberFound() {
            return this.numberFound;
        }
    }
}
