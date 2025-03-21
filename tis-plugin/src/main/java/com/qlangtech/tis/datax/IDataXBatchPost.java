/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qlangtech.tis.datax;

import com.qlangtech.tis.datax.impl.DataXCfgGenerator;
import com.qlangtech.tis.exec.ExecutePhaseRange;
import com.qlangtech.tis.exec.IExecChainContext;
import com.qlangtech.tis.fullbuild.indexbuild.IRemoteTaskPostTrigger;
import com.qlangtech.tis.fullbuild.indexbuild.IRemoteTaskPreviousTrigger;
import com.qlangtech.tis.fullbuild.indexbuild.IRemoteTaskTrigger;
import com.qlangtech.tis.plugin.ds.ISelectedTab;
import com.qlangtech.tis.sql.parser.tuple.creator.EntityName;

/**
 * 当datax任务有多个子任务完成之后（例如：hive数据同步，多个子库的表导入hdfs完成），需要将执行一次hive数据同步工作
 *
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2022-03-09 11:45
 **/
public interface IDataXBatchPost {

    String KEY_POST = "post_";
    String KEY_PREP = "prep_";

    public enum LifeCycleHook {
        Prep(KEY_PREP), Post(KEY_POST);
        private final String token;

        private LifeCycleHook(String token) {
            this.token = token;
        }

        public String getToken() {
            return this.token;
        }
    }

    public EntityName parseEntity(ISelectedTab tab);

    /**
     * 本次批次操作会涉及到的阶段区间
     *
     * @return
     */
    ExecutePhaseRange getPhaseRange();

    public static String getPreExecuteTaskName(EntityName tab) {
        return KEY_PREP + tab.getTabName();
    }

    /**
     * 在dump任务之前执行执行，例如为hudi表创建schema
     *
     * @param execContext
     * @param tab
     * @return
     */
    public IRemoteTaskPreviousTrigger createPreExecuteTask(IExecChainContext execContext, EntityName entity, ISelectedTab tab);

    /**
     * 在dump任务之后执行，例如：将dump到hdfs上的数据同步到数据湖中去
     *
     * @param execContext
     * @param tab
     * @return
     */
    public IRemoteTaskPostTrigger createPostTask(IExecChainContext execContext, EntityName entity, ISelectedTab tab, IDataXGenerateCfgs cfgFileNames);

}
