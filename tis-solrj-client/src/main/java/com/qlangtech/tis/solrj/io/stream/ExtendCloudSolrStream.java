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
package com.qlangtech.tis.solrj.io.stream;

//import com.qlangtech.tis.solrj.extend.ExtendCloudSolrClient;
import com.qlangtech.tis.solrj.io.stream.expr.StreamFactoryWithClient;
import org.apache.solr.client.solrj.io.stream.CloudSolrStream;
//import org.apache.solr.client.solrj.io.stream.SolrStream;
//import org.apache.solr.client.solrj.io.stream.TupleStream;
import org.apache.solr.client.solrj.io.stream.expr.StreamExpression;
import org.apache.solr.client.solrj.io.stream.expr.StreamFactory;
//import org.apache.solr.common.cloud.ClusterState;
//import org.apache.solr.common.cloud.DocCollection;
//import org.apache.solr.common.cloud.Replica;
//import org.apache.solr.common.cloud.Slice;
//import org.apache.solr.common.cloud.ZkCoreNodeProps;
//import org.apache.solr.common.cloud.ZkStateReader;
//import org.apache.solr.common.params.ShardParams;
//import org.apache.solr.common.util.ExecutorUtil;
//import org.apache.solr.common.util.SolrjNamedThreadFactory;
import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.List;
import java.util.Map;
//import java.util.Random;
//import java.util.Set;
//import java.util.TreeSet;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Future;

/*
 * 
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class ExtendCloudSolrStream extends CloudSolrStream {

	private static final long serialVersionUID = 1L;
	private Map<String, String> fieldMappingsCopy;

	public ExtendCloudSolrStream(StreamExpression expression, StreamFactory factory) throws IOException {
		super(expression, factory);
		this.cloudSolrClient = ((StreamFactoryWithClient) factory).getExtendClient();
	}

//	@Override
//	public void open() throws IOException {
//		this.tuples = new TreeSet();
//		this.solrStreams = new ArrayList();
//		this.eofTuples = Collections.synchronizedMap(new HashMap());
//		this.cloudSolrClient.connect();
//		constructStreams();
//		openStreams();
//	}
//
//	@Override
//	public void close() throws IOException {
//		if (solrStreams != null) {
//			for (TupleStream solrStream : solrStreams) {
//				solrStream.close();
//			}
//		}
//	}

//	@Override
//	protected void constructStreams() throws IOException {
//		try {
//			String sharedKey = params.get(ShardParams._ROUTE_);
//			if (sharedKey == null) {
//				throw new Exception("_route_ can not be null in params " + params.toString());
//			}
//			ZkStateReader zkStateReader = cloudSolrClient.getZkStateReader();
//			ClusterState clusterState = zkStateReader.getClusterState();
//			Set<String> liveNodes = clusterState.getLiveNodes();
//			DocCollection docCollection = ((ExtendCloudSolrClient) cloudSolrClient).getDocCollection(collection);
//			// System.out.println("Connected to zk an got cluster state.");
//			// 这里slices的数量应该只为1
//			Collection<Slice> slices = docCollection.getRouter().getSearchSlices(sharedKey, null, docCollection);
//			if (slices == null) {
//				// Try case insensitive match
////				for (String col : clusterState.getCollections()) {
////					if (col.equalsIgnoreCase(collection)) {
////						slices = clusterState.getActiveSlices(col);
////						break;
////					}
////				}
//				//if (slices == null) {
//					throw new Exception("Collection not found:" + this.collection);
//				//}
//			}
//			// We are the aggregator.
//			params.put("distrib", "false");
//			for (Slice slice : slices) {
//				Collection<Replica> replicas = slice.getReplicas();
//				List<Replica> shuffler = new ArrayList<>();
//				// String replicaKey = "core_node" +
//				// (Math.abs(sharedKey.hashCode() % replicas.size()) + 1);
//				// slice.getReplicasMap().get(replicaKey);
//				Replica targetReplica = ExtendCloudSolrClient.getTargetReplica(slice, sharedKey);
//				// }
//				if (targetReplica.getState() == Replica.State.ACTIVE
//						&& liveNodes.contains(targetReplica.getNodeName())) {
//					shuffler.add(targetReplica);
//				} else {
//					for (Replica replica : replicas) {
//						if (replica.getState() == Replica.State.ACTIVE && liveNodes.contains(replica.getNodeName()))
//							shuffler.add(replica);
//					}
//					Collections.shuffle(shuffler, new Random());
//				}
//				Replica rep = shuffler.get(0);
//				ZkCoreNodeProps zkProps = new ZkCoreNodeProps(rep);
//				String url = zkProps.getCoreUrl();
//				SolrStream solrStream = new SolrStream(url, params);
//				if (streamContext != null) {
//					solrStream.setStreamContext(streamContext);
//				}
//				solrStream.setFieldMappings(this.fieldMappingsCopy);
//				solrStreams.add(solrStream);
//			}
//		} catch (Exception e) {
//			throw new IOException(e);
//		}
//	}
//
//	private void openStreams() throws IOException {
//		ExecutorService service = ExecutorUtil
//				.newMDCAwareCachedThreadPool(new SolrjNamedThreadFactory("ExtendCloudSolrStream"));
//		try {
//			List<Future<TupleWrapper>> futures = new ArrayList();
//			for (TupleStream solrStream : solrStreams) {
//				StreamOpener so = new StreamOpener((SolrStream) solrStream, comp);
//				Future<TupleWrapper> future = service.submit(so);
//				futures.add(future);
//			}
//			try {
//				for (Future<TupleWrapper> f : futures) {
//					TupleWrapper w = f.get();
//					if (w != null) {
//						tuples.add(w);
//					}
//				}
//			} catch (Exception e) {
//				throw new IOException(e);
//			}
//		} finally {
//			service.shutdown();
//		}
//	}
//
//	@Override
//	public void setFieldMappings(Map<String, String> fieldMappings) {
//		super.setFieldMappings(fieldMappings);
//		fieldMappingsCopy = fieldMappings;
//	}
}
