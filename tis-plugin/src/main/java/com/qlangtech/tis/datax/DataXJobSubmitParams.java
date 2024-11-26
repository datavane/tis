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

import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.config.ParamsConfig;
import com.qlangtech.tis.config.k8s.ReplicasSpec;
import com.qlangtech.tis.coredefine.module.action.Specification;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.plugin.IPluginStore;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.Validator;
import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;
import com.qlangtech.tis.util.IPluginContext;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-04-27 17:03
 **/
public abstract class DataXJobSubmitParams extends ParamsConfig implements IPluginStore.AfterPluginSaved {
    private static final String LOCAL_DATAX_SUBMIT_PARAMS = "DataXSubmitParams";
    private static final String FIELD_NAME = "name";

    private static final Integer MEMORY_REQUEST_DEFAULT = 1024;

    @FormField(ordinal = 0, identity = true, type = FormFieldType.INPUTTEXT, validate = {Validator.require, Validator.identity})
    public String name;

    /**
     * 一个数据管道中最多的任务
     */
    @FormField(ordinal = 1, type = FormFieldType.INT_NUMBER, validate = {Validator.require, Validator.integer})
    public Integer maxJobs;

    /**
     * 单个管道中的并行度,需要满足： pipelineParallelism <= vmParallelism
     */
    @FormField(ordinal = 2, type = FormFieldType.INT_NUMBER, validate = {Validator.require, Validator.integer})
    public Integer pipelineParallelism;

    /**
     * 单机VM中的任务并行度
     */
    @FormField(ordinal = 3, type = FormFieldType.INT_NUMBER, validate = {Validator.require, Validator.integer})
    public Integer vmParallelism;

    @FormField(ordinal = 4, type = FormFieldType.INT_NUMBER, validate = {Validator.require, Validator.integer})
    public Integer memoryRequest;

    @FormField(ordinal = 5, type = FormFieldType.INT_NUMBER, validate = {Validator.require, Validator.integer})
    public Integer memoryLimit;


    /**
     * 取得内存规格参数
     *
     * @return
     */
    public String getJavaMemorySpec() {
        ReplicasSpec replicSpec = new ReplicasSpec();
        replicSpec.setMemoryLimit(Specification.parse(this.memoryLimit + Specification.MEMORY_UNIT_MEGABYTE));
        replicSpec.setMemoryRequest(Specification.parse(this.memoryRequest + Specification.MEMORY_UNIT_MEGABYTE));
        return replicSpec.toJavaMemorySpec(Optional.empty());
    }

    private transient ArrayBlockingQueue<Long> availableAreaController;

    private ArrayBlockingQueue<Long> getAvailableAreaController() {
        if (availableAreaController == null) {
            availableAreaController = new ArrayBlockingQueue<>(this.vmParallelism);
        }
        return availableAreaController;
    }

    @Override
    public void afterSaved(IPluginContext pluginContext, Optional<Context> context) {
        this.availableAreaController = null;
    }

    public final void execParallelTask(String dataxName, ParallelTaskRunnable runnable)
            throws IOException, InterruptedException, DataXJobSingleProcessorException {
        ArrayBlockingQueue<Long> parallelismController = this.getAvailableAreaController();
        Objects.requireNonNull(parallelismController);
        if (parallelismController.offer(System.currentTimeMillis(), 1, TimeUnit.HOURS)) {
            try {
                runnable.run();
            } finally {
                parallelismController.take();
            }
        } else {
            // 等待提交任务超时
            throw new DataXJobSingleProcessorException("dataX:" + dataxName + ",job submit timeout,wait "
                    + "for 1 hours");
        }
    }


    @Override
    public final DataXJobSubmitParams createConfigInstance() {
        return this;
    }


    public interface ParallelTaskRunnable {
        void run() throws IOException,
                InterruptedException, DataXJobSingleProcessorException;
    }

    public static DataXJobSubmitParams getDftIfEmpty() {
        return getSubmitParams().orElseGet(() -> {
            DataXJobSubmitParams dft = new DataXJobSubmitParams() {
            };
            dft.name = "default";
            dft.maxJobs = DataXJobSubmit.MAX_TABS_NUM_IN_PER_JOB;
            dft.vmParallelism = DataXJobSubmit.DEFAULT_PARALLELISM_IN_VM;
            dft.pipelineParallelism = DataXJobSubmit.DEFAULT_PARALLELISM_IN_VM;
            dft.memoryLimit = MEMORY_REQUEST_DEFAULT;
            dft.memoryRequest = MEMORY_REQUEST_DEFAULT;
            return dft;
        });
    }

    public static Integer dftMaxJobs() {
        return DataXJobSubmit.MAX_TABS_NUM_IN_PER_JOB;
    }

