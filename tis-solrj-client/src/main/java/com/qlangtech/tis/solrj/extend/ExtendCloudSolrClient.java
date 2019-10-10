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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
//import java.io.IOException;
//import java.lang.reflect.Field;
//import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
//import java.util.HashMap;
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Map;
import java.util.Optional;
import java.util.Set;

//
import org.apache.http.client.HttpClient;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
//import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
//import org.apache.solr.client.solrj.impl.LBHttpSolrClient;
//import org.apache.solr.client.solrj.request.UpdateRequest;
//import org.apache.solr.client.solrj.response.UpdateResponse;
//import org.apache.solr.common.SolrException;
//import org.apache.solr.common.cloud.DocCollection;
//import org.apache.solr.common.cloud.DocRouter;
//import org.apache.solr.common.cloud.OnReconnect;
//import org.apache.solr.common.cloud.Slice;
//import org.apache.solr.common.cloud.SolrZkClient;
//import org.apache.solr.common.cloud.TisSolrZkClient;
//import org.apache.solr.common.cloud.ZkCoreNodeProps;
//import org.apache.solr.common.cloud.ZkStateReader;
//import org.apache.solr.common.cloud.ZooKeeperException;
//import org.apache.solr.common.params.ModifiableSolrParams;
//import org.apache.solr.common.params.ShardParams;
//import org.apache.zookeeper.KeeperException;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import com.qlangtech.tis.TisZkClient;
//import com.qlangtech.tis.solr.common.cloud.ZkRepeatClientConnectionStrategy;
import org.apache.solr.client.solrj.impl.LBHttpSolrClient;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.cloud.ClusterState;
import org.apache.solr.common.cloud.DocCollection;
import org.apache.solr.common.cloud.DocRouter;
import org.apache.solr.common.cloud.Replica;
import org.apache.solr.common.cloud.Slice;
import org.apache.solr.common.cloud.ZkCoreNodeProps;
import org.apache.solr.common.cloud.ZkNodeProps;
import org.apache.solr.common.cloud.ZkStateReader;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.ShardParams;

