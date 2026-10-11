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

package com.qlangtech.tis.plugin.ontology;

import com.alibaba.fastjson.annotation.JSONField;
import com.qlangtech.tis.extension.MultiStepsSupportHost;
import com.qlangtech.tis.extension.MultiStepsSupportHostDescriptor;
import com.qlangtech.tis.extension.OneStepOfMultiSteps;
import com.qlangtech.tis.plugin.IPluginStore;
import com.qlangtech.tis.plugin.IdentityName;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.Validator;
import com.qlangtech.tis.util.IPluginContext;

import java.util.Objects;

/**
 * TIS 本体 - Action Type (操作类型)
 * 定义对象的写操作，包括参数、规则、提交条件等
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/9/6
 * @see <a href="/opt/misc/palantir-study/use-case-data-quality/arch/04-ontology-architecture.md">Ontology Architecture</a>
 */
@SuppressWarnings("all")
public abstract class OntologyAction extends Ontology
        implements IdentityName, MultiStepsSupportHost, IPluginStore.ManipuldateProcessor {

    public static final String KEY_ACTION_TYPE = "action-type";

    protected OneStepOfMultiSteps[] stepsPlugin;

    /**
     * 注意：此字段当前未使用，但由于实现了 IdentityName 接口，TIS 框架要求必须存在
     * Caution: This field is currently unused but required since we implement IdentityName
     */
    @FormField(identity = true, ordinal = 0, validate = {Validator.require, Validator.identity})
    public String useless;

    /**
     * 期望的步骤数<b>不再写死</b>，而是与 {@link MultiStepsSupportHostDescriptor#getStepDescriptionList()}
     * 同源。原先这里写死 {@code FIXED_ACTION_STEPS_LENGTH = 3}，而
     * {@link com.qlangtech.tis.plugin.ontology.impl.action.DefaultOntologyAction.DefaultDesc} 声明的是 4 步，
     * 于是保存时必然抛 {@code IllegalStateException}（{@link com.qlangtech.tis.extension.Descriptor}
     * 的 visitMultiStepsHost 把 {@code multiStepsSavedItems} 原样交给本方法）。改成从 descriptor 取，
     * 声明与断言就不可能再各说各话。
     */
    @Override
    @SuppressWarnings("unchecked")
    public void setSteps(OneStepOfMultiSteps[] stepsPlugin) {
        this.stepsPlugin = Objects.requireNonNull(stepsPlugin, "stepsPlugin can not be null");
        MultiStepsSupportHostDescriptor<OntologyAction> desc =
                (MultiStepsSupportHostDescriptor<OntologyAction>) this.getDescriptor();
        final int expectLength = desc.getStepDescriptionList().size();
        if (stepsPlugin.length != expectLength) {
            throw new IllegalStateException("stepsPlugin.length must be equal to " + expectLength
                    + ", but got " + stepsPlugin.length);
        }
    }

    @JSONField(serialize = false)
    @Override
    public OneStepOfMultiSteps[] getMultiStepsSavedItems() {
        return stepsPlugin;
    }

    @Override
    public void manipuldateProcess(IPluginContext pluginContext,
                                   com.qlangtech.tis.util.UploadPluginMeta pluginMeta,
                                   java.util.Optional<com.alibaba.citrus.turbine.Context> context) {
        // 子类可以覆盖此方法实现自定义持久化逻辑
        // 例如：验证跨步骤的业务规则、保存到自定义存储等
    }

    /**
     * 获取 Action 的名称（从第一步获取）
     *
     * @return action name
     */
    public abstract String getName();

    /**
     * 获取 Action 的显示名称
     *
     * @return display name
     */
    public abstract String getDisplayName();

    /**
     * 获取 Action 的描述
     *
     * @return description
     */
    public abstract String getDescription();
}