    public static Integer dftParallelism() {
        return DataXJobSubmit.DEFAULT_PARALLELISM_IN_VM;
    }

    public static Integer dftMemory() {
        return MEMORY_REQUEST_DEFAULT;
    }

    /**
     * 由于配置实例LocalDataXJobSubmitParams 为单例，只能返回一个，在TIS中配置需要保证其单例性
     *
     * @return
     */
    private static Optional<DataXJobSubmitParams> getSubmitParams() {
        List<DataXJobSubmitParams> cfgs = ParamsConfig.getItems(LOCAL_DATAX_SUBMIT_PARAMS);
        for (DataXJobSubmitParams cfg : cfgs) {
            return Optional.of(cfg);
        }
        return Optional.empty();
        // throw new IllegalStateException("have not found any submit params");
    }

    @Override
    public String identityValue() {
        return this.name;
    }

    // @TISExtension
    public abstract static class DefaultDesc extends BasicParamsConfigDescriptor {
        public DefaultDesc() {
            super(LOCAL_DATAX_SUBMIT_PARAMS);
        }

        @Override
        public String getDisplayName() {
            return LOCAL_DATAX_SUBMIT_PARAMS;
        }

        @Override
        protected boolean validateAll(IControlMsgHandler msgHandler, Context context, PostFormVals postFormVals) {
            return this.verify(msgHandler, context, postFormVals);
        }

        private static final String FIELD_MEMORY_LIMIT = "memoryLimit";
        private static final String FIELD_MEMORY_REQUEST = "memoryRequest";
        private static final String FIELD_PIPELINE_PARALLELISM = "pipelineParallelism";
        private static final String FIELD_VM_PARALLELISM = "vmParallelism";

        @Override
        protected boolean verify(IControlMsgHandler msgHandler, Context context, PostFormVals postFormVals) {
            Optional<DataXJobSubmitParams> params = getSubmitParams();
            DataXJobSubmitParams submitParams = postFormVals.newInstance();
            if (params.isPresent()) {
                DataXJobSubmitParams already = params.get();
                if(!StringUtils.equals(already.name,submitParams.name)){
                    msgHandler.addFieldError(context, FIELD_NAME, "该配置必须为单实例，已经存在ID为：" + already.name + "的配置实例");
                    return false;
                }
            }
            boolean validateFaild = false;

            if (submitParams.maxJobs < DataXJobSubmit.MAX_TABS_NUM_IN_PER_JOB) {
                msgHandler.addFieldError(context, "maxJobs", "不能小于" + DataXJobSubmit.MAX_TABS_NUM_IN_PER_JOB);
                validateFaild = true;
            }
            if (submitParams.memoryLimit < MEMORY_REQUEST_DEFAULT) {
                msgHandler.addFieldError(context, FIELD_MEMORY_LIMIT, "不能小于" + MEMORY_REQUEST_DEFAULT);
                validateFaild = true;
            }
            if (submitParams.memoryRequest < MEMORY_REQUEST_DEFAULT) {
                msgHandler.addFieldError(context, FIELD_MEMORY_REQUEST, "不能小于" + MEMORY_REQUEST_DEFAULT);
                validateFaild = true;
            }
            if (!validateFaild && (submitParams.memoryLimit < submitParams.memoryRequest)) {
                msgHandler.addFieldError(context, FIELD_MEMORY_REQUEST, "不能小于" + submitParams.memoryLimit);
                msgHandler.addFieldError(context, FIELD_MEMORY_LIMIT, "不能大于" + submitParams.memoryRequest);
                validateFaild = true;
            }
            //parallelism====================================
            if (submitParams.pipelineParallelism < DataXJobSubmit.DEFAULT_PARALLELISM_IN_VM) {
                msgHandler.addFieldError(context, FIELD_PIPELINE_PARALLELISM, "不能小于" + DataXJobSubmit.DEFAULT_PARALLELISM_IN_VM);
                validateFaild = true;
            }

            if (submitParams.vmParallelism < DataXJobSubmit.DEFAULT_PARALLELISM_IN_VM) {
                msgHandler.addFieldError(context, FIELD_VM_PARALLELISM, "不能小于" + DataXJobSubmit.DEFAULT_PARALLELISM_IN_VM);
                validateFaild = true;
            }

            if (!validateFaild && (submitParams.pipelineParallelism > submitParams.vmParallelism)) {
                msgHandler.addFieldError(context, FIELD_PIPELINE_PARALLELISM, "不能大于" + submitParams.vmParallelism);
                msgHandler.addFieldError(context, FIELD_VM_PARALLELISM, "不能小于" + submitParams.pipelineParallelism);
                validateFaild = true;
            }

            return !validateFaild;
        }
    }
}