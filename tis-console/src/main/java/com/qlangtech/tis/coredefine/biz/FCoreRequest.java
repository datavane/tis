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
package com.qlangtech.tis.coredefine.biz;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.qlangtech.tis.coredefine.module.action.CoreAction.CoreRequest;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class FCoreRequest {

    protected final CoreRequest request;

    private boolean valid = false;

    protected final List<String> ips = new ArrayList<String>(0);

    // 已经分配到的组数
    private final int assigndGroup;

    private final List<String> rules = new ArrayList<>();

    public List<String> getRules() {
        return this.rules;
    }

    /**
     * 添加规则
     * @param rule
     */
    public void addRule(String rule) {
        this.rules.add(rule);
    }

    public String getCreateNodeSet() {
        StringBuffer result = new StringBuffer();
        int ipsSize = ips.size();
        for (int i = 0; i < ipsSize; i++) {
            result.append(ips.get(i));
            if (i < (ipsSize - 1)) {
                result.append(",");
            }
        }
        return result.toString();
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

    public FCoreRequest(CoreRequest request, int groupCount) {
        this(request, groupCount, 0);
    }

    /**
     * @param request
     * @param groupCount
     *            现在应用的总组数
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

    /**
     * @param valid
     *            the valid to set
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
