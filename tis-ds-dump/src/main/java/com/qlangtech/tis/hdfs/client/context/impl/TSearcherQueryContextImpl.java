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
package com.qlangtech.tis.hdfs.client.context.impl;

import com.qlangtech.tis.TisZkClient;
import com.qlangtech.tis.hdfs.client.context.TSearcherQueryContext;
import com.qlangtech.tis.hdfs.client.router.GroupRouter;
import com.qlangtech.tis.sql.parser.tuple.creator.EntityName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import java.util.Collections;
import java.util.Set;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2013-3-13
 */
public class TSearcherQueryContextImpl implements InitializingBean, TSearcherQueryContext {

    static final Log logger = LogFactory.getLog(TSearcherQueryContextImpl.class);

    public static final int DEFAULT_ZK_TIMEOUT = 300000;

    private int zkTimeout = DEFAULT_ZK_TIMEOUT;

    protected EntityName dumpTable = null;

    // private String shardKey = "id";
    private GroupRouter groupRouter;

    // private IServiceConfig serviceConfig = null;
    private TisZkClient zkClient = null;

    @Override
    public void afterPropertiesSet() throws Exception {
    }

    public TisZkClient getZkClient() {
        return this.zkClient;
    }

    @Override
    public EntityName getDumpTable() {
        if (this.dumpTable == null) {
            throw new IllegalStateException("dumptable can not be null");
        }
        return this.dumpTable;
    }

    public void setDumpTable(EntityName dumpTable) {
        this.dumpTable = dumpTable;
    }

    /**
     * 触发服务配置更新事件
     */
    public void fireServiceConfigChange() {
    }

    /**
     */
    public TSearcherQueryContextImpl() {
        super();
    }

    public final GroupRouter getGroupRouter() {
        return groupRouter;
    }

    public final void setGroupRouter(GroupRouter groupRouter) {
        this.groupRouter = groupRouter;
    }

    // public final void setZkAddress(String zkAddress) {
    // this.zkAddress = zkAddress;
    // }
    // public final String getShardKey() {
    // return shardKey;
    // }
    // 
    // public final void setShardKey(String shardKey) {
    // this.shardKey = shardKey;
    // }
    // public TerminatorZkClient getZkClient() {
    // return zkClient;
    // }
    // 
    public void setZkClient(TisZkClient zkClient) {
        this.zkClient = zkClient;
    }

    // @Override
    public Set<String> getGroupNameSet() {
        return Collections.emptySet();
    }
}
