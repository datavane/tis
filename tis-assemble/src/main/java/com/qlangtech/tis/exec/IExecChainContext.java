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
package com.qlangtech.tis.exec;

import com.qlangtech.tis.TisZkClient;
import com.qlangtech.tis.fs.ITISFileSystemFactory;
import com.qlangtech.tis.offline.IndexBuilderTriggerFactory;
import com.qlangtech.tis.offline.TableDumpFactory;
import com.qlangtech.tis.order.center.IJoinTaskContext;
import com.qlangtech.tis.sql.parser.SqlTaskNodeMeta;
import com.qlangtech.tis.sql.parser.er.ERRules;
import org.apache.solr.common.cloud.ZkStateReader;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2015年12月15日 上午11:48:16
 */
public interface IExecChainContext extends IJoinTaskContext {

    public TisZkClient getZkClient();

    public ZkStateReader getZkStateReader();

    public String getPartitionTimestamp();

    public ExecutePhaseRange getExecutePhaseRange();

    IIndexMetaData getIndexMetaData();

    /**
     * 全量構建流程ID
     *
     * @return
     */
    public Integer getWorkflowId();

    public String getWorkflowName();

    public SqlTaskNodeMeta.SqlDataFlowTopology getTopology();

    // public ERRules getERRules();
    public ITISFileSystemFactory getIndexBuildFileSystem();

    public TableDumpFactory getTableDumpFactory();

    public IndexBuilderTriggerFactory getIndexBuilderFactory();

    public void rebindLoggingMDCParams();
}
