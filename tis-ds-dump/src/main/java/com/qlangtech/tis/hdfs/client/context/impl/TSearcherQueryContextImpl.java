/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.hdfs.client.context.impl;

import com.qlangtech.tis.TisZkClient;
import com.qlangtech.tis.hdfs.client.context.TSearcherQueryContext;
import com.qlangtech.tis.sql.parser.tuple.creator.EntityName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collections;
import java.util.Set;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2013-3-13
 */
public class TSearcherQueryContextImpl implements TSearcherQueryContext {

    static final Log logger = LogFactory.getLog(TSearcherQueryContextImpl.class);

    public static final int DEFAULT_ZK_TIMEOUT = 300000;

    private int zkTimeout = DEFAULT_ZK_TIMEOUT;

    protected EntityName dumpTable = null;


    // private IServiceConfig serviceConfig = null;
    private TisZkClient zkClient = null;


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

    public TSearcherQueryContextImpl() {
        super();
    }


    public void setZkClient(TisZkClient zkClient) {
        this.zkClient = zkClient;
    }

    // @Override
    public Set<String> getGroupNameSet() {
        return Collections.emptySet();
    }
}
