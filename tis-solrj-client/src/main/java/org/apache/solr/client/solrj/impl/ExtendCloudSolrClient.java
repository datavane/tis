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
package org.apache.solr.client.solrj.impl;

import org.apache.http.client.HttpClient;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.IsUpdateRequest;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.request.V2Request;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.client.solrj.routing.ReplicaListTransformer;
import org.apache.solr.client.solrj.routing.RequestReplicaListTransformerGenerator;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.cloud.*;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.ShardParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.StrUtils;
import org.apache.solr.common.util.Utils;
import java.io.IOException;
import java.util.*;
import static org.apache.solr.common.params.CommonParams.ADMIN_PATHS;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class ExtendCloudSolrClient extends CloudSolrClient {

    private static final long serialVersionUID = 1L;

    public static final String SINGLE_SLICE_QUERY = "single.slice.query";

    static final Optional<String> emptyOptional = Optional.empty();

    public ExtendCloudSolrClient(String zkHost, HttpClient httpClient) {
        super(new SolrClientBuilder(Collections.singletonList(zkHost)));
    }

    private static class SolrClientBuilder extends CloudSolrClient.Builder {

        public SolrClientBuilder(List<String> zkHosts) {
            super(zkHosts, emptyOptional);
            this.solrUrls = null;
        }
    }

    private Random rand = new Random();

    private RequestReplicaListTransformerGenerator requestRLTGenerator = new RequestReplicaListTransformerGenerator();

    /**
     * https://issues.apache.org/jira/browse/SOLR-11444
     * https://github.com/apache/lucene-solr/commit/e001f352895c83652c3cf31e3c724d29a46bb721#
     * 奇怪在solr8.0的时候把这块 逻辑给改了，导致在客户端设置_router_ ,distrbute=false 参数是想定位到服务端特定shared时候只能定位到node，
     * 然后在服务端端会随机选择一个shared（前提是在一个node中部署多个shared）进行查询，这样会造成客户端查询结果不正确
     *
     * @param request
     * @param inputCollections
     * @return
     * @throws SolrServerException
     * @throws IOException
     */
    @Override
    protected NamedList<Object> sendRequest(SolrRequest request, List<String> inputCollections) throws SolrServerException, IOException {
        connect();
        boolean sendToLeaders = false;
        boolean isUpdate = false;
        if (request instanceof IsUpdateRequest) {
            if (request instanceof UpdateRequest) {
                // }
                return super.sendRequest(request, inputCollections);
            }
            sendToLeaders = true;
        }
        SolrParams reqParams = request.getParams();
        if (reqParams == null) {
            // TODO fix getParams to never return null!
            reqParams = new ModifiableSolrParams();
        }
        ReplicaListTransformer replicaListTransformer = requestRLTGenerator.getReplicaListTransformer(reqParams);
        final Set<String> liveNodes = getClusterStateProvider().getLiveNodes();
        // we populate this as follows...
        final List<String> theUrlList = new ArrayList<>();
        if (request instanceof V2Request) {
            if (!liveNodes.isEmpty()) {
                List<String> liveNodesList = new ArrayList<>(liveNodes);
                Collections.shuffle(liveNodesList, rand);
                theUrlList.add(Utils.getBaseUrlForNodeName(liveNodesList.get(0), getClusterStateProvider().getClusterProperty(ZkStateReader.URL_SCHEME, "http")));
            }
        } else if (ADMIN_PATHS.contains(request.getPath())) {
            for (String liveNode : liveNodes) {
                theUrlList.add(Utils.getBaseUrlForNodeName(liveNode, getClusterStateProvider().getClusterProperty(ZkStateReader.URL_SCHEME, "http")));
            }
        } else {
            // Typical...
            // Set<String> collectionNames = resolveAliases(inputCollections, isUpdate);
            // baisui modify 先不需要别名处理
            // resolveAliases(inputCollections, isUpdate);
            List<String> collectionNames = inputCollections;
            if (collectionNames.isEmpty()) {
                throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "No collection param specified on request and no default collection has been set: " + inputCollections);
            }
            // TODO: not a big deal because of the caching, but we could avoid looking
            // at every shard when getting leaders if we tweaked some things
            // Retrieve slices from the cloud state and, for each collection specified, add it to the Map of slices.
            Map<String, Slice> slices = new HashMap<>();
            String shardKeys = reqParams.get(ShardParams._ROUTE_);
            for (String collectionName : collectionNames) {
                DocCollection col = getDocCollection(collectionName, null);
                if (col == null) {
                    throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "Collection not found: " + collectionName);
                }
                Collection<Slice> routeSlices = col.getRouter().getSearchSlices(shardKeys, reqParams, col);
                ClientUtils.addSlices(slices, collectionName, routeSlices, true);
            }
            // Gather URLs, grouped by leader or replica
            List<Replica> sortedReplicas = new ArrayList<>();
            List<Replica> replicas = new ArrayList<>();
            for (Slice slice : slices.values()) {
                Replica leader = slice.getLeader();
                for (Replica replica : slice.getReplicas()) {
                    String node = replica.getNodeName();
                    if (// Must be a live node to continue
                    !liveNodes.contains(node) || // Must be an ACTIVE replica to continue
                    replica.getState() != Replica.State.ACTIVE)
                        continue;
                    if (sendToLeaders && replica.equals(leader)) {
                        // put leaders here eagerly (if sendToLeader mode)
                        sortedReplicas.add(replica);
                    } else {
                        // replicas here
                        replicas.add(replica);
                    }
                }
            }
            // Sort the leader replicas, if any, according to the request preferences    (none if !sendToLeaders)
            replicaListTransformer.transform(sortedReplicas);
            // Sort the replicas, if any, according to the request preferences and append to our list
            replicaListTransformer.transform(replicas);
            sortedReplicas.addAll(replicas);
            String joinedInputCollections = StrUtils.join(inputCollections, ',');
            Set<String> seenNodes = new HashSet<>();
            sortedReplicas.forEach(replica -> {
                if (seenNodes.add(replica.getNodeName())) {
                    // baisui modify 还是向特定的core发送请求
                    theUrlList.add(replica.getCoreUrl());
                // theUrlList.add(ZkCoreNodeProps.getCoreUrl(replica.getBaseUrl(), joinedInputCollections));
                }
            });
            if (theUrlList.isEmpty()) {
                collectionStateCache.keySet().removeAll(collectionNames);
                throw new SolrException(SolrException.ErrorCode.INVALID_STATE, "Could not find a healthy node to handle the request.");
            }
        }
        LBSolrClient.Req req = new LBSolrClient.Req(request, theUrlList);
        LBSolrClient.Rsp rsp = getLbClient().request(req);
        return rsp.getResponse();
    }

    /**
     * @param collection
     * @return
     */
    public List<ZkCoreNodeProps> getRequestShards(String collection, String sharedKey) {
        if (sharedKey != null) {
            throw new IllegalArgumentException("param sharedkey is not supported");
        }
        List<ZkCoreNodeProps> nodes = new ArrayList<>();
        this.connect();
        ZkStateReader zkStateReader = this.getZkStateReader();
        ClusterState clusterState = zkStateReader.getClusterState();
        Set<String> liveNodes = clusterState.getLiveNodes();
        DocCollection docCollection = this.getDocCollection(collection, null);
        Map<String, List<ZkCoreNodeProps>> validReplica = new HashMap<>();
        Collection<Slice> targetSlices = docCollection.getSlices();
        for (Slice slice : targetSlices) {
            List<ZkCoreNodeProps> sliceNodes = new LinkedList<>();
            validReplica.put(slice.getName(), sliceNodes);
            for (ZkNodeProps nodeProps : slice.getReplicasMap().values()) {
                ZkCoreNodeProps coreNodeProps = new ZkCoreNodeProps(nodeProps);
                if (!liveNodes.contains(coreNodeProps.getNodeName()) || Replica.State.getState(coreNodeProps.getState()) != Replica.State.ACTIVE) {
                    // + ",active:" + coreNodeProps.getState());
                    continue;
                }
                sliceNodes.add(coreNodeProps);
            }
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

    public DocCollection getDocCollection(String collection) {
        return super.getDocCollection(collection, null);
    }

    public UpdateResponse deleteById(String collection, final String id, final String shareid, final long version) throws SolrServerException, IOException {
        UpdateRequest req = new UpdateRequest() {

            private static final long serialVersionUID = 1L;

            public Map<String, LBHttpSolrClient.Req> getRoutes(DocRouter router, DocCollection col, Map<String, List<String>> urlMap, ModifiableSolrParams params, String idField) {
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
    // // public void addStateChageEvent(OnReconnect chageEvent) {
    // // clusterStateChangeListener.add(chageEvent);
    // // }
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
