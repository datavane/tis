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
package com.qlangtech.tis.extension.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.MultiStepsSupportHostDescriptor;
import com.qlangtech.tis.extension.OneStepOfMultiSteps;
import com.qlangtech.tis.extension.PluginFormProperties;
import com.qlangtech.tis.manage.common.Option;
import com.qlangtech.tis.runtime.module.action.IParamGetter;
import com.qlangtech.tis.util.DescribableJSON;
import com.qlangtech.tis.util.IPluginContext;
import com.qlangtech.tis.util.UploadPluginMeta;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-04-12 11:05
 */
public class RootFormProperties extends PluginFormProperties {
    public final Map<String, /*** fieldname*/PropertyType> propertiesType;
    private final Descriptor descriptor;
    private static final Logger logger = LoggerFactory.getLogger(RootFormProperties.class);

    private final boolean isSupportMultiStep;

    public RootFormProperties(Descriptor descriptor, Map<String, PropertyType> propertiesType) {
        this.propertiesType = Objects.requireNonNull(propertiesType, "param propertiesType can not be null");
        this.descriptor = descriptor;
        this.isSupportMultiStep = descriptor instanceof MultiStepsSupportHostDescriptor;
    }


    @Override
    public Descriptor getDescriptor() {
        return this.descriptor;
    }

    @Override
    public boolean containProperty(String fieldName) {
        return this.propertiesType.containsKey(fieldName);
    }

    @Override
    public Set<Map.Entry<String, PropertyType>> getKVTuples() {
        return this.propertiesType.entrySet();
    }

    @SuppressWarnings("all")
    @Override
    public JSON getInstancePropsJson(Object instance) {
        JSONObject vals = new JSONObject();
        List<Descriptor.ValueChangePipe> valChangePipes = null;
        Map<String, Object> pipeSourceVals = null;
        try {

            Object o = null;
            for (Map.Entry<String, PropertyType> entry : propertiesType.entrySet()) {

                o = entry.getValue().getFrontendOutput(instance);
                if (o == null) {
                    continue;
                }


                if (entry.getValue().isDescribable()) {
                    DescribableJSON djson = new DescribableJSON((Describable) o);
                    vals.put(entry.getKey(), djson.getItemJson());
                } else {
                    // 级联数据同步控件primary控件必须是普通非Describable的属性
                    Descriptor.ValueChangePipe valueChangePipe = descriptor.getValueChangePipe(entry.getKey(), false);
                    if (valueChangePipe != null) {
                        if (valChangePipes == null) {
                            valChangePipes = Lists.newArrayList();
                            pipeSourceVals = Maps.newHashMap();
                        }
                        pipeSourceVals.put(entry.getKey(), o);
                        valChangePipes.add(valueChangePipe);
                    }

                    vals.put(entry.getKey(), o);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("fetchKeys:" + propertiesType.keySet().stream().collect(Collectors.joining(","))//
                    + "，hasKeys:" + Arrays.stream(instance.getClass().getFields()).map((r) -> r.getName()).collect(Collectors.joining(",")), e);
        }

        try {
            if (CollectionUtils.isNotEmpty(valChangePipes)) {
                // 此处用以生成级联select控件下拉选项功能
                IPluginContext threadLocalContext = IPluginContext.getThreadLocalInstance();
                UploadPluginMeta uploadMeta =
                        UploadPluginMeta.createPluginMeta(threadLocalContext.getContext());
                IParamGetter params = new PluginPropParams(pipeSourceVals);
                for (Descriptor.ValueChangePipe pipe : valChangePipes) {
                    Map<String, List<? extends Option>> renderResult = pipe.render(uploadMeta, params);
                    for (Map.Entry<String, List<? extends Option>> entry : renderResult.entrySet()) {
                        // 使用"$" 作为前缀，在前端处理中可以方便与插件的property区别
                        // entry.getKey() 值可能中间包含“.”作为分割符，一个plugin 的property可以级联更新，另外一个Describable类型的子select控件属性
                        vals.put("$pipe_field$" + entry.getKey(), Option.toJson(entry.getValue()));
                    }
                }
            }
        } catch (Exception e) {
            //throw new RuntimeException(e);
            logger.warn(e.getMessage(), e);
        }
        return (vals);
    }

    public static class PluginPropParams implements IParamGetter {
        private final Map<String, Object> pipeSourceVals;

        public PluginPropParams(Map<String, Object> pipeSourceVals) {
            this.pipeSourceVals = Objects.requireNonNull(pipeSourceVals, "pipeSourceVals can not be null");
        }

        @Override
        public String getString(String key) {
            return String.valueOf((Object) getPropVal(key));
        }

        @SuppressWarnings("all")
        public <T> T getPropVal(String key) {
            return (T) Objects.requireNonNull(pipeSourceVals.get(key), "key:" + key + " relevant value can"
                    + " not be null");
        }
    }

    @Override
    public <T> T accept(IVisitor visitor) {
        return visitor.visit(this);
    }
}
