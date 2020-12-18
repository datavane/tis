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
// */
package com.qlangtech.tis.coredefine.module.control;

import com.qlangtech.tis.runtime.module.action.BasicModule;
import org.apache.solr.common.cloud.TISZkStateReader;
import org.apache.zookeeper.KeeperException;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/09/25
 */
public class SelectableServer {

  static final String key = SelectableServer.class.getName() + ".candidateServer";

  private static Collection<CoreNode> getSelectableNodes(TISZkStateReader stateReader) throws KeeperException, InterruptedException {
    return stateReader.getSelectTableNodes();
  }


  @SuppressWarnings("all")
  public static CoreNode[] getCoreNodeInfo(HttpServletRequest request, BasicModule module
    , boolean excludeHaveAppServers, boolean isAppNameAware) {
    try {
      CoreNode[] result = null;
      if ((result = (CoreNode[]) request.getAttribute(key)) == null) {
        Collection<CoreNode> nodes = getSelectableNodes(module.getZkStateReader());
        result = nodes.toArray(new CoreNode[0]);
        request.setAttribute(key, result);
        if (excludeHaveAppServers) {
          // 过滤掉那些已经部署了应用的服务器
          List<CoreNode> excludeAppServers = new ArrayList<CoreNode>();
          for (CoreNode node : result) {
            if (node.getSolrCoreCount() < 1) {
              excludeAppServers.add(node);
            }
          }
          result = excludeAppServers.toArray(new CoreNode[excludeAppServers.size()]);
          request.setAttribute(key, result);
        }
      }
      return result;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  //
  public static class ServerNodeTopology {

    private int shardCount;

    private int replicaCount;

    private CoreNode[] hosts;

    public int getShardCount() {
      return shardCount;
    }

    public void setShardCount(int shardCount) {
      this.shardCount = shardCount;
    }

    public int getReplicaCount() {
      return replicaCount;
    }

    public void setReplicaCount(int replicaCount) {
      this.replicaCount = replicaCount;
    }

    public CoreNode[] getHosts() {
      return hosts;
    }

    public void setHosts(CoreNode[] hosts) {
      this.hosts = hosts;
    }
  }

  //
  public static class CoreNode {

    private String nodeName;

    private String luceneSpecVersion;

    private String hostName;

    private int solrCoreCount;

    public String getNodeName() {
      // StringUtils.substringBefore(this.nodeName,
      return this.nodeName;
      // "_");
    }

    public String getHostName() {
      return this.hostName;
    }

    public void setHostName(String hostName) {
      this.hostName = hostName;
    }

    public String getLuceneVersion() {
      return luceneSpecVersion;
      // return String.valueOf(LuceneVersion.parseByVersionNumber(luceneSpecVersion).getVersion());
    }

    public String getLuceneSpecVersion() {
      return luceneSpecVersion;
      // return LuceneVersion.parseByVersionNumber(luceneSpecVersion).getVersion();
    }

    public void setLuceneSpecVersion(String luceneSpecVersion) {
      this.luceneSpecVersion = luceneSpecVersion;
    }

    public void setNodeName(String nodeName) {
      this.nodeName = nodeName;
    }

    public int getSolrCoreCount() {
      return solrCoreCount;
    }

    public void setSolrCoreCount(int solrCoreCount) {
      this.solrCoreCount = solrCoreCount;
    }
  }
}
