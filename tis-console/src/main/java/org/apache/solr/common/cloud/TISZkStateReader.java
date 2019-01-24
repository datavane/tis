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
package org.apache.solr.common.cloud;

import static java.util.Arrays.asList;
import static java.util.Collections.EMPTY_MAP;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;
import static org.apache.solr.common.cloud.ZkStateReader.AUTO_ADD_REPLICAS;
import static org.apache.solr.common.cloud.ZkStateReader.CLUSTER_PROPS;
import static org.apache.solr.common.cloud.ZkStateReader.CLUSTER_STATE;
import static org.apache.solr.common.cloud.ZkStateReader.COLLECTIONS_ZKNODE;
import static org.apache.solr.common.cloud.ZkStateReader.LEGACY_CLOUD;
import static org.apache.solr.common.cloud.ZkStateReader.LIVE_NODES_ZKNODE;
import static org.apache.solr.common.cloud.ZkStateReader.SOLR_SECURITY_CONF_PATH;
import static org.apache.solr.common.cloud.ZkStateReader.URL_SCHEME;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrException.ErrorCode;
import org.apache.solr.common.util.Utils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.qlangtech.tis.coredefine.module.control.SelectableServer.CoreNode;
import com.qlangtech.tis.manage.common.ConfigFileContext;
import com.qlangtech.tis.manage.common.HttpUtils;
import com.qlangtech.tis.pubhook.common.RunEnvironment;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TISZkStateReader {

	public static final String LEADER_ELECT_ZKNODE = "leader_elect";

	public static final String SHARD_LEADERS_ZKNODE = "leaders";

	public static final String ELECTION_NODE = "election";

	public static final Set<String> KNOWN_CLUSTER_PROPS = unmodifiableSet(
			new HashSet<>(asList(LEGACY_CLOUD, URL_SCHEME, AUTO_ADD_REPLICAS)));

	private static final Logger LOG = LoggerFactory.getLogger(TISZkStateReader.class);

	private static final int GET_LEADER_RETRY_INTERVAL_MS = 50;

	private static final int GET_LEADER_RETRY_DEFAULT_TIMEOUT = 4000;

	private final Map<String, CoreNode> selectTableNodesMap = new HashMap<>();

	private static final MessageFormat GET_LUCENE_VER_RUL_FORMAT = new MessageFormat("http://{0}:8080/solr/version");

	/**
	 * Collections we actively care about, and will try to keep watch on.
	 */
	private final Set<String> interestingCollections = Collections.newSetFromMap(new ConcurrentHashMap<>());

	/**
	 * Collections with format2 state.json, "interesting" and actively watched.
	 */
	private final ConcurrentHashMap<String, DocCollection> watchedCollectionStates = new ConcurrentHashMap<>();

	/**
	 * Collections with format2 state.json, not "interesting" and not actively
	 * watched.
	 */
	private final ConcurrentHashMap<String, LazyCollectionRef> lazyCollectionStates = new ConcurrentHashMap<>();

	private final SolrZkClient zkClient;

	// private final ZkConfigManager configManager;
	private final boolean closeClient;

	/**
	 * A view of the current state of all collections; combines all the
	 * different state sources into a single view.
	 */
	protected volatile ClusterState clusterState;

	/**
	 * Collections tracked in the legacy (shared) state format, reflects the
	 * contents of clusterstate.json.
	 */
	private Map<String, ClusterState.CollectionRef> legacyCollectionStates = emptyMap();

	/**
	 * Last seen ZK version of clusterstate.json.
	 */
	private int legacyClusterStateVersion = 0;

	private volatile Set<String> liveNodes = emptySet();

	// private volatile Aliases aliases = new Aliases();
	private ConfigData securityData;

	private volatile boolean closed = false;

	public TISZkStateReader(SolrZkClient zkClient) {
		this.zkClient = zkClient;
		this.closeClient = false;
	}

	private static String getIP(String name) {
		return StringUtils.substringBefore(name, ":");
	}

	// private static String getUrl(String hostName) {
	// if (StringUtils.isBlank(hostName)) {
	// throw new IllegalStateException("hostName can not be blank");
	// }
	//
	// return String.format("http://%s:8080/solr/version.jsp", hostName);
	//
	// }
	private static String getLuceneVer(URL url) throws IOException {
		try {
			return HttpUtils.processContent(url, new ConfigFileContext.StreamProcess<String>() {

				public String p(int status, InputStream stream, String md5) {
					try {
						return StringUtils.substringBefore(IOUtils.toString(stream, Charset.forName("utf8")), "_");
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			});
		} catch (Exception e) {
			return "";
		}
	}

	public static DocCollection getCollectionLive(TISZkStateReader zkStateReader, String coll) {
		try {
			return zkStateReader.fetchCollectionState(coll, null);
		} catch (KeeperException e) {
			throw new SolrException(ErrorCode.BAD_REQUEST, "Could not load collection from ZK: " + coll, e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new SolrException(ErrorCode.BAD_REQUEST, "Could not load collection from ZK: " + coll, e);
		}
	}

	// public Aliases getAliases() {
	// return aliases;
	// }
	// public Integer compareStateVersions(String coll, int version) {
	// DocCollection collection = clusterState.getCollectionOrNull(coll);
	// if (collection == null)
	// return null;
	// if (collection.getZNodeVersion() < version) {
	// LOG.debug("Server older than client {}<{}", collection.getZNodeVersion(),
	// version);
	// DocCollection nu = getCollectionLive(this, coll);
	// if (nu == null)
	// return -1;
	// if (nu.getZNodeVersion() > collection.getZNodeVersion()) {
	// updateWatchedCollection(coll, nu);
	// collection = nu;
	// }
	// }
	//
	// if (collection.getZNodeVersion() == version) {
	// return null;
	// }
	//
	// LOG.debug("Wrong version from client [{}]!=[{}]", version,
	// collection.getZNodeVersion());
	//
	// return collection.getZNodeVersion();
	// }
	// public static String getCollectionPath(String coll) {
	// return COLLECTIONS_ZKNODE + "/" + coll + "/state.json";
	// }
	/**
	 * @return
	 */
	public Collection<CoreNode> getSelectTableNodes() {
		if (selectTableNodesMap.isEmpty()) {
			synchronized (this) {
				if (selectTableNodesMap.isEmpty()) {
					fetchSelectTableNodes();
				}
			}
		}
		return selectTableNodesMap.values();
	}

	public void clearSelectTableNodes() {
		selectTableNodesMap.clear();
	}

	private Collection<CoreNode> fetchSelectTableNodes() {
		selectTableNodesMap.clear();
		try {
			// 计算各个节点的副本数
			ClusterState clusterState = this.getClusterState();
			Set<String> collectionNames = clusterState.getCollectionStates().keySet();
			Map<String, Integer> solrCoreCountMap = new HashMap<>();
			clusterState.getLiveNodes().forEach(node -> solrCoreCountMap.put(node, 0));
			collectionNames.stream().map(clusterState::getCollection).map(DocCollection::getSlices)
					.flatMap(// collection流转化为扁平流
							Collection::stream)
					.map(Slice::getReplicas).flatMap(Collection::stream).forEach(replica -> solrCoreCountMap
							.put(replica.getNodeName(), solrCoreCountMap.getOrDefault(replica.getNodeName(), 0) + 1));
			// 计算各个节点的lucene版本
			for (String nodeName : solrCoreCountMap.keySet()) {
				CoreNode node = new CoreNode();
				String ip = getIP(nodeName);
				RunEnvironment runtime = RunEnvironment.getSysRuntime();
				String host = ip;
				if (runtime != RunEnvironment.DAILY) {
					InetAddress address = InetAddress.getByName(ip);
					host = address.getHostName();
				}
				node.setHostName(host);
				node.setNodeName(nodeName);
				node.setSolrCoreCount(solrCoreCountMap.get(nodeName));
				URL url = new URL(GET_LUCENE_VER_RUL_FORMAT.format(new String[] { ip }));
				node.setLuceneSpecVersion(getLuceneVer(url));
				selectTableNodesMap.put(ip, node);
			}
		} catch (Exception e) {
			throw new RuntimeException("get select table node error", e);
		}
		return selectTableNodesMap.values();
	}

	/**
	 * Refresh legacy (shared) clusterstate.json
	 */
	// private void refreshLegacyClusterState(Watcher watcher) throws
	// KeeperException, InterruptedException {
	// try {
	// final Stat stat = new Stat();
	// final byte[] data = zkClient.getData(CLUSTER_STATE, watcher, stat, true);
	// final ClusterState loadedData = ClusterState.load(stat.getVersion(),
	// data, emptySet(), CLUSTER_STATE);
	// synchronized (getUpdateLock()) {
	// this.legacyCollectionStates = loadedData.getCollectionStates();
	// this.legacyClusterStateVersion = stat.getVersion();
	// }
	// } catch (KeeperException.NoNodeException e) {
	// // Ignore missing legacy clusterstate.json.
	// synchronized (getUpdateLock()) {
	// this.legacyCollectionStates = emptyMap();
	// this.legacyClusterStateVersion = 0;
	// }
	// }
	// }
	/**
	 * Forcibly refresh cluster state from ZK. Do this only to avoid race
	 * conditions because it's expensive.
	 */
	public void updateClusterState() throws KeeperException, InterruptedException {
		synchronized (getUpdateLock()) {
			if (clusterState == null) {
				// Never initialized, just run normal initialization.
				createClusterStateWatchersAndUpdate();
				return;
			}
			// No need to set watchers because we should already have watchers
			// registered for everything.
			// refreshLegacyClusterState(null);
			// Need a copy so we don't delete from what we're iterating over.
			Collection<String> safeCopy = new ArrayList<>(watchedCollectionStates.keySet());
			for (String coll : safeCopy) {
				DocCollection newState = fetchCollectionState(coll, null);
				updateWatchedCollection(coll, newState);
			}
			refreshCollectionList(null);
			refreshLiveNodes(null);
			constructState();
		}
	}

	/**
	 * Refresh the set of live nodes.
	 */
	public void updateLiveNodes() throws KeeperException, InterruptedException {
		refreshLiveNodes(null);
	}

	public synchronized void createClusterStateWatchersAndUpdate() throws KeeperException, InterruptedException {
		// We need to fetch the current cluster state and the set of live nodes
		LOG.info("Updating cluster state from ZooKeeper... ");
		// Sanity check ZK structure.
		if (!zkClient.exists(CLUSTER_STATE, true)) {
			throw new SolrException(ErrorCode.SERVICE_UNAVAILABLE,
					"Cannot connect to cluster at " + zkClient.getZkServerAddress() + ": cluster not found/not ready");
		}
		// on reconnect of SolrZkClient force refresh and re-add watches.
		// refreshLegacyClusterState(new LegacyClusterStateWatcher());
		refreshStateFormat2Collections();
		refreshCollectionList(new CollectionsChildWatcher());
		refreshLiveNodes(new LiveNodeWatcher());
		synchronized (TISZkStateReader.this.getUpdateLock()) {
			constructState();
		}
	}

	/**
	 * Construct the total state view from all sources. Must hold
	 * {@link #getUpdateLock()} before calling this.
	 */
	private void constructState() {
		// Legacy clusterstate is authoritative, for backwards compatibility.
		// To move a collection's state to format2, first create the new state2
		// format node, then remove legacy entry.
		Map<String, ClusterState.CollectionRef> result = new LinkedHashMap<>(legacyCollectionStates);
		// legacy cluster state?
		for (String coll : interestingCollections) {
			if (!result.containsKey(coll) && !watchedCollectionStates.containsKey(coll)) {
				new StateWatcher(coll).refreshAndWatch(true);
			}
		}
		// states.
		for (Map.Entry<String, DocCollection> entry : watchedCollectionStates.entrySet()) {
			result.putIfAbsent(entry.getKey(), new ClusterState.CollectionRef(entry.getValue()));
		}
		// Finally, add any lazy collections that aren't already accounted for.
		for (Map.Entry<String, LazyCollectionRef> entry : lazyCollectionStates.entrySet()) {
			result.putIfAbsent(entry.getKey(), entry.getValue());
		}
		this.clusterState = new ClusterState(liveNodes, result, legacyClusterStateVersion);
		// fetchSelectTableNodes();
		LOG.debug("clusterStateSet: legacy [{}] interesting [{}] watched [{}] lazy [{}] total [{}]",
				legacyCollectionStates.keySet().size(), interestingCollections.size(),
				watchedCollectionStates.keySet().size(), lazyCollectionStates.keySet().size(),
				clusterState.getCollectionStates().size());
		if (LOG.isTraceEnabled()) {
			LOG.trace("clusterStateSet: legacy [{}] interesting [{}] watched [{}] lazy [{}] total [{}]",
					legacyCollectionStates.keySet(), interestingCollections, watchedCollectionStates.keySet(),
					lazyCollectionStates.keySet(), clusterState.getCollectionStates());
		}
	}

	/**
	 * Refresh state format2 collections.
	 */
	private void refreshStateFormat2Collections() {
		// exist.
		for (String coll : interestingCollections) {
			new StateWatcher(coll).refreshAndWatch(watchedCollectionStates.containsKey(coll));
		}
	}

	/**
	 * Search for any lazy-loadable state format2 collections.
	 * <p>
	 * A stateFormat=1 collection which is not interesting to us can also be put
	 * into the {@link #lazyCollectionStates} map here. But that is okay because
	 * {@link #constructState()} will give priority to collections in the shared
	 * collection state over this map. In fact this is a clever way to avoid
	 * doing a ZK exists check on the /collections/collection_name/state.json
	 * znode Such an exists check is done in
	 * {@link ClusterState#hasCollection(String)} and
	 * {@link ClusterState#getCollections()} method as a safeguard against
	 * exposing wrong collection names to the users
	 */
	private void refreshCollectionList(Watcher watcher) throws KeeperException, InterruptedException {
		List<String> children = null;
		try {
			children = zkClient.getChildren(COLLECTIONS_ZKNODE, watcher, true);
		} catch (KeeperException.NoNodeException e) {
			LOG.warn("Error fetching collection names: [{}]", e.getMessage());
			// fall through
		}
		if (children == null || children.isEmpty()) {
			lazyCollectionStates.clear();
			return;
		}
		// Don't mess with watchedCollections, they should self-manage.
		// First, drop any children that disappeared.
		this.lazyCollectionStates.keySet().retainAll(children);
		for (String coll : children) {
			// collections, so don't add to lazy.
			if (!interestingCollections.contains(coll)) {
				// Double check contains just to avoid allocating an object.
				LazyCollectionRef existing = lazyCollectionStates.get(coll);
				if (existing == null) {
					lazyCollectionStates.putIfAbsent(coll, new LazyCollectionRef(coll));
				}
			}
		}
	}

	/**
	 * Refresh live_nodes.
	 */
	private void refreshLiveNodes(Watcher watcher) throws KeeperException, InterruptedException {
		Set<String> newLiveNodes;
		try {
			List<String> nodeList = zkClient.getChildren(LIVE_NODES_ZKNODE, watcher, true);
			newLiveNodes = new HashSet<>(nodeList);
		} catch (KeeperException.NoNodeException e) {
			newLiveNodes = emptySet();
		}
		Set<String> oldLiveNodes;
		synchronized (getUpdateLock()) {
			oldLiveNodes = this.liveNodes;
			this.liveNodes = newLiveNodes;
			if (clusterState != null) {
				clusterState.setLiveNodes(newLiveNodes);
			}
		}
		LOG.info("Updated live nodes from ZooKeeper... ({}) -> ({})", oldLiveNodes.size(), newLiveNodes.size());
		if (LOG.isDebugEnabled()) {
			LOG.debug("Updated live nodes from ZooKeeper... {} -> {}", new TreeSet<>(oldLiveNodes),
					new TreeSet<>(newLiveNodes));
		}
	}

	/**
	 * @return information about the cluster from ZooKeeper
	 */
	public ClusterState getClusterState() {
		return clusterState;
	}

	public Object getUpdateLock() {
		return this;
	}

	public void close() {
		this.closed = true;
		if (closeClient) {
			zkClient.close();
		}
	}

	public String getLeaderUrl(String collection, String shard, int timeout) throws InterruptedException {
		ZkCoreNodeProps props = new ZkCoreNodeProps(getLeaderRetry(collection, shard, timeout));
		return props.getCoreUrl();
	}

	public Replica getLeader(String collection, String shard) {
		if (clusterState != null) {
			Replica replica = clusterState.getCollection(collection).getLeader(shard);
			if (replica != null && getClusterState().liveNodesContain(replica.getNodeName())) {
				return replica;
			}
		}
		return null;
	}

	/**
	 * Get shard leader properties, with retry if none exist.
	 */
	public Replica getLeaderRetry(String collection, String shard) throws InterruptedException {
		return getLeaderRetry(collection, shard, GET_LEADER_RETRY_DEFAULT_TIMEOUT);
	}

	/**
	 * Get shard leader properties, with retry if none exist.
	 */
	public Replica getLeaderRetry(String collection, String shard, int timeout) throws InterruptedException {
		long timeoutAt = System.nanoTime() + TimeUnit.NANOSECONDS.convert(timeout, TimeUnit.MILLISECONDS);
		while (true) {
			Replica leader = getLeader(collection, shard);
			if (leader != null)
				return leader;
			if (System.nanoTime() >= timeoutAt || closed)
				break;
			Thread.sleep(GET_LEADER_RETRY_INTERVAL_MS);
		}
		throw new SolrException(ErrorCode.SERVICE_UNAVAILABLE, "No registered leader was found after waiting for "
				+ timeout + "ms " + ", collection: " + collection + " slice: " + shard);
	}

	public List<ZkCoreNodeProps> getReplicaProps(String collection, String shardId, String thisCoreNodeName) {
		return getReplicaProps(collection, shardId, thisCoreNodeName, null);
	}

	public List<ZkCoreNodeProps> getReplicaProps(String collection, String shardId, String thisCoreNodeName,
			Replica.State mustMatchStateFilter) {
		return getReplicaProps(collection, shardId, thisCoreNodeName, mustMatchStateFilter, null);
	}

	public List<ZkCoreNodeProps> getReplicaProps(String collection, String shardId, String thisCoreNodeName,
			Replica.State mustMatchStateFilter, Replica.State mustNotMatchStateFilter) {
		assert thisCoreNodeName != null;
		ClusterState clusterState = this.clusterState;
		if (clusterState == null) {
			return null;
		}
		Map<String, Slice> slices = clusterState.getCollection(collection).getSlicesMap();
		if (slices == null) {
			throw new ZooKeeperException(ErrorCode.BAD_REQUEST, "Could not find collection in zk: " + collection + " "
					+ clusterState.getCollectionStates().keySet());
		}
		Slice replicas = slices.get(shardId);
		if (replicas == null) {
			throw new ZooKeeperException(ErrorCode.BAD_REQUEST, "Could not find shardId in zk: " + shardId);
		}
		Map<String, Replica> shardMap = replicas.getReplicasMap();
		List<ZkCoreNodeProps> nodes = new ArrayList<>(shardMap.size());
		for (Entry<String, Replica> entry : shardMap.entrySet()) {
			ZkCoreNodeProps nodeProps = new ZkCoreNodeProps(entry.getValue());
			String coreNodeName = entry.getValue().getName();
			if (clusterState.liveNodesContain(nodeProps.getNodeName()) && !coreNodeName.equals(thisCoreNodeName)) {
				if (mustMatchStateFilter == null
						|| mustMatchStateFilter == Replica.State.getState(nodeProps.getState())) {
					if (mustNotMatchStateFilter == null
							|| mustNotMatchStateFilter != Replica.State.getState(nodeProps.getState())) {
						nodes.add(nodeProps);
					}
				}
			}
		}
		if (nodes.size() == 0) {
			// no replicas
			return null;
		}
		return nodes;
	}

	// public void updateAliases() throws KeeperException, InterruptedException
	// {
	// final byte[] data = zkClient.getData(ALIASES, null, null, true);
	// this.aliases = ClusterState.load(data);
	// }
	public SolrZkClient getZkClient() {
		return zkClient;
	}

	public Map getClusterProps() {
		try {
			if (getZkClient().exists(ZkStateReader.CLUSTER_PROPS, true)) {
				return (Map) Utils.fromJSON(getZkClient().getData(ZkStateReader.CLUSTER_PROPS, null, new Stat(), true));
			} else {
				return new LinkedHashMap();
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new SolrException(ErrorCode.SERVER_ERROR, "Thread interrupted. Error reading cluster properties", e);
		} catch (KeeperException e) {
			throw new SolrException(ErrorCode.SERVER_ERROR, "Error reading cluster properties", e);
		}
	}

	/**
	 * This method sets a cluster property.
	 *
	 * @param propertyName
	 *            The property name to be set.
	 * @param propertyValue
	 *            The value of the property.
	 */
	public void setClusterProperty(String propertyName, String propertyValue) {
		if (!KNOWN_CLUSTER_PROPS.contains(propertyName)) {
			throw new SolrException(ErrorCode.BAD_REQUEST, "Not a known cluster property " + propertyName);
		}
		for (;;) {
			Stat s = new Stat();
			try {
				if (getZkClient().exists(CLUSTER_PROPS, true)) {
					Map properties = (Map) Utils.fromJSON(getZkClient().getData(CLUSTER_PROPS, null, s, true));
					if (propertyValue == null) {
						// Don't update ZK unless absolutely necessary.
						if (properties.get(propertyName) != null) {
							properties.remove(propertyName);
							getZkClient().setData(CLUSTER_PROPS, Utils.toJSON(properties), s.getVersion(), true);
						}
					} else {
						// Don't update ZK unless absolutely necessary.
						if (!propertyValue.equals(properties.get(propertyName))) {
							properties.put(propertyName, propertyValue);
							getZkClient().setData(CLUSTER_PROPS, Utils.toJSON(properties), s.getVersion(), true);
						}
					}
				} else {
					Map properties = new LinkedHashMap();
					properties.put(propertyName, propertyValue);
					getZkClient().create(CLUSTER_PROPS, Utils.toJSON(properties), CreateMode.PERSISTENT, true);
				}
			} catch (KeeperException.BadVersionException | KeeperException.NodeExistsException e) {
				LOG.warn("Race condition while trying to set a new cluster prop on current version [{}]",
						s.getVersion());
				// race condition
				continue;
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				LOG.error("Thread Interrupted. Error updating path [{}]", CLUSTER_PROPS, e);
				throw new SolrException(ErrorCode.SERVER_ERROR,
						"Thread Interrupted. Error updating cluster property " + propertyName, e);
			} catch (KeeperException e) {
				LOG.error("Error updating path [{}]", CLUSTER_PROPS, e);
				throw new SolrException(ErrorCode.SERVER_ERROR, "Error updating cluster property " + propertyName, e);
			}
			break;
		}
	}

	/**
	 * Returns the content of /security.json from ZooKeeper as a Map If the
	 * files doesn't exist, it returns null.
	 */
	public ConfigData getSecurityProps(boolean getFresh) {
		if (!getFresh) {
			if (securityData == null)
				return new ConfigData(EMPTY_MAP, -1);
			return new ConfigData(securityData.data, securityData.version);
		}
		try {
			Stat stat = new Stat();
			if (getZkClient().exists(SOLR_SECURITY_CONF_PATH, true)) {
				final byte[] data = getZkClient().getData(ZkStateReader.SOLR_SECURITY_CONF_PATH, null, stat, true);
				return data != null && data.length > 0
						? new ConfigData((Map<String, Object>) Utils.fromJSON(data), stat.getVersion()) : null;
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new SolrException(ErrorCode.SERVER_ERROR, "Error reading security properties", e);
		} catch (KeeperException e) {
			throw new SolrException(ErrorCode.SERVER_ERROR, "Error reading security properties", e);
		}
		return null;
	}

	/**
	 * Returns the baseURL corresponding to a given node's nodeName -- NOTE:
	 * does not (currently) imply that the nodeName (or resulting baseURL)
	 * exists in the cluster.
	 *
	 * @lucene.experimental
	 */
	public String getBaseUrlForNodeName(final String nodeName) {
		final int _offset = nodeName.indexOf("_");
		if (_offset < 0) {
			throw new IllegalArgumentException("nodeName does not contain expected '_' seperator: " + nodeName);
		}
		final String hostAndPort = nodeName.substring(0, _offset);
		try {
			final String path = URLDecoder.decode(nodeName.substring(1 + _offset), "UTF-8");
			String urlScheme = (String) getClusterProps().get(URL_SCHEME);
			if (urlScheme == null) {
				urlScheme = "http";
			}
			return urlScheme + "://" + hostAndPort + (path.isEmpty() ? "" : ("/" + path));
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("JVM Does not seem to support UTF-8", e);
		}
	}

	/**
	 * Watches the legacy clusterstate.json.
	 */
	private DocCollection fetchCollectionState(String coll, Watcher watcher)
			throws KeeperException, InterruptedException {
		String collectionPath = ZkStateReader.getCollectionPath(coll);
		try {
			Stat stat = new Stat();
			byte[] data = zkClient.getData(collectionPath, watcher, stat, true);
			ClusterState state = ClusterState.load(stat.getVersion(), data, Collections.<String>emptySet(),
					collectionPath);
			ClusterState.CollectionRef collectionRef = state.getCollectionStates().get(coll);
			return collectionRef == null ? null : collectionRef.get();
		} catch (KeeperException.NoNodeException e) {
			return null;
		}
	}

	public void addCollectionWatch(String coll) {
		if (interestingCollections.add(coll)) {
			LOG.info("addZkWatch [{}]", coll);
			new StateWatcher(coll).refreshAndWatch(false);
			synchronized (getUpdateLock()) {
				constructState();
			}
		}
	}

	private void updateWatchedCollection(String coll, DocCollection newState) {
		if (newState == null) {
			LOG.info("Deleting data for [{}]", coll);
			watchedCollectionStates.remove(coll);
			return;
		}
		// CAS update loop
		while (true) {
			if (!interestingCollections.contains(coll)) {
				break;
			}
			DocCollection oldState = watchedCollectionStates.get(coll);
			if (oldState == null) {
				if (watchedCollectionStates.putIfAbsent(coll, newState) == null) {
					LOG.info("Add data for [{}] ver [{}]", coll, newState.getZNodeVersion());
					break;
				}
			} else {
				if (oldState.getZNodeVersion() >= newState.getZNodeVersion()) {
					// Nothing to do, someone else updated same or newer.
					break;
				}
				if (watchedCollectionStates.replace(coll, oldState, newState)) {
					LOG.info("Updating data for [{}] from [{}] to [{}]", coll, oldState.getZNodeVersion(),
							newState.getZNodeVersion());
					break;
				}
			}
		}
		// Resolve race with removeZKWatch.
		if (!interestingCollections.contains(coll)) {
			watchedCollectionStates.remove(coll);
			LOG.info("Removing uninteresting collection [{}]", coll);
		}
	}

	/**
	 * This is not a public API. Only used by ZkController
	 */
	public void removeZKWatch(String coll) {
		LOG.info("Removing watch for uninteresting collection [{}]", coll);
		interestingCollections.remove(coll);
		watchedCollectionStates.remove(coll);
		lazyCollectionStates.put(coll, new LazyCollectionRef(coll));
		synchronized (getUpdateLock()) {
			constructState();
		}
	}

	public static class ConfigData {

		public Map<String, Object> data;

		public int version;

		public ConfigData() {
		}

		public ConfigData(Map<String, Object> data, int version) {
			this.data = data;
			this.version = version;
		}
	}

	private class LazyCollectionRef extends ClusterState.CollectionRef {

		private final String collName;

		public LazyCollectionRef(String collName) {
			super(null);
			this.collName = collName;
		}

		@Override
		public DocCollection get() {
			// TODO: consider limited caching
			return getCollectionLive(TISZkStateReader.this, collName);
		}

		@Override
		public boolean isLazilyLoaded() {
			return true;
		}

		@Override
		public String toString() {
			return "LazyCollectionRef(" + collName + ")";
		}
	}

	/**
	 * Watches a single collection's format2 state.json.
	 */
	class StateWatcher implements Watcher {

		private final String coll;

		StateWatcher(String coll) {
			this.coll = coll;
		}

		@Override
		public void process(WatchedEvent event) {
			// watcher
			if (EventType.None.equals(event.getType())) {
				return;
			}
			if (!interestingCollections.contains(coll)) {
				// This collection is no longer interesting, stop watching.
				LOG.info("Uninteresting collection {}", coll);
				return;
			}
			int liveNodesSize = TISZkStateReader.this.clusterState == null ? 0
					: TISZkStateReader.this.clusterState.getLiveNodes().size();
			LOG.info(
					"A cluster state change: [{}] for collection [{}] has occurred - updating... (live nodes size: [{}])",
					event, coll, liveNodesSize);
			refreshAndWatch(true);
			synchronized (getUpdateLock()) {
				constructState();
			}
		}

		/**
		 * Refresh collection state from ZK and leave a watch for future
		 * changes. As a side effect, updates {@link #clusterState} and
		 * {@link #watchedCollectionStates} with the results of the refresh.
		 *
		 * @param expectExists
		 *            if true, error if no state node exists
		 */
		public void refreshAndWatch(boolean expectExists) {
			try {
				DocCollection newState = fetchCollectionState(coll, this);
				updateWatchedCollection(coll, newState);
			} catch (KeeperException.NoNodeException e) {
				if (expectExists) {
					LOG.warn("State node vanished for collection: [{}]", coll, e);
				}
			} catch (KeeperException.SessionExpiredException | KeeperException.ConnectionLossException e) {
				LOG.warn("ZooKeeper watch triggered, but Solr cannot talk to ZK: [{}]", e.getMessage());
			} catch (KeeperException e) {
				LOG.error("Unwatched collection: [{}]", coll, e);
				throw new ZooKeeperException(ErrorCode.SERVER_ERROR, "A ZK error has occurred", e);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				LOG.error("Unwatched collection: [{}]", coll, e);
			}
		}
	}

	/**
	 * Watches /collections children .
	 */
	class CollectionsChildWatcher implements Watcher {

		@Override
		public void process(WatchedEvent event) {
			// watcher
			if (EventType.None.equals(event.getType())) {
				return;
			}
			LOG.info("A collections change: [{}], has occurred - updating...", event);
			refreshAndWatch();
			synchronized (getUpdateLock()) {
				constructState();
			}
		}

		/**
		 * Must hold {@link #getUpdateLock()} before calling this method.
		 */
		public void refreshAndWatch() {
			try {
				refreshCollectionList(this);
			} catch (KeeperException.SessionExpiredException | KeeperException.ConnectionLossException e) {
				LOG.warn("ZooKeeper watch triggered, but Solr cannot talk to ZK: [{}]", e.getMessage());
			} catch (KeeperException e) {
				LOG.error("A ZK error has occurred", e);
				throw new ZooKeeperException(SolrException.ErrorCode.SERVER_ERROR, "A ZK error has occurred", e);
			} catch (InterruptedException e) {
				// Restore the interrupted status
				Thread.currentThread().interrupt();
				LOG.warn("Interrupted", e);
			}
		}
	}

	/**
	 * Watches the live_nodes and syncs changes.
	 */
	class LiveNodeWatcher implements Watcher {

		@Override
		public void process(WatchedEvent event) {
			// watcher
			if (EventType.None.equals(event.getType())) {
				return;
			}
			LOG.info("A live node change: [{}], has occurred - updating... (live nodes size: [{}])", event,
					liveNodes.size());
			refreshAndWatch();
		}

		public void refreshAndWatch() {
			try {
				refreshLiveNodes(this);
			} catch (KeeperException.SessionExpiredException | KeeperException.ConnectionLossException e) {
				LOG.warn("ZooKeeper watch triggered, but Solr cannot talk to ZK: [{}]", e.getMessage());
			} catch (KeeperException e) {
				LOG.error("A ZK error has occurred", e);
				throw new ZooKeeperException(SolrException.ErrorCode.SERVER_ERROR, "A ZK error has occurred", e);
			} catch (InterruptedException e) {
				// Restore the interrupted status
				Thread.currentThread().interrupt();
				LOG.warn("Interrupted", e);
			}
		}
	}
}
