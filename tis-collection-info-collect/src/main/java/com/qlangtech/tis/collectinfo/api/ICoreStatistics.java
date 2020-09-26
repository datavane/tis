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
package com.qlangtech.tis.collectinfo.api;

import java.util.List;
import java.util.Set;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2014年10月8日下午10:35:32
 */
public interface ICoreStatistics {

    public List<String> getReplicIps(Integer groupIndex);

    public String getAppName();

    public int getHostsCount();

    public Set<String> getHosts();

    public int getGroupCount();

    public long getRequests();

    public String getFormatRequests();

    public long getNumDocs();

    public List<String> getAllServers();
}
