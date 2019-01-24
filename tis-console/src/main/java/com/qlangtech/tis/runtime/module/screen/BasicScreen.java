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
package com.qlangtech.tis.runtime.module.screen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.manage.biz.dal.pojo.Server;
import com.qlangtech.tis.manage.biz.dal.pojo.ServerGroup;
import com.qlangtech.tis.manage.biz.dal.pojo.ServerGroupCriteria;
import com.qlangtech.tis.manage.biz.dal.pojo.ServerGroupCriteria.Criteria;
import com.qlangtech.tis.manage.biz.dal.pojo.Snapshot;
import com.qlangtech.tis.manage.common.AppDomainInfo;
import com.qlangtech.tis.manage.common.RunContext;
import com.qlangtech.tis.runtime.module.action.BasicModule;
import com.qlangtech.tis.runtime.pojo.ServerGroupAdapter;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public abstract class BasicScreen extends BasicModule {

    /**
     */
    private static final long serialVersionUID = 1L;

    protected static final int PAGE_SIZE = 30;

    /**
     */
    public BasicScreen() {
        super();
    }

    protected void shallnotShowEnvironment(Context context) {
        context.put("shallnotShowEnvironment", true);
    }

    public boolean isShallPresentInFrame() {
        return true;
    }

    /**
     * @param groupName
     */
    public BasicScreen(String groupName) {
        super(groupName);
    }

    public abstract void execute(Context context) throws Exception;

    protected List<ServerGroupAdapter> createServerGroupAdapterList() {
        return createServerGroupAdapterList(true);
    }

    protected List<ServerGroupAdapter> createServerGroupAdapterList(final boolean publishSnapshotIdIsNotNull) {
        final AppDomainInfo domain = this.getAppDomain();
        return createServerGroupAdapterList(new ServerGroupCriteriaSetter() {

            @Override
            public void process(Criteria criteria) {
                criteria.andAppIdEqualTo(domain.getAppid()).andRuntEnvironmentEqualTo(domain.getRunEnvironment().getId());
                if (publishSnapshotIdIsNotNull) {
                    criteria.andPublishSnapshotIdIsNotNull();
                }
            }
        }, publishSnapshotIdIsNotNull);
    }

    public abstract static class ServerGroupCriteriaSetter {

        public abstract void process(ServerGroupCriteria.Criteria criteria);

        public List<Server> getServers(RunContext daoContext, ServerGroup group) {
            // return daoContext.getServerDAO().selectByExample(scrit);
            return Collections.emptyList();
        }

        public int getMaxSnapshotId(ServerGroup group, RunContext daoContext) {
            return 0;
        }
    }

    protected List<ServerGroupAdapter> createServerGroupAdapterList(ServerGroupCriteriaSetter setter, final boolean publishSnapshotIdIsNotNull) {
        return createServerGroupAdapterList(setter, publishSnapshotIdIsNotNull, this);
    }

    public static List<ServerGroupAdapter> createServerGroupAdapterList(ServerGroupCriteriaSetter setter, final boolean publishSnapshotIdIsNotNull, RunContext daoContext) {
        ServerGroupCriteria criteria = new ServerGroupCriteria();
        ServerGroupCriteria.Criteria query = criteria.createCriteria();
        query.andNotDelete();
        setter.process(query);
        // query.andAppIdEqualTo(domain.getAppid()).andRuntEnvironmentEqualTo(
        // domain.getRunEnvironment().getId());
        // 
        // if (publishSnapshotIdIsNotNull) {
        // query.andPublishSnapshotIdIsNotNull();
        // }
        List<ServerGroup> groupList = daoContext.getServerGroupDAO().selectByExample(criteria, 1, 400);
        List<ServerGroupAdapter> groupAdapterList = new ArrayList<ServerGroupAdapter>();
        for (ServerGroup group : groupList) {
            Snapshot snapshot = new Snapshot();
            int maxSnapshotId = 0;
            if (publishSnapshotIdIsNotNull) {
                snapshot = daoContext.getSnapshotDAO().selectByPrimaryKey(group.getPublishSnapshotId());
                // SnapshotCriteria snapshotCriteria = new SnapshotCriteria();
                // snapshotCriteria.createCriteria().andAppidEqualTo(
                // group.getGid());
                // maxSnapshotId = daoContext.getSnapshotDAO().getMaxSnapshotId(
                // snapshotCriteria);
                maxSnapshotId = setter.getMaxSnapshotId(group, daoContext);
                if (snapshot == null) {
                    throw new IllegalStateException("group:" + group.getGid() + " has not set PublishSnapshotId,or group.getPublishSnapshotId():" + group.getPublishSnapshotId() + " has any snapshot in db");
                }
            }
            ServerGroupAdapter adapter = new ServerGroupAdapter(group, snapshot);
            // maxSnapshotId
            adapter.setMaxSnapshotId(maxSnapshotId);
            // ServerCriteria scrit = new ServerCriteria();
            // scrit.createCriteria(false).andGidEqualTo(group.getGid());
            // ;
            // adapter.addServer(daoContext.getServerDAO().selectByExample(scrit));
            adapter.addServer(setter.getServers(daoContext, group));
            groupAdapterList.add(adapter);
        }
        return groupAdapterList;
    }
}
