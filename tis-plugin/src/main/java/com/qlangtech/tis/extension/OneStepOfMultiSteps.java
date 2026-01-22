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
package com.qlangtech.tis.extension;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import com.qlangtech.tis.plugin.IPluginStore;
import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;
import com.qlangtech.tis.util.AttrValMap;
import com.qlangtech.tis.util.DefaultDescriptorsJSON;
import com.qlangtech.tis.util.DescribableJSON;
import com.qlangtech.tis.util.IPluginContext;
import org.apache.commons.collections.CollectionUtils;

import java.util.Objects;
import java.util.Optional;

/**
 * 支持多步骤完成的插件配置,其中的一个子步骤
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/1/13
 */
@JSONType(serializer = OneStepOfMultiStepsJsonSerializer.class)
public abstract class OneStepOfMultiSteps implements Describable<OneStepOfMultiSteps>,
        IPluginStore.ManipuldateProcessor {

    public enum Step {
        Step1(0), Step2(1), Step3(2), Step4(3), Step5(4), Step6(5), Step7(6);

        private final int stepIndex;

        Step(int stepIndex) {
            this.stepIndex = stepIndex;
        }

        public int getStepIndex() {
            return stepIndex;
        }
    }

    /**
     * 取得上一步插件的对象实例
     * <p>
     * 该方法从ThreadLocal的IPluginContext中获取前一步骤的插件实例。
     * 前一步的实例是在 manipuldateProcess() 方法中通过 currentCtx.put() 保存的。
     *
     * @param pluginClass 前一步插件的Class对象
     * @param <T>         插件类型
     * @return 前一步的插件实例
     * @throws MultiStepPluginException.StepPluginNotFoundException 如果找不到前一步的插件实例
     */
    public static <T extends OneStepOfMultiSteps> T getPreviousStepInstance(Class<T> pluginClass) {
        IPluginContext pluginContext = IPluginContext.getThreadLocalInstance();
        T plugin = pluginClass.cast(pluginContext.getContext().get(pluginClass.getName()));

        if (plugin == null) {
            throw new MultiStepPluginException.StepPluginNotFoundException(pluginClass.getName());
        }

        return plugin;
    }

    @JSONField(serialize = false)
    @Override
    public final Descriptor<OneStepOfMultiSteps> getDescriptor() {
        Descriptor<OneStepOfMultiSteps> descriptor = Describable.super.getDescriptor();
        if (!BasicDesc.class.isAssignableFrom(descriptor.getClass())) {
            throw new IllegalStateException("descriptor:" + descriptor.getClass().getName()
                    + " must extend from " + BasicDesc.class.getName());
        }
        return descriptor;
    }

    private static final String KEY_NEXT_STEP_PLUGIN_DESC = "nextStepPluginDesc";
    private static final String KEY_NEXT_STEP_PLUGIN_INDEX = "nextStepPluginIndex";
    private static final String KEY_FINAL_STEP = "finalStep";
    private static final String KEY_CURRENT_SAVED = "currentSaved";
    private static final String KEY_CURRENT_STEP_INDEX = "currentStepIndex";
    // 对应前几步保存的plugin内容
    private static final String KEY_STEP_SAVED_PLUGIN = "stepSavedPlugin";

    /**
     * 解析前端提交的步骤插件数据
     *
     * @param pluginContext      插件上下文
     * @param context            Turbine上下文
     * @param preStepSavedPlugin 前面步骤保存的插件数据（JSON数组）
     * @return 步骤插件实例数组
     */
    public static OneStepOfMultiSteps[] parseStepsPlugin(IControlMsgHandler pluginContext, Context context,
                                                         JSONArray preStepSavedPlugin) {
        OneStepOfMultiSteps[] preSavedStepPlugins = new OneStepOfMultiSteps[preStepSavedPlugin.size()];
        for (int i = 0; i < preStepSavedPlugin.size(); i++) {
            JSONObject item = preStepSavedPlugin.getJSONObject(i);
            preSavedStepPlugins[i] = (OneStepOfMultiSteps) AttrValMap.parseDescribableMap(Optional.empty(), item)
                    .createDescribable(pluginContext, context).getInstance();
        }
        return preSavedStepPlugins;
    }

    /**
     * 处理多步骤插件的当前步骤逻辑
     * <p>
     * 该方法会：
     * <ol>
     *   <li>解析前端提交的历史步骤数据</li>
     *   <li>调用子类的 processPreSaved 方法处理业务逻辑</li>
     *   <li>将当前步骤实例保存到Context中，供下一步使用</li>
     *   <li>构建并返回下一步的描述符信息</li>
     * </ol>
     *
     * @param pluginContext 插件上下文，包含HTTP请求数据
     * @param context       Turbine上下文，用于在步骤间传递数据
     * @throws MultiStepPluginException.StepProcessingException 当步骤处理失败时抛出
     * @see #processPreSaved(IPluginContext, Context, OneStepOfMultiSteps[])
     * @see MultiStepsSupportHost
     */
    @Override
    public final void manipuldateProcess(IPluginContext pluginContext, Optional<Context> context) {
        try {
            Context currentCtx = context.orElseThrow();

            // 1. 解析前置步骤数据
            OneStepOfMultiSteps[] preSavedStepPlugins = parsePreviousSteps(pluginContext, currentCtx);

            // 2. 处理当前步骤的业务逻辑
            processCurrentStep(pluginContext, currentCtx, preSavedStepPlugins);

            // 3. 构建并返回结果
            JSONObject result = buildStepResult(currentCtx);
            pluginContext.setBizResult(currentCtx, result);

        } catch (Exception e) {
            throw new MultiStepPluginException.StepProcessingException(
                    "step: " + this.getClass().getSimpleName(), e);
        }
    }

    /**
     * 解析前置步骤数据
     * 将前端提交的数组格式 [index1, item1, index2, item2, ...] 转换为索引化的JSONArray
     *
     * @param pluginContext 插件上下文
     * @param currentCtx    当前上下文
     * @return 前置步骤插件实例数组
     */
    private OneStepOfMultiSteps[] parsePreviousSteps(IPluginContext pluginContext, Context currentCtx) {
        JSONObject postContent = pluginContext.getJSONPostContent();
        JSONArray preStepSavedPlugin = postContent.getJSONArray(KEY_STEP_SAVED_PLUGIN);

        if (CollectionUtils.isEmpty(preStepSavedPlugin)) {
            return new OneStepOfMultiSteps[0];
        }

        JSONArray indexedSavedPlugin = indexStepPlugins(preStepSavedPlugin);
        return parseStepsPlugin((IControlMsgHandler) pluginContext, currentCtx, indexedSavedPlugin);
    }

    /**
     * 将数组格式的步骤数据转换为索引化的JSONArray
     * 输入格式：[index1, item1, index2, item2, ...]
     * 输出格式：JSONArray，其中 array[index] = item
     *
     * @param preStepSavedPlugin 前端提交的步骤数据
     * @return 索引化的JSONArray
     */
    private JSONArray indexStepPlugins(JSONArray preStepSavedPlugin) {
        JSONArray indexedSavedPlugin = new JSONArray();
        for (int i = 0; i < preStepSavedPlugin.size(); i += 2) {
            int index = preStepSavedPlugin.getIntValue(i);
            JSONObject item = preStepSavedPlugin.getJSONObject(i + 1);
            indexedSavedPlugin.set(index, item);
        }
        return indexedSavedPlugin;
    }

    /**
     * 处理当前步骤
     * 调用子类的业务逻辑处理方法，并将当前实例保存到Context中
     *
     * @param pluginContext         插件上下文
     * @param currentCtx            当前上下文
     * @param preSavedStepPlugins   前置步骤插件实例数组
     */
    private void processCurrentStep(IPluginContext pluginContext, Context currentCtx,
                                    OneStepOfMultiSteps[] preSavedStepPlugins) {
        this.processPreSaved(pluginContext, currentCtx, preSavedStepPlugins);
        // 将当前步骤实例放入上下文，供下一步使用
        currentCtx.put(this.getClass().getName(), this);
    }

    /**
     * 构建步骤处理结果
     * 包含当前步骤的保存数据和下一步的描述符信息
     *
     * @param currentCtx 当前上下文
     * @return 结果JSON对象
     */
    private JSONObject buildStepResult(Context currentCtx) {
        try {
            JSONObject saved = new JSONObject();
            BasicDesc descriptor = (BasicDesc) this.getDescriptor();

            // 保存当前步骤的数据
            DescribableJSON pluginJSON = new DescribableJSON(this, descriptor);
            saved.put(KEY_CURRENT_SAVED, pluginJSON.getItemJson());
            saved.put(KEY_CURRENT_STEP_INDEX, descriptor.getStep().stepIndex);

            // 处理下一步信息
            Optional<BasicDesc> nextPluginDesc = descriptor.nextPluginDesc();
            if (nextPluginDesc.isPresent()) {
                addNextStepInfo(saved, nextPluginDesc.get());
            }

            return saved;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 添加下一步的信息到结果中
     *
     * @param saved    结果JSON对象
     * @param nextDesc 下一步的描述符
     */
    private void addNextStepInfo(JSONObject saved, BasicDesc nextDesc) {
        DefaultDescriptorsJSON desc2Json = new DefaultDescriptorsJSON(nextDesc);
        saved.put(KEY_NEXT_STEP_PLUGIN_DESC, desc2Json.getDescriptorsJSON());
        saved.put(KEY_NEXT_STEP_PLUGIN_INDEX, nextDesc.getStep().stepIndex);
        saved.put(KEY_FINAL_STEP, nextDesc.nextPluginDesc().isEmpty());
    }

    /**
     * 处理前置步骤数据的钩子方法
     * <p>
     * 子类可以重写此方法来：
     * <ul>
     *   <li>访问前面步骤的数据</li>
     *   <li>执行当前步骤的业务逻辑</li>
     *   <li>将数据放入Context供下一步使用</li>
     * </ul>
     *
     * @param pluginContext         插件上下文
     * @param currentCtx            当前上下文
     * @param preSavedStepPlugins   前置步骤插件实例数组
     */
    protected void processPreSaved(IPluginContext pluginContext, Context currentCtx,
                                   OneStepOfMultiSteps[] preSavedStepPlugins) {
        // 子类可以重写此方法
    }

    /**
     * 步骤描述符基类
     */
    public static abstract class BasicDesc extends Descriptor<OneStepOfMultiSteps> {

        public BasicDesc() {
            super();
        }

        /**
         * 获取步骤枚举
         *
         * @return 步骤枚举值
         */
        public abstract Step getStep();

        /**
         * 获取下一步骤插件的descriptor
         * 如果已经是最后一步，则返回 Optional.empty()
         *
         * @return 下一步的描述符，如果是最后一步则为空
         */
        public abstract Optional<OneStepOfMultiSteps.BasicDesc> nextPluginDesc();

        /**
         * 获取步骤描述
         *
         * @return 步骤描述文本
         */
        public abstract String getStepDescription();
    }
}
