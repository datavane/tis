/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 *
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.manage.servlet;

import com.qlangtech.tis.coredefine.module.action.CollectionTopology;
import com.qlangtech.tis.manage.common.AppDomainInfo;
import com.qlangtech.tis.manage.servlet.QueryIndexServlet.SolrQueryModuleCreator;
import org.apache.commons.lang3.StringUtils;
import java.util.*;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2012-9-5
 */
public abstract class QueryResutStrategy {

    public final AppDomainInfo domain;

    private final SolrQueryModuleCreator creator;

    // public boolean isResultAware() {
    // return creator.queryResultAware();
    // }
    public SolrQueryModuleCreator getRequest() {
        return this.creator;
    }

    public final Map<String, List<ServerJoinGroup>> selectCandiate = new HashMap<>();

    protected QueryResutStrategy(AppDomainInfo domain, SolrQueryModuleCreator creator) {
        super();
        if (domain == null || StringUtils.isBlank(domain.getAppName())) {
            throw new IllegalStateException("collection info can not be null");
        }
        this.domain = domain;
        this.creator = creator;
    }

    static final Collection<String> emptyStringCol = Collections.emptyList();

    public final List<ServerJoinGroup> queryProcess() {
        List<ServerJoinGroup> result = new ArrayList<>();
        List<ServerJoinGroup> serverList = query();
        for (ServerJoinGroup server : serverList) {
            List<ServerJoinGroup> groupServer = selectCandiate.get(server.getGroupIndex());
            if (groupServer == null) {
                groupServer = new ArrayList<>();
                selectCandiate.put(String.valueOf(server.getGroupIndex()), groupServer);
            }
            groupServer.add(server);
        }
        this.getRequest().setQuerySelectServerCandiate(selectCandiate);
        // 如果用户没有点选任何服务器则可以默认选择一个服务器作为组内的服务器
        boolean hasSelectServer = false;
        for (Map.Entry<String, List<ServerJoinGroup>> entry : selectCandiate.entrySet()) {
            String[] serverGroup = this.getRequest().getParameterValues("servergroup" + entry.getKey());
            if (serverGroup != null && serverGroup.length > 0) {
                hasSelectServer = true;
                break;
            }
        }
        ServerJoinGroup selected = null;
        for (Map.Entry<String, List<ServerJoinGroup>> entry : selectCandiate.entrySet()) {
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
                selected = entry.getValue().get(0);
                selected.setChecked(true);
                result.add(selected);
            }
        }
        return result;
    }

    public abstract CollectionTopology createCollectionTopology();

    protected abstract List<ServerJoinGroup> query();

    public interface GroupServerProcess {

        void add(short groupIndex, String server, int port);
    }
}
