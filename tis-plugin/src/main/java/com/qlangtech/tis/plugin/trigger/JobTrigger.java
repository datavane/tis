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

package com.qlangtech.tis.plugin.trigger;

import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.fullbuild.IFullBuildContext;
import com.qlangtech.tis.manage.common.HttpUtils;
import com.qlangtech.tis.order.center.IParamContext;
import com.qlangtech.tis.plugin.IdentityName;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 触发任务执行扩展点
 */
public abstract class JobTrigger implements Describable<JobTrigger> {

    public static Optional<JobTrigger> getPartialTriggerFromContext(Context context) {
        Optional<JobTrigger> partialTrigger = Optional.ofNullable((JobTrigger) context.get(JobTrigger.class.getName()));
        return partialTrigger;
    }

    /**
     * 通过JobTrigger 插件设置需要执行的数据同步的 tabs，后由前端控制forward转向到 dataxAction的trigger方法上
     *
     * @param context
     * @param trigger
     */
    protected void setPartialTrigger2Context(Optional<Context> context, JobTrigger trigger) {
        if (!context.isPresent()) {
            throw new IllegalStateException("context must be present");
        }
        context.get().put(JobTrigger.class.getName(), trigger);
    }


    /**
     * 被选中用于单次同步过滤的的表对象
     *
     * @return
     */
    public abstract List<IdentityName> selectedTabs();

    public static Optional<JobTrigger> getTriggerFromHttpParam(IParamContext params) {
        final String partialTabs = params.getString(IFullBuildContext.KEY_PARTIAL_TABS_JOB_TRIGGER);
        if (StringUtils.isBlank(partialTabs)) {
            return Optional.empty();
        }

        List<IdentityName> targetTabs
                = Arrays.stream(StringUtils.split(partialTabs, ",")).map((id) -> IdentityName.create(id)).collect(Collectors.toList());
        JobTrigger trigger = new JobTrigger() {
            @Override
            public List<IdentityName> selectedTabs() {
                return targetTabs;
            }
        };
        return Optional.of(trigger);
    }

    public HttpUtils.PostParam getHttpPostSelectedTabsAsParam() {
        if (CollectionUtils.isEmpty(this.selectedTabs())) {
            throw new IllegalStateException("selected tabs can not be empty");
        }
        return new HttpUtils.PostParam(IFullBuildContext.KEY_PARTIAL_TABS_JOB_TRIGGER, this.selectedTabs().stream().map((tab) -> tab.identityValue())
                .collect(Collectors.joining(",")));
    }
}
