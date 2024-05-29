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

package com.qlangtech.tis.plugin.trigger.impl;

import com.alibaba.citrus.turbine.Context;
import com.google.common.collect.Lists;
import com.qlangtech.tis.datax.IDataxProcessor;
import com.qlangtech.tis.datax.impl.DataXCfgGenerator.GenerateCfgs;
import com.qlangtech.tis.datax.impl.DataxProcessor;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.TISExtension;
import com.qlangtech.tis.manage.common.AppAndRuntime;
import com.qlangtech.tis.manage.common.Option;
import com.qlangtech.tis.plugin.IPluginStore.AfterPluginSaved;
import com.qlangtech.tis.plugin.IdentityName;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.Validator;
import com.qlangtech.tis.plugin.trigger.JobTrigger;
import com.qlangtech.tis.util.IPluginContext;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 部份表同步任务触发执行器
 */
public class PartialJobsTrigger extends JobTrigger implements AfterPluginSaved {

    @FormField(ordinal = 1, type = FormFieldType.MULTI_SELECTABLE, validate = {Validator.require})
    public List<IdentityName> tabs = Lists.newArrayList();

    @Override
    public List<IdentityName> selectedTabs() {
        return this.tabs;
    }

    public static List<Option> getTabsCandidate() {
        AppAndRuntime appAndRuntime = AppAndRuntime.getAppAndRuntime();
        if (appAndRuntime == null) {
            return Collections.emptyList();
        }

        IDataxProcessor dataxProcessor = DataxProcessor.load(null, appAndRuntime.getAppName());
        GenerateCfgs dataxCfgFileNames = dataxProcessor.getDataxCfgFileNames(null, Optional.empty());

        return (dataxCfgFileNames.getTargetTabs().stream().map((tab) -> new Option(tab)).collect(Collectors.toList()));

    }

    @Override
    public void afterSaved(IPluginContext pluginContext, Optional<Context> context) {
//        if (!context.isPresent()) {
//            throw new IllegalStateException("context must be present");
//        }
        this.setPartialTrigger2Context(context, this);
//        context.get().put(JobTrigger.class.getName(), this);
        //  System.out.printf(this.tabs.stream().map((tab) -> tab.identityValue()).collect(Collectors.joining(",")));
    }

    @TISExtension
    public static class DefaultDesc extends Descriptor<JobTrigger> {
        @Override
        public String getDisplayName() {
            return "PartialTables";
        }
    }
}
