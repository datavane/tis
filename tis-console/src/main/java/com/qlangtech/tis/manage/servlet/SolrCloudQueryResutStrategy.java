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
package com.qlangtech.tis.manage.servlet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.cloud.DocCollection;
import org.apache.solr.common.cloud.Replica;
import org.apache.solr.common.cloud.Slice;
import org.apache.solr.common.cloud.TISZkStateReader;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.qlangtech.tis.manage.biz.dal.pojo.ServerJoinGroup;
import com.qlangtech.tis.manage.common.AppDomainInfo;
import com.qlangtech.tis.manage.common.RunContext;
import com.qlangtech.tis.manage.servlet.QueryIndexServlet.SolrQueryModuleCreator;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class SolrCloudQueryResutStrategy extends QueryResutStrategy {

	protected final RunContext runContext;

	private static final String SHARD_PREIX = "shard";

	private static final Cache<String, Map<Short, List<ServerJoinGroup>>> /*
																			 * collection
																			 * name
																			 */
	sharedNodesCache;

	static {
		sharedNodesCache = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).build();
	}

	SolrCloudQueryResutStrategy(AppDomainInfo domain, RunContext runContext, SolrQueryModuleCreator creator) {
		super(domain, creator);
		this.runContext = runContext;
	}

	@Override
	public boolean collectionExist() {
		TISZkStateReader clusterReader = runContext.getZkStateReader();
		return clusterReader.getClusterState().getCollectionRef(domain.getAppName()) != null;
	}

	// @Override
	// public int getServicePort() {
	// return 7001;
	// }
	@Override
	public Map<Short, List<ServerJoinGroup>> getSharedNodes() {
		Map<Short, List<ServerJoinGroup>> selectCandiate = null;
		selectCandiate = sharedNodesCache.getIfPresent(domain.getAppName());
		if (selectCandiate == null) {
			try {
				selectCandiate = sharedNodesCache.get(domain.getAppName(),
						new Callable<Map<Short, List<ServerJoinGroup>>>() {

							@Override
							public Map<Short, List<ServerJoinGroup>> call() throws Exception {
								List<ServerJoinGroup> result = new ArrayList<ServerJoinGroup>();
								ServerJoinGroup groupServer = null;
								TISZkStateReader clusterReader = runContext.getZkStateReader();
								DocCollection docCollection = TISZkStateReader.getCollectionLive(clusterReader,
										domain.getAppName());
								Map<String, Slice> groups = docCollection.getSlicesMap();
								short shard;
								for (Map.Entry<String, Slice> entry : groups.entrySet()) {
									for (Replica replic : entry.getValue().getReplicas()) {
										groupServer = new ServerJoinGroup();
										groupServer.setLeader(replic.getBool("leader", false));
										groupServer.setIpAddress(replic.getCoreUrl());
										shard = (Short
												.parseShort(StringUtils.substringAfter(entry.getKey(), SHARD_PREIX)));
										groupServer.setGroupIndex(--shard);
										result.add(groupServer);
									}
								}
								Map<Short, List<ServerJoinGroup>> selectCandiate = new HashMap<Short, List<ServerJoinGroup>>();
								for (ServerJoinGroup server : result) {
									List<ServerJoinGroup> servers = selectCandiate.get(server.getGroupIndex());
									if (servers == null) {
										servers = new ArrayList<ServerJoinGroup>();
										selectCandiate.put(server.getGroupIndex(), servers);
									}
									servers.add(server);
								}
								return selectCandiate;
							}
						});
			} catch (ExecutionException e) {
				throw new RuntimeException(e);
			}
		}
		return selectCandiate;
	}

	public static QueryResutStrategy create(final AppDomainInfo domain, SolrQueryModuleCreator creator,
			RunContext runContext) {
		return new SolrCloudQueryResutStrategy(domain, runContext, creator);
	}
}
