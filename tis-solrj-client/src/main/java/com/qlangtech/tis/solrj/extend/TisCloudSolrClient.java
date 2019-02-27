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

import com.qlangtech.tis.TisZkClient;
import com.qlangtech.tis.solrj.io.stream.ExtendCloudSolrStream;
import com.qlangtech.tis.solrj.io.stream.NotExistStream;
import com.qlangtech.tis.solrj.io.stream.expr.StreamFactoryWithClient;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TisCloudSolrClient extends AbstractTisCloudSolrClient {

	private static ExtendCloudSolrClient solrClient;

	private static final StreamFactory streamFactory = new StreamFactoryWithClient()
			.withFunctionName("searchExtend", ExtendCloudSolrStream.class)
			.withFunctionName("search", CloudSolrStream.class).withFunctionName("unique", UniqueStream.class)
			.withFunctionName("top", RankStream.class).withFunctionName("group", ReducerStream.class)
			.withFunctionName("innerJoin", InnerJoinStream.class)
			.withFunctionName("leftOuterJoin", LeftOuterJoinStream.class)
			.withFunctionName("complement", ComplementStream.class).withFunctionName("notExist", NotExistStream.class)
			.withFunctionName("count", CountStream.class).withFunctionName("unique", UniqueStream.class);

	private static final MessageFormat STREAM_QUERY_FORMAT = new MessageFormat(
			"searchExtend({0}, qt=/export, " + "_route_={1}, q=\"{2}\", fl=\"{3}\", sort=\"{4}\")");

	public TisCloudSolrClient(String zkHost) {
		this(zkHost, HttpClientUtil.createClient(null));
	}

	public TisCloudSolrClient(String zkHost, HttpClient httpClient) {
		super(httpClient, zkHost);
	}

	public TisCloudSolrClient(String zkHost, int socketTimeout, int connTimeout, int maxConnectionsPerHost,
			int maxConnections) {
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
					((StreamFactoryWithClient) streamFactory).withExtendClient(solrClient);
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
	public <T> void queryAndStreamResponse(String collection, SolrQuery query, String routerId,
			final ResponseCallback<T> resultProcess, final Class<T> clazz) throws InvocationTargetException,
			IntrospectionException, InstantiationException, IllegalAccessException, IOException {
		List<String> sortList = query.getSorts().stream()
				.map(sortClause -> sortClause.getItem() + " " + sortClause.getOrder()).collect(Collectors.toList());
		String streamQueryString = STREAM_QUERY_FORMAT.format(new Object[] { collection, routerId,
				query.get(CommonParams.Q), query.get(CommonParams.FL), StringUtils.join(sortList, ",") });
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
	public <T> void queryAndStreamResponse(String query, final Class<T> clazz,
			final ResponseCallback<T> responseCallback) throws IOException, IntrospectionException,
			IllegalAccessException, InstantiationException, InvocationTargetException {
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
						setMethods.put(propertyDescriptor.getName(), new SchemaPojoMapping(setterMethod,
								addUnderline(propertyDescriptor.getName()).toString()));
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
	public TisZkClient getTisZkClient() {

		long start = System.currentTimeMillis();
		try {
			// System.out.println("start init TisZkClient");
			solrClient.connect();
			return solrClient.getTisZkClient();
		} finally {
			System.out.println("init TisZkClient consume:" + (System.currentTimeMillis() - start) + "ms");
		}
	}
	//
	// public void addOnReconnect(OnReconnect onReconnectEvent) {
	// solrClient.addOnReconnect(onReconnectEvent);
	// }
	//
	// public UpdateResponse add(String collection, SolrInputDocument doc, long
	// timeVersion) throws SolrServerException {
	// try {
	// UpdateRequest req = new TisUpdateRequest();
	// req.add(doc);
	// req.setCommitWithin(-1);
	// doc.setField("_version_", String.valueOf(timeVersion));
	// req.setParam("_version_", String.valueOf(timeVersion));
	// return req.process(solrClient, collection);
	// } catch (IOException e) {
	// throw new SolrServerException(e);
	// }
	// }
	//
	// /**
	// * 添加一个列表
	// *
	// * @param collection
	// * @param docs
	// * @return
	// * @throws SolrServerException
	// */
	// public UpdateResponse addList(String collection,
	// Collection<SolrInputDocument> docs) throws SolrServerException {
	// try {
	// if (docs.size() > 2000) {
	// throw new IllegalStateException("doc size can not big than 2000,size:" +
	// docs.size());
	// }
	// UpdateRequest req = new TisUpdateRequest();
	// req.add(docs);
	// req.setCommitWithin(-1);
	// // doc.setField("_version_", String.valueOf(timeVersion));
	// // req.setParam("_version_", String.valueOf(timeVersion));
	//
	// return req.process(solrClient, collection);
	// } catch (IOException e) {
	// throw new SolrServerException(e);
	// }
	// }
	//
	// /**
	// * @param collection
	// * @param routerId
	// * 路由键
	// * @param query
	// * @param clazz
	// * @return
	// * @throws Exception
	// */
	// public <T> SimpleQueryResult<T> query(String collection, String routerId,
	// SolrQuery query, Class<T> clazz)
	// throws Exception {
	//
	// QueryResponse response = query(collection, routerId, query);
	// return new SimpleQueryResult<T>(response, response.getBeans(clazz),
	// response.getResults().getNumFound());
	// }
	//
	// public QueryResponse query(String collection, String routerId, SolrQuery
	// query) throws Exception {
	// if (StringUtils.isBlank(routerId)) {
	// throw new IllegalStateException("routerid can not be null");
	// }
	// ISpan span = null;
	// try {
	// span = tracker.start(collection, query);
	// // span = startTracer(collection, query);
	// // query.set(ShardParams.SHARDS, this
	// // .getRequestShards(collection, routerId).toString());
	// // 标示是单组查询
	// query.set(ExtendCloudSolrClient.SINGLE_SLICE_QUERY, true);
	// query.setDistrib(false);
	// query.setParam(ShardParams._ROUTE_, routerId);
	// QueryResponse response = solrClient.query(collection, query,
	// METHOD.POST);
	// return response;
	// } catch (Exception e) {
	// // this.finishTracer(span, e);
	// span.error(e);
	// throw e;
	// } finally {
	// // this.finishTracer(span, null);
	// span.finish();
	// }
	// }
	//
	// /**
	// * 取得刚刚加入到服务端的更新记录
	// *
	// * @param collection
	// * @param id
	// * @return
	// * @throws SolrServerException
	// */
	// public SolrDocument getById(String collection, String id, String
	// routeValue)
	// throws SolrServerException {
	// ISpan span = null;
	// try {
	// SolrQuery query = new SolrQuery();
	// query.setQuery("id:" + id);
	// // span = startTracer(collection, query);
	// span = tracker.start(collection, query);
	// ModifiableSolrParams params = new ModifiableSolrParams();
	// params.set(ShardParams._ROUTE_, routeValue);
	// params.set(UpdateParams.COLLECTION, collection);
	//
	// return solrClient.getById(collection, id, params);
	// } catch (SolrServerException e) {
	// // this.finishTracer(span, e);
	// span.error(e);
	// throw e;
	// } catch (IOException e) {
	// span.error(e);
	// // this.finishTracer(span, e);
	// throw new SolrServerException(e);
	// } finally {
	// span.finish();
	// // this.finishTracer(span, null);
	// }
	// }
	//
	// private static final Joiner IDS_JOINER = Joiner.on(",").skipNulls();
	//
	// /**
	// * 取得多个document
	// *
	// * @param collection
	// * @param ids
	// * @param routeValue
	// * @return
	// * @throws SolrServerException
	// */
	// public SolrDocumentList getByIds(String collection, Collection<String>
	// ids,
	// String routeValue)
	// throws SolrServerException {
	// ISpan span = null;
	// try {
	// SolrQuery query = new SolrQuery();
	// query.setQuery("id:" + IDS_JOINER.join(ids));
	// span = tracker.start(collection, query);
	// ModifiableSolrParams params = new ModifiableSolrParams();
	// params.set(ShardParams._ROUTE_, routeValue);
	// params.set(UpdateParams.COLLECTION, collection);
	//
	// return solrClient.getById(collection, ids, params);
	// } catch (SolrServerException e) {
	// // this.finishTracer(span, e);
	// span.error(e);
	// throw e;
	// } catch (IOException e) {
	// span.error(e);
	// // this.finishTracer(span, e);
	// throw new SolrServerException(e);
	// } finally {
	// span.finish();
	// // this.finishTracer(span, null);
	// }
	// }
	//
	// /**
	// * 从多组上合并取得结果
	// *
	// * @param collection
	// * @param ids
	// * @return
	// * @throws SolrServerException
	// */
	// public SolrDocumentList getMergeByIds(String collection,
	// Collection<String>
	// ids) throws SolrServerException {
	// ISpan span = null;
	// try {
	// SolrQuery query = new SolrQuery();
	// query.setQuery("ids:" + IDS_JOINER.join(ids));
	// span = tracker.start(collection, query);
	// return solrClient.getById(collection, ids);
	// } catch (SolrServerException e) {
	// // this.finishTracer(span, e);
	// span.error(e);
	// throw e;
	// } catch (IOException e) {
	// span.error(e);
	// // this.finishTracer(span, e);
	// throw new SolrServerException(e);
	// } finally {
	// span.finish();
	// // this.finishTracer(span, null);
	// }
	// }
	//
	// public void deleteById(String collection, String id, String shareid)
	// throws
	// SolrServerException {
	// this.deleteById(collection, id, shareid, 0);
	// }
	//
	// public void deleteById(String collection, String id, String shareid, long
	// version) throws SolrServerException {
	// if (StringUtils.isEmpty(collection)) {
	// throw new IllegalStateException("param collection can not be null");
	// }
	// if (StringUtils.isBlank(id)) {
	// throw new IllegalStateException("param id can not be null");
	// }
	// if (StringUtils.isBlank(shareid)) {
	// throw new IllegalStateException("param shareid can not be null");
	// }
	// try {
	// solrClient.deleteById(collection, id, shareid, version);
	// } catch (SolrServerException e) {
	// throw e;
	// } catch (IOException e) {
	// new SolrServerException(e);
	// }
	// }
	//
	// public void deleteById(String collection, String id) throws
	// SolrServerException {
	// if (StringUtils.isEmpty(collection)) {
	// throw new IllegalStateException("param collection can not be null");
	// }
	// if (StringUtils.isBlank(id)) {
	// throw new IllegalStateException("param id can not be null");
	// }
	//
	// try {
	// solrClient.deleteById(collection, id);
	// } catch (SolrServerException e) {
	// throw e;
	// } catch (IOException e) {
	// new SolrServerException(e);
	// }
	// }
	//
	// public <T> T getBeanById(String collection, String id, String routeValue,
	// Class<T> clazz)
	// throws SolrServerException {
	// SolrDocument doc = getById(collection, id, routeValue);
	// if (doc == null) {
	// return null;
	// }
	// return solrClient.getBinder().getBean(clazz, doc);
	// }
	//
	// public DocumentObjectBinder getDocumentObjectBinder() {
	// return solrClient.getBinder();
	// }
	//
	// /**
	// * 提交commit生效
	// *
	// * @param collection
	// * @return
	// * @throws SolrServerException
	// * @throws IOException
	// */
	// public UpdateResponse commit(String collection) throws
	// SolrServerException,
	// IOException {
	// // boolean waitFlush, boolean waitSearcher,
	// return solrClient.commit(collection, true, true, true);
	// }
	//
	// public UpdateResponse deleteByQuery(String collection, String shareid,
	// String
	// query)
	// throws SolrServerException, IOException {
	// // return solrClient.deleteByQuery(collection, query, 500/*ms*/);
	// if (StringUtils.isBlank(shareid)) {
	// throw new IllegalArgumentException("param shareid can not be null");
	// }
	// UpdateRequest req = new UpdateRequest();
	// req.deleteByQuery(query);
	// req.setCommitWithin(500/* ms */);
	// // req.setParam(param, value);
	//
	// req.setParam(ShardParams._ROUTE_, shareid);
	// return req.process(solrClient, collection);
	// }
	//
	// /**
	// * 更新记录
	// *
	// * @param collection
	// * @param doc
	// * @param timeVersion
	// * @return
	// * @throws SolrServerException
	// */
	// public UpdateResponse update(String collection, SolrInputDocument doc,
	// long
	// timeVersion)
	// throws SolrServerException {
	// return add(collection, doc, timeVersion);
	// }
	//
	// public interface ResponseCallback<T> {
	//
	// public void process(T pojo);
	//
	// public void lististInfo(long numFound, long start);
	// }
	//
	// public static class SimpleQueryResult<T> {
	// private final List<T> result;
	// private final long numberFound;
	// private final QueryResponse response;
	//
	// public SimpleQueryResult(QueryResponse response, List<T> result, long
	// numberFound) {
	// super();
	// this.result = result;
	// this.numberFound = numberFound;
	// this.response = response;
	// }
	//
	// public QueryResponse getResponse() {
	// return response;
	// }
	//
	// public List<T> getResult() {
	// return result;
	// }
	//
	// public long getNumberFound() {
	// return this.numberFound;
	// }
	//
	// }
}
