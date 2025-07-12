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

package com.qlangtech.tis.plugin.rate;

import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.config.ParamsConfig;
import com.qlangtech.tis.datax.DataXName;
import com.qlangtech.tis.extension.TISExtension;
import com.qlangtech.tis.manage.common.AppAndRuntime;
import com.qlangtech.tis.plugin.IPluginStore;
import com.qlangtech.tis.plugin.IPluginStore.BeforePluginSaved;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.Validator;
import com.qlangtech.tis.plugin.incr.IncrStreamFactory;
import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;
import com.qlangtech.tis.util.IPluginContext;

import java.util.Objects;
import java.util.Optional;

import com.qlangtech.tis.realtime.yarn.rpc.IncrRateControllerCfgDTO;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2025-07-07 15:21
 **/
public class IncrRateController extends ParamsConfig implements IPluginStore.AfterPluginSaved, BeforePluginSaved {
    private static final String INCR_RATE_CONTROLLER = IncrRateController.class.getSimpleName();
    @FormField(ordinal = 0, identity = true, type = FormFieldType.INPUTTEXT, validate = {Validator.require, Validator.identity})
    public String name;

    public Long lastModified;

    public static final String dftProcessName() {
        AppAndRuntime appAndRuntime = Objects.requireNonNull(AppAndRuntime.getAppAndRuntime(), "appAndRuntime can not be null");
        return String.valueOf(appAndRuntime.getAppName());
    }

    public static IncrRateController getRateController(DataXName dataXName) {
        return ParamsConfig.getItem(dataXName.getPipelineName(), INCR_RATE_CONTROLLER, false);
    }

    /**
     * 是否暂停？
     */
    @FormField(ordinal = 1, type = FormFieldType.ENUM, validate = {Validator.require})
    public Boolean pause;

    @FormField(ordinal = 2, validate = {Validator.require})
    public IncrRateParam rateCfg;

    @Override
    public void beforeSaved(IPluginContext pluginContext, Optional<Context> context) {
        this.lastModified = System.currentTimeMillis();
    }

    @Override
    public IncrRateController createConfigInstance() {
        return this;
    }

    @Override
    public void afterSaved(IPluginContext pluginContext, Optional<Context> context) {

    }

    @Override
    public String identityValue() {
        return this.name;
    }

    public IncrRateControllerCfgDTO createIncrRateControllerCfgDTO() {
        IncrRateControllerCfgDTO cfgDTO = new IncrRateControllerCfgDTO();
        cfgDTO.setPause(this.pause);
        cfgDTO.setPayloadParams(Objects.requireNonNull(this.rateCfg).getPayloadParams());
        cfgDTO.setControllerType(this.rateCfg.getControllerType());
        cfgDTO.setLastModified(this.lastModified);
        return cfgDTO;
    }

    @TISExtension
    public static class DftDesc extends BasicParamsConfigDescriptor {
        public DftDesc() {
            super(INCR_RATE_CONTROLLER);
        }

        @Override
        protected boolean validateAll(IControlMsgHandler msgHandler, Context context, PostFormVals postFormVals) {
            String nameVal = postFormVals.getField("name");
            IncrStreamFactory streamFactory = IncrStreamFactory.getFactory(nameVal);
            if (!Objects.requireNonNull(streamFactory, "streamFactory can not be null")
                    .supportRateLimiter()) {
                msgHandler.addErrorMessage(context, "由于在增量实例配置中没有开启开启流控开关，因此不能在运行时动态调整流控，详细请咨询项目管理员");
                return false;
            }
            return true;
        }

        @Override
        protected boolean verify(IControlMsgHandler msgHandler, Context context, PostFormVals postFormVals) {
            return this.validateAll(msgHandler, context, postFormVals);
        }
    }
}
