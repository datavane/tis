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
package com.qlangtech.tis.openapi.impl;

import org.apache.commons.lang.StringUtils;
import com.qlangtech.tis.pubhook.common.RunEnvironment;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
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

    public String getAppName() {
        return appName;
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
        final String stamp = (appName + String.valueOf(groupIndex) + runtime.getKeyName() + String.valueOf(unmergeglobalparams) + (this.getTargetSnapshotId() == null ? StringUtils.EMPTY : this.getTargetSnapshotId()) + (System.currentTimeMillis() / (1000 * 5)));
        return stamp.hashCode();
    }
}
