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
package com.qlangtech.tis.openapi.impl;

import com.qlangtech.tis.pubhook.common.RunEnvironment;
import org.apache.commons.lang.StringUtils;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2013-12-20
 */
public class AppKey {

    public final String appName;

    public final Short groupIndex;

    public final RunEnvironment runtime;

    public final boolean unmergeglobalparams;

    // 目标配置文件版本
    private Long targetSnapshotId;

    // 取的内容是否要用缓存中索取
    private boolean fromCache = true;

    public AppKey(String appName, Short groupIndex, RunEnvironment runtime, boolean unmergeglobalparams) {
        this.appName = appName;
        this.groupIndex = groupIndex;
        this.runtime = runtime;
        this.unmergeglobalparams = unmergeglobalparams;
    }

    public boolean isFromCache() {
        return fromCache;
    }

    public void setFromCache(boolean fromCache) {
        this.fromCache = fromCache;
    }

    public Long getTargetSnapshotId() {
        return targetSnapshotId;
    }

    public void setTargetSnapshotId(Long targetSnapshotId) {
        this.targetSnapshotId = targetSnapshotId;
    }

    @Override
    public int hashCode() {
        // 确保 这个key在5秒之内是相同的
        final String stamp = (appName + String.valueOf(groupIndex) + runtime.getKeyName() + String.valueOf(unmergeglobalparams) + (this.getTargetSnapshotId() == null ? StringUtils.EMPTY : this.getTargetSnapshotId()) + (System.currentTimeMillis() / (1000 * 50)));
        return stamp.hashCode();
    }

    @Override
    public String toString() {
        return "AppKey{" + "appName='" + appName + '\'' + ", groupIndex=" + groupIndex + ", runtime=" + runtime + ", unmergeglobalparams=" + unmergeglobalparams + ", targetSnapshotId=" + targetSnapshotId + ", fromCache=" + fromCache + '}';
    }
}
