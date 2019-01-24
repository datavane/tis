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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.qlangtech.tis.manage.biz.dal.pojo.ServerJoinGroup;
import com.qlangtech.tis.manage.common.AppDomainInfo;
import com.qlangtech.tis.manage.servlet.QueryIndexServlet.SolrQueryModuleCreator;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public abstract class QueryResutStrategy {

    protected final AppDomainInfo domain;

    private final SolrQueryModuleCreator creator;

    // public boolean isResultAware() {
    // return creator.queryResultAware();
    // }
    public SolrQueryModuleCreator getRequest() {
        return this.creator;
    }

    protected QueryResutStrategy(// RunContext runContext,
    AppDomainInfo domain, SolrQueryModuleCreator creator) {
        super();
        this.domain = domain;
        this.creator = creator;
    }

    static final Collection<String> emptyStringCol = Collections.emptyList();

    public final List<ServerJoinGroup> queryProcess() {
        List<ServerJoinGroup> result = new ArrayList<ServerJoinGroup>();
        Map<Short, List<ServerJoinGroup>> serverList = getSharedNodes();
        // this.getRequest().setAttribute("querySelectServerCandiate",
        // selectCandiate);
        this.getRequest().setQuerySelectServerCandiate(serverList);
        // 如果用户没有点选任何服务器则可以默认选择一个服务器作为组内的服务器
        boolean hasSelectServer = false;
        for (Map.Entry<Short, List<ServerJoinGroup>> entry : serverList.entrySet()) {
            String[] serverGroup = this.getRequest().getParameterValues("servergroup" + entry.getKey());
            if (serverGroup != null && serverGroup.length > 0) {
                hasSelectServer = true;
                break;
            }
        }
        for (Map.Entry<Short, List<ServerJoinGroup>> entry : serverList.entrySet()) {
            String[] serverGroup = this.getRequest().getParameterValues("servergroup" + entry.getKey());
            Collection<String> serverCol = ((serverGroup == null) ? emptyStringCol : Arrays.asList(serverGroup));
            boolean bingo = false;
            for (ServerJoinGroup server : entry.getValue()) {
                if (serverCol.contains(server.getIpAddress())) {
                    result.add(server);
                    bingo = true;
                }
            }
            // 如果用户没有点选任何服务器则可以默认选择一个服务器作为组内的服务器
            if (!hasSelectServer && !bingo && entry.getValue().size() > 0) {
                result.add(entry.getValue().get(0));
            }
        }
        final Collection<String> selectedCanidateServers = new ArrayList<String>();
        for (ServerJoinGroup s : result) {
            selectedCanidateServers.add(s.getIpAddress() + "_" + s.getGroupIndex());
        }
        this.getRequest().selectedCanidateServers(selectedCanidateServers);
        return result;
    }

    protected abstract Map<Short, List<ServerJoinGroup>> getSharedNodes();

    public interface GroupServerProcess {

        void add(short groupIndex, String server, int port);
    }
    // private static List<ServerJoinGroup> traverseAllServer(
    // LocatedCores locatedCores) {
    // final List<ServerJoinGroup> serverlist = new
    // ArrayList<ServerJoinGroup>();
    // traverseAllServers(locatedCores, new GroupServerProcess() {
    // @Override
    // public void add(short groupIndex, String server, int port) {
    // ServerJoinGroup s = new ServerJoinGroup();
    // s.setGroupIndex(groupIndex);
    // s.setIpAddress(server);
    // s.setPort(port);
    // serverlist.add(s);
    // }
    // });
    // return serverlist;
    // }
    // public static void traverseAllServers(LocatedCores locatedCores,
    // GroupServerProcess serverprocess) {
    // if (locatedCores != null) {
    // for (LocatedCore group : locatedCores.getCores()) {
    // for (CoreNodeExcessMap server : group.getLocs()) {
    // // ServerJoinGroup s = new ServerJoinGroup();
    // serverprocess.add((short) group.getC().getCoreNums(),
    // StringUtils.substringBefore(server.getInfo()
    // .getName(), ":"), server.getInfo()
    // .getInfoPort());
    // }
    // }
    // }
    // }
}
