/**
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.qlangtech.tis.hdfs.client.context.impl;

import com.qlangtech.tis.TisZkClient;
import com.qlangtech.tis.hdfs.client.context.TSearcherQueryContext;
import com.qlangtech.tis.sql.parser.tuple.creator.EntityName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Set;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2013-3-13
 */
public class TSearcherQueryContextImpl implements TSearcherQueryContext {

    static final Logger logger = LoggerFactory.getLogger(TSearcherQueryContextImpl.class);

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
