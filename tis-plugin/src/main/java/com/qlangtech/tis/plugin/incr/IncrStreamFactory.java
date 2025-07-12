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
package com.qlangtech.tis.plugin.incr;

import com.qlangtech.tis.TIS;
import com.qlangtech.tis.annotation.Public;
import com.qlangtech.tis.coredefine.module.action.IFlinkIncrJobStatus;
import com.qlangtech.tis.coredefine.module.action.IRCController;
import com.qlangtech.tis.coredefine.module.action.TargetResName;
import com.qlangtech.tis.datax.job.ServerLaunchToken;
import com.qlangtech.tis.datax.job.ServerLaunchToken.FlinkClusterType;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.order.center.IParamContext;
import com.qlangtech.tis.plugin.IPluginStore;
import org.apache.commons.lang.StringUtils;

import java.util.Optional;
import java.util.function.Function;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
@Public
public abstract class IncrStreamFactory implements Describable<IncrStreamFactory>, IRCController {

    public static Function<String, IncrStreamFactory> stubStreamFactory;

    public static IncrStreamFactory getFactory(String indexName) {
        if (StringUtils.isEmpty(indexName)) {
            throw new IllegalArgumentException("indexName:" + indexName + " can not be empty");
        }
        if (stubStreamFactory != null) {
            return stubStreamFactory.apply(indexName);
        }
        IPluginStore<IncrStreamFactory> store = TIS.getPluginStore(indexName, IncrStreamFactory.class);
        IncrStreamFactory k8sConfig = store.getPlugin();
        if (k8sConfig == null) {
            throw new IllegalStateException("key:" + indexName + " have not set k8s plugin");
        }
        return k8sConfig;
    }

    public abstract boolean supportRateLimiter();
    public abstract <RateLimiterStrategy> RateLimiterStrategy getRateLimiterStrategy();
    public abstract Integer getParallelism();

    public abstract ServerLaunchToken getLaunchToken(TargetResName indexName);
//    {
//        return ServerLaunchToken.createFlinkClusterToken().token(this.getClusterType(), indexName);
//        // return incrLaunchToken;
//    }

    @Override
    public boolean hasCreated(TargetResName collection) {
        return getLaunchToken(collection).isLaunchTokenExist();
    }
//  public abstract IRCController getIncrSync();

    /**
     * 取得集群部署类型类型
     *
     * @return
     */
    public abstract FlinkClusterType getClusterType();

    /**
     * 增量任务是否可恢复？例如，Flink重启之后，可以利用savepoint或者checkpoint恢复job
     *
     * @return
     */
    public abstract Optional<ISavePointSupport> restorable();

    /**
     * 缺的当前执行任务的状态
     *
     * @return
     */
    public abstract IFlinkIncrJobStatus getIncrJobStatus(TargetResName collection);

    public abstract <StreamExecutionEnvironment> StreamExecutionEnvironment createStreamExecutionEnvironment();


    @Override
    public final Descriptor<IncrStreamFactory> getDescriptor() {
        return TIS.get().getDescriptor(this.getClass());
    }


    /**
     * 支持Flink应用Savepoint功能
     */
    public interface ISavePointSupport {

        public boolean supportSavePoint();

        public String getSavePointRootPath();

        public default String createSavePointPath() {
            return getSavePointRootPath() + "/" + IFlinkIncrJobStatus.KEY_SAVEPOINT_DIR_PREFIX + IParamContext.getCurrentMillisecTimeStamp();
        }
    }
}
