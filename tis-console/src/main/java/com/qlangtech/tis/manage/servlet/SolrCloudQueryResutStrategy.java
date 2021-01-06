/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.manage.servlet;

import com.qlangtech.tis.coredefine.module.action.CollectionTopology;
import com.qlangtech.tis.manage.biz.dal.pojo.ServerJoinGroup;
import com.qlangtech.tis.manage.common.AppDomainInfo;
import com.qlangtech.tis.manage.common.RunContext;
import com.qlangtech.tis.manage.servlet.QueryIndexServlet.SolrQueryModuleCreator;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.cloud.DocCollection;
import org.apache.solr.common.cloud.Replica;
import org.apache.solr.common.cloud.Slice;
import org.apache.solr.common.cloud.TISZkStateReader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2015年12月23日 下午6:58:13
 */
public class SolrCloudQueryResutStrategy extends QueryResutStrategy {

  protected final RunContext runContext;

  private static final String SHARD_PREIX = "shard";

  SolrCloudQueryResutStrategy(AppDomainInfo domain, RunContext runContext, SolrQueryModuleCreator creator) {
    super(domain, creator);
    this.runContext = runContext;
  }

  // @Override
  // public int getServicePort() {
  // return 7001;
  // }
  @Override
  public List<ServerJoinGroup> query() {
    List<ServerJoinGroup> result = new ArrayList<ServerJoinGroup>();
    ServerJoinGroup groupServer = null;
    TISZkStateReader clusterReader = runContext.getZkStateReader();
    DocCollection docCollection = TISZkStateReader.getCollectionLive(clusterReader, domain.getAppName());
    if (docCollection == null) {
      throw new IllegalStateException("collection:" + domain.getAppName() + " relevant docCollection can not be null");
    }
    Map<String, Slice> groups = docCollection.getSlicesMap();
    short shard;
    for (Map.Entry<String, Slice> entry : groups.entrySet()) {

      for (Replica replic : entry.getValue().getReplicas()) {
        groupServer = new ServerJoinGroup();
        groupServer.setLeader(replic.getBool("leader", false));
        groupServer.setIpAddress(replic.getCoreUrl());
        groupServer.setReplicBaseUrl(replic.getBaseUrl());
        shard = (Short.parseShort(StringUtils.substringAfter(entry.getKey(), SHARD_PREIX)));
        groupServer.setGroupIndex(--shard);
        result.add(groupServer);
      }
    }
    return result;
  }

  public CollectionTopology createCollectionTopology() {
    CollectionTopology topology = new CollectionTopology();
    TISZkStateReader clusterReader = runContext.getZkStateReader();
    DocCollection docCollection = TISZkStateReader.getCollectionLive(clusterReader, domain.getAppName());
    if (docCollection == null) {
      throw new IllegalStateException("collection:" + domain.getAppName() + " relevant docCollection can not be null");
    }
    CollectionTopology.Shared shared = null;
    Map<String, Slice> groups = docCollection.getSlicesMap();
    short shard;
    for (Map.Entry<String, Slice> entry : groups.entrySet()) {
      shared = new CollectionTopology.Shared(entry.getKey());
      topology.addShard(shared);
      // shardName = entry.getKey();
      for (Replica replic : entry.getValue().getReplicas()) {
        shared.addReplic(replic);
        // groupServer = new ServerJoinGroup();
        // groupServer.setLeader(replic.getBool("leader", false));
        // groupServer.setIpAddress(replic.getCoreUrl());
        // shard = (Short.parseShort(StringUtils.substringAfter(entry.getKey(), SHARD_PREIX)));
        // groupServer.setGroupIndex(--shard);
        // result.add(groupServer);
      }
    }
    return topology;
  }

  public static QueryResutStrategy create(final AppDomainInfo domain, SolrQueryModuleCreator creator, RunContext runContext) {
    // if (domain.isAutoDeploy()) {
    return new SolrCloudQueryResutStrategy(domain, runContext, creator);
    // } else {
    // return new NormalQueryResutStrategy(domain, runContext, creator);
    // }
  }
}
