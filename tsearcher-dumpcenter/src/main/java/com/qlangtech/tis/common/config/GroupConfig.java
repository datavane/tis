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
package com.qlangtech.tis.common.config;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class GroupConfig extends HashMap<String, HostConfig> implements Serializable {

    private static final long serialVersionUID = -4401590902703282785L;

    protected static Log log = LogFactory.getLog(GroupConfig.class);

    /**
     * @uml.property  name="groupName"
     */
    protected String groupName = "0";

    public GroupConfig(String groupName) {
        this.groupName = groupName;
    }

    public HostConfig addHostConfig(HostConfig hostConfig) {
        return this.put(hostConfig.getIp(), hostConfig);
    }

    public HostConfig getHostConfig(String ip) {
        return this.get(ip);
    }

    /**
     * @return
     * @uml.property  name="groupName"
     */
    public String getGroupName() {
        return this.groupName;
    }

    /**
     * @param groupName
     * @uml.property  name="groupName"
     */
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        Set<String> ipSet = this.keySet();
        sb.append("   ").append(groupName).append("\n");
        for (String ip : ipSet) {
            HostConfig hostConfig = this.getHostConfig(ip);
            sb.append("  ").append(hostConfig.toString()).append("\n");
        }
        return sb.toString();
    }
}
