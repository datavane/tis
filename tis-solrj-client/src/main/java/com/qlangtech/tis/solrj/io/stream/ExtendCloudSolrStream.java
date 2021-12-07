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
package com.qlangtech.tis.solrj.io.stream;

import org.apache.solr.client.solrj.io.stream.CloudSolrStream;
import org.apache.solr.client.solrj.io.stream.expr.StreamExpression;
import org.apache.solr.client.solrj.io.stream.expr.StreamFactory;
import java.io.IOException;
import java.util.Map;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class ExtendCloudSolrStream extends CloudSolrStream {

    private static final long serialVersionUID = 1L;

    private Map<String, String> fieldMappingsCopy;

    public ExtendCloudSolrStream(StreamExpression expression, StreamFactory factory) throws IOException {
        super(expression, factory);
    // this.cloudSolrClient = ((StreamFactoryWithClient) factory).getExtendClient();
    }
    // @Override
    // public void open() throws IOException {
    // this.tuples = new TreeSet();
    // this.solrStreams = new ArrayList();
    // this.eofTuples = Collections.synchronizedMap(new HashMap());
    // this.cloudSolrClient.connect();
    // constructStreams();
    // openStreams();
    // }
    //
    // @Override
    // public void close() throws IOException {
    // if (solrStreams != null) {
    // for (TupleStream solrStream : solrStreams) {
    // solrStream.close();
    // }
    // }
    // }
    // @Override
    // protected void constructStreams() throws IOException {
    // try {
    // String sharedKey = params.get(ShardParams._ROUTE_);
    // if (sharedKey == null) {
    // throw new Exception("_route_ can not be null in params " + params.toString());
    // }
    // ZkStateReader zkStateReader = cloudSolrClient.getZkStateReader();
    // ClusterState clusterState = zkStateReader.getClusterState();
    // Set<String> liveNodes = clusterState.getLiveNodes();
    // DocCollection docCollection = ((ExtendCloudSolrClient) cloudSolrClient).getDocCollection(collection);
    // // System.out.println("Connected to zk an got cluster state.");
    // // 这里slices的数量应该只为1
    // Collection<Slice> slices = docCollection.getRouter().getSearchSlices(sharedKey, null, docCollection);
    // if (slices == null) {
    // // Try case insensitive match
    // //				for (String col : clusterState.getCollections()) {
    // //					if (col.equalsIgnoreCase(collection)) {
    // //						slices = clusterState.getActiveSlices(col);
    // //						break;
    // //					}
    // //				}
    // //if (slices == null) {
    // throw new Exception("Collection not found:" + this.collection);
    // //}
    // }
    // // We are the aggregator.
    // params.put("distrib", "false");
    // for (Slice slice : slices) {
    // Collection<Replica> replicas = slice.getReplicas();
    // List<Replica> shuffler = new ArrayList<>();
    // // String replicaKey = "core_node" +
    // // (Math.abs(sharedKey.hashCode() % replicas.size()) + 1);
    // // slice.getReplicasMap().get(replicaKey);
    // Replica targetReplica = ExtendCloudSolrClient.getTargetReplica(slice, sharedKey);
    // // }
    // if (targetReplica.getState() == Replica.State.ACTIVE
    // && liveNodes.contains(targetReplica.getNodeName())) {
    // shuffler.add(targetReplica);
    // } else {
    // for (Replica replica : replicas) {
    // if (replica.getState() == Replica.State.ACTIVE && liveNodes.contains(replica.getNodeName()))
    // shuffler.add(replica);
    // }
    // Collections.shuffle(shuffler, new Random());
    // }
    // Replica rep = shuffler.get(0);
    // ZkCoreNodeProps zkProps = new ZkCoreNodeProps(rep);
    // String url = zkProps.getCoreUrl();
    // SolrStream solrStream = new SolrStream(url, params);
    // if (streamContext != null) {
    // solrStream.setStreamContext(streamContext);
    // }
    // solrStream.setFieldMappings(this.fieldMappingsCopy);
    // solrStreams.add(solrStream);
    // }
    // } catch (Exception e) {
    // throw new IOException(e);
    // }
    // }
    //
    // private void openStreams() throws IOException {
    // ExecutorService service = ExecutorUtil
    // .newMDCAwareCachedThreadPool(new SolrjNamedThreadFactory("ExtendCloudSolrStream"));
    // try {
    // List<Future<TupleWrapper>> futures = new ArrayList();
    // for (TupleStream solrStream : solrStreams) {
    // StreamOpener so = new StreamOpener((SolrStream) solrStream, comp);
    // Future<TupleWrapper> future = service.submit(so);
    // futures.add(future);
    // }
    // try {
    // for (Future<TupleWrapper> f : futures) {
    // TupleWrapper w = f.get();
    // if (w != null) {
    // tuples.add(w);
    // }
    // }
    // } catch (Exception e) {
    // throw new IOException(e);
    // }
    // } finally {
    // service.shutdown();
    // }
    // }
    //
    // @Override
    // public void setFieldMappings(Map<String, String> fieldMappings) {
    // super.setFieldMappings(fieldMappings);
    // fieldMappingsCopy = fieldMappings;
    // }
}
