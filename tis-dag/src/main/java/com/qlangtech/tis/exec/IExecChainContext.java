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
package com.qlangtech.tis.exec;

import com.qlangtech.tis.TisZkClient;
import com.qlangtech.tis.fs.ITISFileSystem;
import com.qlangtech.tis.manage.IBasicAppSource;
import com.qlangtech.tis.offline.IndexBuilderTriggerFactory;
import com.qlangtech.tis.offline.TableDumpFactory;
import com.qlangtech.tis.order.center.IJoinTaskContext;
import org.apache.solr.common.cloud.ZkStateReader;

import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2015年12月15日 上午11:48:16
 */
public interface IExecChainContext extends IJoinTaskContext {

    public void addAsynSubJob(AsynSubJob jobName);

    public List<AsynSubJob> getAsynSubJobsName();

    public boolean containAsynJob();

    class AsynSubJob {
        public final String jobName;

        public AsynSubJob(String jobName) {
            this.jobName = jobName;
        }
    }

    <T extends IBasicAppSource> T getAppSource();

    TisZkClient getZkClient();

    ZkStateReader getZkStateReader();

    String getPartitionTimestamp();

    IIndexMetaData getIndexMetaData();

    /**
     * 全量構建流程ID
     *
     * @return
     */
    Integer getWorkflowId();

    String getWorkflowName();

    ITISFileSystem getIndexBuildFileSystem();

    TableDumpFactory getTableDumpFactory();

    IndexBuilderTriggerFactory getIndexBuilderFactory();

    void rebindLoggingMDCParams();
}