import com.qlangtech.tis.TisZkClient;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class ExtendCloudSolrClient extends CloudSolrClient {

	private static final long serialVersionUID = 1L;
	public static final String SINGLE_SLICE_QUERY = "single.slice.query";
	//
	// private static final long serialVersionUID = 1L;
	//
	// private static final Map<String, List<ZkCoreNodeProps>> /* shards */
	// collectionShardMap = new LinkedHashMap<>(200);
	//
	// private static final Logger logger =
	// LoggerFactory.getLogger(ExtendCloudSolrClient.class);
	//
	// private static final Field zkStateReaderField;
	static final Optional<String> emptyOptional = Optional.empty();

	//
	// static {
	// try {
	// zkStateReaderField =
	// CloudSolrClient.class.getDeclaredField("zkStateReader");
	// zkStateReaderField.setAccessible(true);
	// } catch (Exception e) {
	// throw new RuntimeException(e);
	// }
	// }
	//
	// private final List<OnReconnect> onReconnectListener = new ArrayList<>();
	//
	// private final List<OnReconnect> clusterStateChangeListener = new
	// ArrayList<OnReconnect>();
	//
	// private TisZkClient tisZkClient;

	//
	public ExtendCloudSolrClient(String zkHost, HttpClient httpClient) {
		super(new SolrClientBuilder(Collections.singletonList(zkHost)));
	}

	private static class SolrClientBuilder extends CloudSolrClient.Builder {
		public SolrClientBuilder(List<String> zkHosts) {
			super(zkHosts, emptyOptional);
			this.solrUrls = null;
		}
	}

	// @Override
	// @SuppressWarnings("all")
	// protected NamedList<Object> sendRequest(SolrRequest request, List<String>
	// collection)
	// throws SolrServerException, IOException {
	// SolrParams params = request.getParams();
	// if (params == null) {
	// params = new ModifiableSolrParams();
	// }
	// if (!params.getBool(SINGLE_SLICE_QUERY, false)) {
	// return super.sendRequest(request, collection);
	// }
	// // 这里是单组查询
	// if (params.get(ShardParams._ROUTE_) == null) {
	// throw new IllegalStateException("param " + ShardParams._ROUTE_ + " can
	// not be null");
	// }
	// List<ZkCoreNodeProps> nodes = getRequestShards(collection,
	// params.get(ShardParams._ROUTE_));
	// ZkCoreNodeProps n = null;
	// int nodeSize = nodes.size();
	// if (nodeSize > 0) {
	// if (nodeSize > 1) {
	// n = nodes.get((int) (Math.random() * nodeSize));
	// } else {
	// n = nodes.get(0);
	// }
	// LBHttpSolrClient.Req req = new LBHttpSolrClient.Req(request,
	// Arrays.asList(n.getCoreUrl()));
	// LBHttpSolrClient.Rsp rsp = this.getLbClient().request(req);
	// return rsp.getResponse();
	// }
	// throw new IllegalStateException("has any valid node match,nodes,size()="
	// + nodeSize);
	// }

	/**
	 * @param collection
	 * @return
	 */
	public List<ZkCoreNodeProps> getRequestShards(String collection, String sharedKey) {

		if (sharedKey != null) {
			throw new IllegalArgumentException("param sharedkey is not supported");
		}

		// Collection<Slice> slices = null;
		List<ZkCoreNodeProps> nodes = new ArrayList<>();
		this.connect();
		ZkStateReader zkStateReader = this.getZkStateReader();
		ClusterState clusterState = zkStateReader.getClusterState();
		Set<String> liveNodes = clusterState.getLiveNodes();
		DocCollection docCollection = this.getDocCollection(collection, null);
		// 有给定的sharedKey
		// if (StringUtils.isNotBlank(sharedKey)) {
		// // 这里slices的数量应该只为1
		// slices = docCollection.getRouter().getSearchSlices(sharedKey, null,
		// docCollection);
		// if (slices == null) {
		//
		// slices = clusterState.getActiveSlices(collection);
		//
		// for (String col : clusterState.getCollections()) {
		// if (col.equalsIgnoreCase(collection)) {
		// slices = clusterState.getActiveSlices(col);
		// break;
		// }
		// }
		// if (slices == null) {
		// throw new IllegalStateException("shared key:" + sharedKey +
		// ",routeSlices can not be null");
		// }
		// }
		// Slice slice = slices.iterator().next();
		// Collection<Replica> replicas = slice.getReplicas();
		//
		// Replica targetReplica = getTargetReplica(slice, sharedKey);
		// // }
		// if (targetReplica.getState() == Replica.State.ACTIVE &&
		// liveNodes.contains(targetReplica.getNodeName())) {
		// nodes.add(new ZkCoreNodeProps(targetReplica));
		// } else {
		// for (Replica replica : replicas) {
		// if (replica.getState() == Replica.State.ACTIVE &&
		// liveNodes.contains(replica.getNodeName())) {
		// nodes.add(new ZkCoreNodeProps(replica));
		// break;
		// }
		// }
		// }
		// if (nodes.size() == 1) {
		// return nodes;
		// }
		// }
		Map<String, List<ZkCoreNodeProps>> validReplica = new HashMap<>();
		Collection<Slice> targetSlices = docCollection.getSlices();
		// logger.debug("livenodes:" + Arrays.toString(liveNodes.toArray()));
		for (Slice slice : targetSlices) {
			List<ZkCoreNodeProps> sliceNodes = new LinkedList<>();
			validReplica.put(slice.getName(), sliceNodes);
			for (ZkNodeProps nodeProps : slice.getReplicasMap().values()) {
				ZkCoreNodeProps coreNodeProps = new ZkCoreNodeProps(nodeProps);
				if (!liveNodes.contains(coreNodeProps.getNodeName())
						|| Replica.State.getState(coreNodeProps.getState()) != Replica.State.ACTIVE) {
					// + ",active:" + coreNodeProps.getState());
					continue;
				}
				sliceNodes.add(coreNodeProps);
			}
			// logger.debug("sharedname:" + slice.getName() + ",count:"
			// + sliceNodes.size());
		}
		for (List<ZkCoreNodeProps> replicas : validReplica.values()) {
			Collections.shuffle(replicas);
			if (replicas.size() > 0) {
				nodes.add(replicas.iterator().next());
				continue;
			}
		}
		return nodes;
	}

	/**
	 * 根据路由键的hash取模之后定位到某一个副本上
	 *
	 * @param slice
	 * @param sharedKey
	 * @return
	 */
	// public static Replica getTargetReplica(Slice slice, String sharedKey) {
	// Collection<Replica> replicas = slice.getReplicas();
	// // String replicaKey = "core_node" + (Math.abs(sharedKey.hashCode() %
	// // replicas.size()) + 1);
	// final int choiceIndex = Math.abs(sharedKey.hashCode() % replicas.size());
	// int index = 0;
	// // slice.getReplicasMap().get(replicaKey);
	// Replica targetReplica = null;
	// for (Replica r : replicas) {
	// if (choiceIndex == (index++)) {
	// targetReplica = r;
	// break;
	// }
	// }
	// if (targetReplica == null) {
	// StringBuffer keys = new StringBuffer();
	// for (String k : slice.getReplicasMap().keySet()) {
	// keys.append(k).append(",");
	// }
	// throw new NullPointerException("choiceIndex:" + choiceIndex + ",keys:" +
	// keys.toString() + ",shareKey:"
	// + sharedKey + ",sharename:" + slice.getName());
	// }
	// return targetReplica;
	// }

	// @Override
	public DocCollection getDocCollection(String collection) {
		return super.getDocCollection(collection, null);
	}

	// public void addOnReconnect(OnReconnect event) {
	// onReconnectListener.add(event);
	// }
	//
	public UpdateResponse deleteById(String collection, final String id, final String shareid, final long version)
			throws SolrServerException, IOException {
		UpdateRequest req = new UpdateRequest() {

			private static final long serialVersionUID = 1L;

			public Map<String, LBHttpSolrClient.Req> getRoutes(DocRouter router, DocCollection col,
					Map<String, List<String>> urlMap, ModifiableSolrParams params, String idField) {
				Map<String, LBHttpSolrClient.Req> routes = new HashMap<>();
				Slice slice = router.getTargetSlice(null, null, shareid, null, col);
				List<String> urls = urlMap.get(slice.getName());
				String leaderUrl = urls.get(0);
				UpdateRequest urequest = new UpdateRequest();
				urequest.setParams(params);
				urequest.deleteById(id, shareid, (version));
				urequest.setParam(ShardParams._ROUTE_, shareid);
				urequest.setParam("_version_", String.valueOf(version));
				LBHttpSolrClient.Req request = new LBHttpSolrClient.Req(urequest, urls);
				routes.put(leaderUrl, request);
				return routes;
			}
		};
		req.deleteById(id, shareid, version);
		req.setCommitWithin(-1);
		return req.process(this, collection);
	}

	// private void setZKStateReader(ZkStateReader zk) {
	// // zkStateReader
	// try {
	// zkStateReaderField.set(this, zk);
	// } catch (Exception e) {
	// throw new RuntimeException(e);
	// }
	// }
	//
	//// public void addStateChageEvent(OnReconnect chageEvent) {
	//// clusterStateChangeListener.add(chageEvent);
	//// }
	//
	// @Override
	// public void connect() {
	// if (this.getZkStateReader() == null) {
	// synchronized (this) {
	// if (this.getZkStateReader() == null) {
	// ZkStateReader zk = null;
	// try {
	// // int zkClientConnectTimeout = getZkClientTimeout();
	// int zkClientConnectTimeout = 50000;
	// SolrZkClient zkClient = new TisSolrZkClient(this.getZkHost(),
	// zkClientConnectTimeout,
	// zkClientConnectTimeout, new ZkRepeatClientConnectionStrategy(), new
	// OnReconnect() {
	//
	// @Override
	// public void command() {
	// try {
	// for (OnReconnect onReconnect : onReconnectListener) {
	// onReconnect.command();
	// }
	// getZkStateReader().createClusterStateWatchersAndUpdate();
	// } catch (KeeperException e) {
	// logger.error("", e);
	// throw new ZooKeeperException(SolrException.ErrorCode.SERVER_ERROR, "",
	// e);
	// } catch (InterruptedException e) {
	// // Restore the interrupted status
	// Thread.currentThread().interrupt();
	// logger.error("", e);
	// throw new ZooKeeperException(SolrException.ErrorCode.SERVER_ERROR, "",
	// e);
	// }
	// }
	// });
	// setTisZkClient(new TisZkClient(zkClient, onReconnectListener));
	// // ==============================================================
	// zk = new ZkStateReader(zkClient) {
	//
	// @Override
	// public Object getUpdateLock() {
	// // 当集群状态发生变化可以进行监听
	// for (OnReconnect change : clusterStateChangeListener) {
	// change.command();
	// }
	// return super.getUpdateLock();
	// }
	// };
	// zk.createClusterStateWatchersAndUpdate();
	// // zkStateReader = zk;
	// setZKStateReader(zk);
	// } catch (InterruptedException e) {
	// zk.close();
	// Thread.currentThread().interrupt();
	// throw new ZooKeeperException(SolrException.ErrorCode.SERVER_ERROR, "",
	// e);
	// } catch (KeeperException e) {
	// zk.close();
	// throw new ZooKeeperException(SolrException.ErrorCode.SERVER_ERROR, "",
	// e);
	// } catch (Exception e) {
	// if (zk != null)
	// zk.close();
	// // underlying exception being thrown
	// throw e;
	// }
	// }
	// }
	// }
	// }
}
