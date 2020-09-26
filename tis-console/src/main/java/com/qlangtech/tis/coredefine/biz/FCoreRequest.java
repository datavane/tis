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
package com.qlangtech.tis.coredefine.biz;

import com.qlangtech.tis.coredefine.module.action.CoreAction.CoreRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2013-4-15
 */
public class FCoreRequest {

    protected final CoreRequest request;

    private boolean valid = false;

    protected final List<String> ips = new ArrayList<String>(0);

    // 已经分配到的组数
    private final int assigndGroup;

    public FCoreRequest(CoreRequest request, int groupCount) {
        this(request, groupCount, 0);
    }

    /**
     * @param request
     * @param groupCount 现在应用的总组数
     */
    public FCoreRequest(CoreRequest request, int groupCount, int assigndGroup) {
        super();
        this.request = request;
        if (groupCount < 1) {
            throw new IllegalArgumentException("groupCount can not be null");
        }
        this.replicCount = new short[groupCount];
        this.assigndGroup = assigndGroup;
    }

    public String getCreateNodeSet() {
        // return ips.stream().map((ip) -> StringUtils.substringBefore(ip, ":")).collect(Collectors.joining(","));
        return ips.stream().collect(Collectors.joining(","));
    }

    public String getIndexName() {
        return this.request.getServiceName();
    }

    // 标识每个组内副本个数
    private final short[] replicCount;

    private final Map<Integer, Collection<String>> serversView = new HashMap<Integer, Collection<String>>();

    public void addNodeIps(int group, String ip) {
        ips.add(ip);
        request.addNodeIps(String.valueOf(group), ip);
        replicCount[group]++;
        Collection<String> servers = serversView.get(group);
        if (servers == null) {
            servers = new ArrayList<String>();
            serversView.put(group, servers);
        }
        servers.add(ip);
    }

    public Map<Integer, Collection<String>> getServersView() {
        return this.serversView;
    }

    public short[] getReplicCount() {
        return Arrays.copyOfRange(replicCount, assigndGroup, replicCount.length);
    }

    public String[] getIps() {
        return ips.toArray(new String[ips.size()]);
    }

    public List<String> getAllIps() {
        return this.ips;
    }

    /**
     * @param valid the valid to set
     */
    public void setValid(boolean valid) {
        this.valid = valid;
    }

    /**
     * @return the valid
     */
    public boolean isValid() {
        return valid;
    }

    public CoreRequest getRequest() {
        return request;
    }
}
