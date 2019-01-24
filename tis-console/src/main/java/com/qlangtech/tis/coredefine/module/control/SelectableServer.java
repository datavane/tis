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
package com.qlangtech.tis.coredefine.module.control;

import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.coredefine.module.screen.CoreDefineScreen;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.cloud.TISZkStateReader;
import org.apache.zookeeper.KeeperException;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/*
 * 可选择的组
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class SelectableServer extends CoreDefineScreen {

    static final String key = SelectableServer.class.getName() + ".candidateServer";

    private static final long serialVersionUID = -3919176682810058692L;

    private Integer contextid;

    private String service;

    private Boolean excludeHaveAppServers = false;

    private Boolean canidateServerNameAware = false;

    private Boolean showselect;

    private String ownserversKey;

    /**
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     */
    private static Collection<CoreNode> getSelectableNodes(TISZkStateReader stateReader) throws KeeperException, InterruptedException {
        return stateReader.getSelectTableNodes();
    }

    // private static String getHostNameFromUrl(String name) {
    // return StringUtils.substringBefore(name, ":");
    // }
    /**
     * @param request
     * @param coreDefineScreen
     * @param excludeHaveAppServers
     *            是否需要排除那些服务器上已经有应用的 服务器
     * @param isAppNameAware
     *            候选服务不能是否与应用名称相关
     * @return
     */
    @SuppressWarnings("all")
    public static CoreNode[] getCoreNodeInfo(HttpServletRequest request, CoreDefineScreen coreDefineScreen, boolean excludeHaveAppServers, boolean isAppNameAware) {
        try {
            CoreNode[] result = null;
            if ((result = (CoreNode[]) request.getAttribute(key)) == null) {
                // if (isAppNameAware) {
                // 
                // // 取得和应用名称相关的候选机器
                // // 这个是在添加组，添加组内副本的时候调用的
                // result = coreDefineScreen.getClientProtocol()
                // .getFreeServersDesc(
                // coreDefineScreen.getAppDomain()
                // .getAppName());
                // } else {
                // // 这个是在初始化应用的时候调用的
                // result = coreDefineScreen.getClientProtocol()
                // .getClusterCoreNodeInfoByFreeDesc();
                // }
                Collection<CoreNode> nodes = getSelectableNodes(coreDefineScreen.getZkStateReader());
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

    @Override
    @SuppressWarnings("all")
    public void execute(Context ctx) throws Exception {
        // CoreNodeExcessMap[] nodelist = (CoreNodeExcessMap[]) context
        // .get("ownservers");
        WrapperContext context = new WrapperContext(ctx);
        Object group = this.getContextid();
        ctx.put("controlContextId", (group != null) ? group : StringUtils.EMPTY);
        Collection<CoreNode> selectableNodes = getSelectableNodes(this.getZkStateReader());
        context.pubCoreNodeList(new ArrayList<CoreNode>(selectableNodes));
    // if (showselect == null) {
    // 
    // // 显示所有集群中服务器地址信息
    // context.pubCoreNodeList(getCoreNodeInfo(//
    // context.getBoolean("excludeHaveAppServers")
    // this.excludeHaveAppServers, // context
    // // 是否是和服务器是否和应用名称相关联
    // // .getBoolean("canidateServerNameAware")
    // this.canidateServerNameAware));
    // return;
    // }
    // 
    // Assert.assertNotNull("service name can not be null", this.service);
    // Assert.assertNotNull("group can not be null", group);
    // if (Boolean.TRUE.equals(showselect)) {
    // List<CoreNodeInfo> coreNodelist = new ArrayList<CoreNodeInfo>();
    // // 显示已经拥有的机器
    // // = (CoreNodeExcessMap[]) ctx
    // // .get("ownservers");
    // 
    // Assert.assertNotNull("have not set ownservers", this
    // .getOwnservers());
    // 
    // for (CoreNodeExcessMap excess : this.getOwnservers()) {
    // coreNodelist.add(new ExtCoreNodeInfo(excess.getInfo(), excess
    // .isMaster(), excess.isRealTime()));
    // }
    // 
    // context.pubCoreNodeList(coreNodelist);
    // } else {
    // // 取得备选服务器
    // context.pubCoreNodeList(this.getCoreManager()
    // .getCoreNodeNotSolrCoreList(service,
    // Integer.parseInt(String.valueOf(group))));
    // }
    // if (nodelist == null) {
    // nodelist = new CoreNodeExcessMap[0];
    // }
    // final List<CoreNodeInfo> selectable = new ArrayList<CoreNodeInfo>();
    // List<CoreNodeInfo> allnodelist = Arrays.asList(getCoreNodeInfo());
    // aa: for (CoreNodeInfo info : allnodelist) {
    // for (CoreNodeExcessMap select : nodelist) {
    // if (StringUtils.equals(select.getInfo().getName(), info
    // .getName())) {
    // continue aa;
    // }
    // }
    // 
    // selectable.add(info);
    // }
    }

    public Integer getContextid() {
        return contextid;
    }

    public void setContextid(Integer contextid) {
        this.contextid = contextid;
    }

    public String getService() {
        return service;
    }

    // /**
    // * @param excludeHaveAppServers
    // * 是否需要排除那些服务器上已经有应用的 服务器
    // * @param isAppNameAware
    // * @return
    // * @throws Exception
    // */
    // private CoreNodeInfo[] getCoreNodeInfo(boolean excludeHaveAppServers,
    // boolean isAppNameAware) throws Exception {
    // return getCoreNodeInfo(this.getRequest(), this, excludeHaveAppServers,
    // isAppNameAware);
    // }
    // 
    public void setService(String service) {
        this.service = service;
    }

    public Boolean getExcludeHaveAppServers() {
        return excludeHaveAppServers;
    }

    public void setExcludeHaveAppServers(Boolean excludeHaveAppServers) {
        this.excludeHaveAppServers = excludeHaveAppServers;
    }

    public Boolean getCanidateServerNameAware() {
        return canidateServerNameAware;
    }

    public void setCanidateServerNameAware(Boolean canidateServerNameAware) {
        this.canidateServerNameAware = canidateServerNameAware;
    }

    public Boolean getShowselect() {
        return showselect;
    }

    public void setShowselect(Boolean showselect) {
        this.showselect = showselect;
    }

    public void setOwnserversKey(String ownserversKey) {
        this.ownserversKey = ownserversKey;
    }

    public static class WrapperContext {

        private static final List<CoreNode> emptylist = Collections.emptyList();

        private final Context context;

        public WrapperContext(Context context) {
            super();
            this.context = context;
        }

        public void pubCoreNodeList(CoreNode[] nodes) {
            pubCoreNodeList((nodes == null) ? emptylist : Arrays.asList(nodes));
        }

        public boolean getBoolean(String key) {
            Boolean result = (Boolean) context.get(key);
            return result == null ? false : result;
        }

        public void pubCoreNodeList(List<CoreNode> nodes) {
            Collections.sort(nodes, new Comparator<CoreNode>() {

                @Override
                public int compare(CoreNode o1, CoreNode o2) {
                    return o1.getHostName().compareToIgnoreCase(o2.getHostName());
                }
            });
            context.put("candidateServer", nodes);
        }
    }

    public static class CoreNode {

        private String nodeName;

        private String luceneSpecVersion;

        private String hostName;

        private int solrCoreCount;

        public String getName() {
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

        public String getLuceneSpecVersion() {
            return luceneSpecVersion;
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
