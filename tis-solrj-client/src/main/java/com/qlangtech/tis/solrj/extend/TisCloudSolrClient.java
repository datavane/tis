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

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.HttpClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.ExtendCloudSolrClient;
import org.apache.solr.client.solrj.impl.HttpClientUtil;
import org.apache.solr.client.solrj.io.Tuple;
import org.apache.solr.client.solrj.io.stream.CloudSolrStream;
import org.apache.solr.client.solrj.io.stream.ComplementStream;
import org.apache.solr.client.solrj.io.stream.InnerJoinStream;
import org.apache.solr.client.solrj.io.stream.LeftOuterJoinStream;
import org.apache.solr.client.solrj.io.stream.RankStream;
import org.apache.solr.client.solrj.io.stream.ReducerStream;
import org.apache.solr.client.solrj.io.stream.TupleStream;
import org.apache.solr.client.solrj.io.stream.UniqueStream;
import org.apache.solr.client.solrj.io.stream.expr.StreamFactory;
import org.apache.solr.common.params.CommonParams;
import com.qlangtech.tis.solrj.io.stream.ExtendCloudSolrStream;
import com.qlangtech.tis.solrj.io.stream.NotExistStream;
import com.qlangtech.tis.solrj.io.stream.expr.StreamFactoryWithClient;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TisCloudSolrClient extends AbstractTisCloudSolrClient {

    private static ExtendCloudSolrClient solrClient;

    private static final StreamFactory streamFactory
            = new StreamFactoryWithClient() //
            .withFunctionName("searchExtend", ExtendCloudSolrStream.class) //
            .withFunctionName("search", CloudSolrStream.class) //
            .withFunctionName("unique", UniqueStream.class) //
            .withFunctionName("top", RankStream.class) //
            .withFunctionName("group", ReducerStream.class) //
            .withFunctionName("innerJoin", InnerJoinStream.class) //
            .withFunctionName("leftOuterJoin", LeftOuterJoinStream.class) //
            .withFunctionName("complement", ComplementStream.class) //
            .withFunctionName("notExist", NotExistStream.class) //
            .withFunctionName("count", CountStream.class) //
            .withFunctionName("unique", UniqueStream.class);

    private static final MessageFormat STREAM_QUERY_FORMAT = new MessageFormat("searchExtend({0}, qt=/export, " + "_route_={1}, q=\"{2}\", fl=\"{3}\", sort=\"{4}\")");

    public TisCloudSolrClient(String zkHost) {
        this(zkHost, HttpClientUtil.createClient(null));
    }

    public TisCloudSolrClient(String zkHost, HttpClient httpClient) {
        super(httpClient, zkHost);
    }

    public TisCloudSolrClient(String zkHost, int socketTimeout, int connTimeout, int maxConnectionsPerHost, int maxConnections) {
        super(socketTimeout, connTimeout, maxConnectionsPerHost, maxConnections, zkHost);
    }

    @Override
    protected void createClient(HttpClient httpClient, String... zkHost) {
        if (zkHost.length != 1) {
            throw new IllegalArgumentException("zkHost.length shall be 1,but is " + zkHost.length);
        }
        if (solrClient == null) {
            synchronized (TisCloudSolrClient.class) {
                if (solrClient == null) {
                    solrClient = new ExtendCloudSolrClient(zkHost[0], httpClient);
                    streamFactory.withDefaultZkHost(zkHost[0]);
                    //((StreamFactoryWithClient) streamFactory).withExtendClient(solrClient);
                }
            }
        }
    // return solrClient;
    }

    /**
     * Query and stream response. 把solrQuery转成字符串的形式
     *
     * @param <T>
     *            the type parameter
     * @param collection
     *            the collection
     * @param query
     *            the query
     * @param routerId
     *            the router id
     * @param resultProcess
     *            the result process
     * @param clazz
     *            the clazz
     * @throws InvocationTargetException
     *             the invocation target exception
     * @throws IntrospectionException
     *             the introspection exception
     * @throws InstantiationException
     *             the instantiation exception
     * @throws IllegalAccessException
     *             the illegal access exception
     * @throws IOException
     *             the io exception
     */
    public <T> void queryAndStreamResponse(String collection, SolrQuery query, String routerId, final ResponseCallback<T> resultProcess, final Class<T> clazz) throws InvocationTargetException, IntrospectionException, InstantiationException, IllegalAccessException, IOException {
        List<String> sortList = query.getSorts().stream().map(sortClause -> sortClause.getItem() + " " + sortClause.getOrder()).collect(Collectors.toList());
        String streamQueryString = STREAM_QUERY_FORMAT.format(new Object[] { collection, routerId, query.get(CommonParams.Q), query.get(CommonParams.FL), StringUtils.join(sortList, ",") });
        queryAndStreamResponse(streamQueryString, clazz, resultProcess);
    }

    /**
     * Query and stream response. 在回调方法的process方法中对每个对象进行处理
     *
     * @param <T>
     *            the type parameter
     * @param query
     *            查询语句
     * @param clazz
     *            the clazz
     * @param responseCallback
     *            the 回调方法
     * @throws IOException
     *             the io exception
     * @throws IntrospectionException
     *             the introspection exception
     * @throws IllegalAccessException
     *             the illegal access exception
     * @throws InstantiationException
     *             the instantiation exception
     * @throws InvocationTargetException
     *             the invocation target exception
     */
    @SuppressWarnings("all")
    public <T> void queryAndStreamResponse(String query, final Class<T> clazz, final ResponseCallback<T> responseCallback)
            throws IOException, IntrospectionException, IllegalAccessException, InstantiationException, InvocationTargetException {
        Map<String, SchemaPojoMapping> setMethods = null;
        TupleStream tupleStream = streamFactory.constructStream(query);
        tupleStream.open();
        boolean isTuple = (clazz == Tuple.class);
        if (!isTuple) {
            BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            setMethods = new HashMap<>();
            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                if (!StringUtils.equals(propertyDescriptor.getName(), "class")) {
                    Method setterMethod = propertyDescriptor.getWriteMethod();
                    if (setterMethod != null) {
                        setMethods.put(propertyDescriptor.getName(), new SchemaPojoMapping(setterMethod, addUnderline(propertyDescriptor.getName()).toString()));
                    }
                }
            }
        }
        SchemaPojoMapping mapping = null;
        while (true) {
            Tuple tuple = tupleStream.read();
            if (tuple.EOF) {
                break;
            } else {
                if (isTuple) {
                    responseCallback.process((T) tuple);
                } else {
                    T pojo = clazz.newInstance();
                    for (String name : setMethods.keySet()) {
                        mapping = setMethods.get(name);
                        // String tupleKey = addUnderline(name).toString();
                        Object tupleValue = tuple.get(mapping.schemaFieldName);
                        if (tupleValue != null) {
                            mapping.pojoSetterMethod.invoke(pojo, tupleValue);
                        }
                    }
                    responseCallback.process(pojo);
                }
            }
        }
        tupleStream.close();
    }

    private class SchemaPojoMapping {

        private final Method pojoSetterMethod;

        private final String schemaFieldName;

        /**
         * @param pojoSetterMethod
         * @param schemaFieldName
         */
        public SchemaPojoMapping(Method pojoSetterMethod, String schemaFieldName) {
            super();
            this.pojoSetterMethod = pojoSetterMethod;
            this.schemaFieldName = schemaFieldName;
        }
    }

    /**
     * Gets stream query count. 获得流式查询的总数
     *
     * @param query
     *            the query
     * @return the stream query count
     * @throws IOException
     *             the io exception
     */
    public long getStreamQueryCount(String query) throws IOException {
        AtomicLong count = new AtomicLong();
        TupleStream tupleStream = streamFactory.constructStream(query);
        tupleStream.open();
        while (true) {
            Tuple tuple = tupleStream.read();
            if (tuple.EOF) {
                break;
            } else {
                count.getAndIncrement();
            }
        }
        return count.get();
    }

    @Override
    protected ExtendCloudSolrClient getClient() {
        return solrClient;
    }
    // private static final Logger log =
    // LoggerFactory.getLogger(TisCloudSolrClient.class);
    // private static ExtendCloudSolrClient solrClient;
    // private static ITracker tracker;
    // private static final StreamFactory streamFactory = new
    // StreamFactoryWithClient()
    // .withFunctionName("searchExtend", ExtendCloudSolrStream.class)
    // .withFunctionName("search",
    // CloudSolrStream.class).withFunctionName("unique",
    // UniqueStream.class)
    // .withFunctionName("top", RankStream.class).withFunctionName("group",
    // ReducerStream.class)
    // .withFunctionName("innerJoin", InnerJoinStream.class)
    // .withFunctionName("leftOuterJoin", LeftOuterJoinStream.class)
    // .withFunctionName("complement",
    // ComplementStream.class).withFunctionName("notExist",
    // NotExistStream.class)
    // .withFunctionName("count", CountStream.class).withFunctionName("unique",
    // UniqueStream.class);
    // private static final MessageFormat STREAM_QUERY_FORMAT = new
    // MessageFormat(
    // "searchExtend({0}, qt=/export, " + "_route_={1}, q=\"{2}\", fl=\"{3}\",
    // sort=\"{4}\")");
    // private TisCloudSolrClient(String zkHost, HttpClient httpClient) {
    // if (solrClient == null) {
    // synchronized (TisCloudSolrClient.class) {
    // if (solrClient == null) {
    // tracker = DefaultTracker.create();
    // solrClient = new ExtendCloudSolrClient(zkHost, httpClient);
    // streamFactory.withDefaultZkHost(zkHost);
    // ((StreamFactoryWithClient) streamFactory).withExtendClient(solrClient);
    // }
    // }
    // }
    // }
    //
    // /**
    // * @param zkHost
    // *
    // * socket timeout measured in ms, closes a socket if read takes
    // * longer than x ms to complete. throws
    // * java.net.SocketTimeoutException: Read timed out exception
    // * @param socketTimeout
    // * connection timeout measures in ms, closes a socket if connection
    // * cannot be established within x ms. with a
    // * java.net.SocketTimeoutException: Connection timed out
    // * @param connTimeout
    // * Maximum connections allowed per host
    // * @param maxConnectionsPerHost
    // * Maximum total connections allowed
    // * @param maxConnections
    // */
    // public TisCloudSolrClient(String zkHost //
    // , int socketTimeout, int connTimeout, int maxConnectionsPerHost, int
    // maxConnections) {
    // this(zkHost, createHttpClient(socketTimeout, connTimeout,
    // maxConnectionsPerHost, maxConnections));
    // }
    //
    // @SuppressWarnings("all")
    // private static HttpClient createHttpClient(int socketTimeout, int
    // connTimeout, int maxConnectionsPerHost,
    // int maxConnections) {
    // ModifiableSolrParams params = new ModifiableSolrParams();
    // params.set(HttpClientUtil.PROP_SO_TIMEOUT, socketTimeout);
    // params.set(HttpClientUtil.PROP_CONNECTION_TIMEOUT, connTimeout);
    // params.set(HttpClientUtil.PROP_MAX_CONNECTIONS_PER_HOST,
    // maxConnectionsPerHost);
    // params.set(HttpClientUtil.PROP_MAX_CONNECTIONS, maxConnections);
    // // HttpClientUtil.addRequestInterceptor(new HttpRequestInterceptor() {
    // // @Override
    // // public void process(HttpRequest request, HttpContext context) throws
    // // HttpException, IOException {
    // // Tracer tracer = Tracing.current().tracer();
    // // Span span = tracer.buildSpan("tisquery").startManual();
    // // Tags.COMPONENT.set(span, TIS_COMPONENT);
    // // span.setTag(Tags.HTTP_URL.getKey(),
    // request.getRequestLine().getUri());
    // // context.setAttribute(TRACER_SPAN, span);
    // // }
    // // });
    // CloseableHttpClient httpClient = HttpClientUtil.createClient(params, new
    // PoolingClientConnectionManager());
    // // if (httpClient instanceof DefaultHttpClient) {
    // // ((DefaultHttpClient) httpClient).addResponseInterceptor(new
    // // HttpResponseInterceptor() {
    // // @Override
    // // public void process(HttpResponse response, HttpContext context) throws
    // // HttpException, IOException {
    // // Span span = (Span) context.getAttribute(TRACER_SPAN);
    // // if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
    // // Config.STATUS.ERR.getType());
    // // }
    // // span.finish();
    // // }
    // // });
    // // } else {
    // // throw new IllegalStateException("httpClient is not type of
    // // 'DefaultHttpClient'");
    // // }
    //
    // return httpClient;
    // }
    //
    // public TisCloudSolrClient(String zkHost) {
    // this(zkHost, HttpClientUtil.createClient(null));
    // }
    //
    // public ExtendCloudSolrClient getExtendSolrClient() {
    // return solrClient;
    // }
    //
    // /**
    // * Query and stream response. 把solrQuery转成字符串的形式
    // *
    // * @param <T>
    // * the type parameter
    // * @param collection
    // * the collection
    // * @param query
    // * the query
    // * @param routerId
    // * the router id
    // * @param resultProcess
    // * the result process
    // * @param clazz
    // * the clazz
    // * @throws InvocationTargetException
    // * the invocation target exception
    // * @throws IntrospectionException
    // * the introspection exception
    // * @throws InstantiationException
    // * the instantiation exception
    // * @throws IllegalAccessException
    // * the illegal access exception
    // * @throws IOException
    // * the io exception
    // */
    // public <T> void queryAndStreamResponse(String collection, SolrQuery
    // query,
    // String routerId,
    // final ResponseCallback<T> resultProcess, final Class<T> clazz) throws
    // InvocationTargetException,
    // IntrospectionException, InstantiationException, IllegalAccessException,
    // IOException {
    //
    // List<String> sortList = query.getSorts().stream()
    // .map(sortClause -> sortClause.getItem() + " " +
    // sortClause.getOrder()).collect(Collectors.toList());
    // String streamQueryString = STREAM_QUERY_FORMAT.format(new Object[] {
    // collection, routerId,
    // query.get(CommonParams.Q), query.get(CommonParams.FL),
    // StringUtils.join(sortList, ",") });
    //
    // queryAndStreamResponse(streamQueryString, clazz, resultProcess);
    // }
    //
    // /**
    // * Query and stream response. 在回调方法的process方法中对每个对象进行处理
    // *
    // * @param <T>
    // * the type parameter
    // * @param query
    // * 查询语句
    // * @param clazz
    // * the clazz
    // * @param responseCallback
    // * the 回调方法
    // * @throws IOException
    // * the io exception
    // * @throws IntrospectionException
    // * the introspection exception
    // * @throws IllegalAccessException
    // * the illegal access exception
    // * @throws InstantiationException
    // * the instantiation exception
    // * @throws InvocationTargetException
    // * the invocation target exception
    // */
    // @SuppressWarnings("all")
    // public <T> void queryAndStreamResponse(String query, final Class<T>
    // clazz,
    // final ResponseCallback<T> responseCallback) throws IOException,
    // IntrospectionException,
    // IllegalAccessException, InstantiationException, InvocationTargetException
    // {
    //
    // Map<String, SchemaPojoMapping> setMethods = null;
    // TupleStream tupleStream = streamFactory.constructStream(query);
    // tupleStream.open();
    // boolean isTuple = (clazz == Tuple.class);
    // if (!isTuple) {
    // BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
    // PropertyDescriptor[] propertyDescriptors =
    // beanInfo.getPropertyDescriptors();
    // setMethods = new HashMap<>();
    // for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
    // if (!StringUtils.equals(propertyDescriptor.getName(), "class")) {
    // Method setterMethod = propertyDescriptor.getWriteMethod();
    // if (setterMethod != null) {
    // setMethods.put(propertyDescriptor.getName(), new
    // SchemaPojoMapping(setterMethod,
    // addUnderline(propertyDescriptor.getName()).toString()));
    // }
    // }
    // }
    // }
    // SchemaPojoMapping mapping = null;
    // while (true) {
    // Tuple tuple = tupleStream.read();
    // if (tuple.EOF) {
    // break;
    // } else {
    // if (isTuple) {
    // responseCallback.process((T) tuple);
    // } else {
    // T pojo = clazz.newInstance();
    // for (String name : setMethods.keySet()) {
    // mapping = setMethods.get(name);
    // // String tupleKey = addUnderline(name).toString();
    // Object tupleValue = tuple.get(mapping.schemaFieldName);
    // if (tupleValue != null) {
    // mapping.pojoSetterMethod.invoke(pojo, tupleValue);
    // }
    // }
    // responseCallback.process(pojo);
    // }
    // }
    // }
    // tupleStream.close();
    // }
    //
    // private class SchemaPojoMapping {
    // private final Method pojoSetterMethod;
    // private final String schemaFieldName;
    //
    // /**
    // * @param pojoSetterMethod
    // * @param schemaFieldName
    // */
    // public SchemaPojoMapping(Method pojoSetterMethod, String schemaFieldName)
    // {
    // super();
    // this.pojoSetterMethod = pojoSetterMethod;
    // this.schemaFieldName = schemaFieldName;
    // }
    // }
    //
    // /**
    // * Gets stream query count. 获得流式查询的总数
    // *
    // * @param query
    // * the query
    // * @return the stream query count
    // * @throws IOException
    // * the io exception
    // */
    // public long getStreamQueryCount(String query) throws IOException {
    // AtomicLong count = new AtomicLong();
    // TupleStream tupleStream = streamFactory.constructStream(query);
    // tupleStream.open();
    // while (true) {
    // Tuple tuple = tupleStream.read();
    // if (tuple.EOF) {
    // break;
    // } else {
    // count.getAndIncrement();
    // }
    // }
    // return count.get();
    // }
    //
    // public static StringBuffer addUnderline(String value) {
    // StringBuffer parsedName = new StringBuffer();
    // char[] nameAry = value.toCharArray();
    // boolean firstAppend = true;
    // for (int i = 0; i < nameAry.length; i++) {
    // if (Character.isUpperCase(nameAry[i])) {
    // if (firstAppend) {
    // parsedName.append(Character.toLowerCase(nameAry[i]));
    // firstAppend = false;
    // } else {
    // parsedName.append('_').append(Character.toLowerCase(nameAry[i]));
    // }
    // } else {
    // parsedName.append(nameAry[i]);
    // firstAppend = false;
    // // .append(Character.toLowerCase());
    // }
    // }
    // return parsedName;
    // }
    //
    // /**
    // * 反序列化bean
    // *
    // * @param clazz
    // * @param doc
    // * @return
    // */
    // public <T> T transferBean(final Class<T> clazz, SolrDocument doc) {
    // return solrClient.getBinder().getBean(clazz, doc);
    // }
    //
    // public QueryResponse mergeQuery(String collection, SolrQuery query)
    // throws
    // Exception {
    // return mergeQuery(collection, query, true /* setShards */);
    // }
    //
    // /**
    // *
    // * @param collection
    // * @param query
    // * @param setShards 是否要客户端准备好shared参数？
    // * @return
    // * @throws Exception
    // */
    // public QueryResponse mergeQuery(String collection, SolrQuery query,
    // boolean
    // setShards) throws Exception {
    // ISpan span = null;
    // try {
    // span = tracker.start(collection, query);
    // if (setShards) {
    // List<ZkCoreNodeProps> nodes = solrClient.getRequestShards(collection,
    // null/*
    // shardkey */);
    // StringBuffer shards = new StringBuffer();
    // for (ZkCoreNodeProps node : nodes) {
    // shards.append(node.getCoreUrl()).append(",");
    // }
    // if (log.isDebugEnabled()) {
    // log.debug("shard:" + shards);
    // }
    // // 为了让客户端查询数据的时候数据不抖动
    // query.set(ShardParams.SHARDS, shards.toString());
    // }
    // QueryResponse response = solrClient.query(collection, query,
    // METHOD.POST);
    // return response;
    // } catch (Exception e) {
    // span.error(e);
    // throw e;
    // } finally {
    // // this.finishTracer(span, null);
    // span.finish();
    // }
    // }
    //
    // /**
    // * 走merge查询（慎用）
    // *
    // * @param collection
    // * @param query
    // * @param clazz
    // * @return
    // * @throws Exception
    // */
    // public <T> SimpleQueryResult<T> mergeQuery(String collection, SolrQuery
    // query, Class<T> clazz) throws Exception {
    // QueryResponse response = mergeQuery(collection, query);
    // return new SimpleQueryResult<T>(response, response.getBeans(clazz),
    // response.getResults().getNumFound());
    // }
    //
    // public SolrZkClient getZkClient() {
    // solrClient.connect();
    // return solrClient.getZkStateReader().getZkClient();
    // }
    //
    // public TisZkClient getTisZkClient() {
    //
    // long start = System.currentTimeMillis();
    // try {
    // // System.out.println("start init TisZkClient");
    // solrClient.connect();
    // return solrClient.getTisZkClient();
    // } finally {
    // System.out.println("init TisZkClient consume:" + (System.currentTimeMillis() - start) + "ms");
    // }
    // }
}
