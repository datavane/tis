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

import com.qlangtech.tis.manage.biz.dal.pojo.ServerGroup;
import com.qlangtech.tis.manage.common.RunContext;
import com.qlangtech.tis.manage.common.SnapshotDomain;
import com.qlangtech.tis.openapi.SnapshotNotFindException;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class SnapshotDomainGetter {

    // private static final long serialVersionUID = 1L;
    private final RunContext runContext;

    public SnapshotDomainGetter(RunContext runContext) {
        super();
        this.runContext = runContext;
    }

    // @Override
    public // HttpServletRequest
    SnapshotDomain getSnapshot(// HttpServletRequest
    AppKey appKey) throws // request)
    SnapshotNotFindException {
        SnapshotInfoFromRequest result = new SnapshotInfoFromRequest();
        // final String resources = getResources(request);
        if (appKey.getTargetSnapshotId() != null && appKey.getTargetSnapshotId() > 0) {
            result.snapshotId = appKey.getTargetSnapshotId().intValue();
        } else {
            final ServerGroup group = runContext.getServerGroupDAO().load(appKey.appName, appKey.groupIndex, appKey.runtime.getId());
            if (group == null) {
                throw new SnapshotNotFindException("appName:" + appKey.appName + " groupIndex:" + appKey.groupIndex + " runtime:" + appKey.runtime + " has not a corresponding server group in db");
            }
            if (group.getPublishSnapshotId() == null) {
                throw new SnapshotNotFindException("groupid:" + group.getGid() + " has not set publish snapshot id");
            }
            result.snapshotId = group.getPublishSnapshotId();
        }
        // 如果在request中设置了unmergeglobalparams 这个参数
        if (!appKey.unmergeglobalparams) {
            result.runtime = appKey.runtime;
        }
        return runContext.getSnapshotViewDAO().getView(result.snapshotId, result.runtime);
    }
}
