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
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.MultiStepsSupportHost;
import com.qlangtech.tis.extension.OneStepOfMultiSteps;
import com.qlangtech.tis.extension.PluginFormProperties;
import com.qlangtech.tis.util.DescribableJSON;

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

    private final boolean isSupportMultiStep;

    public RootFormProperties(Descriptor descriptor, Map<String, PropertyType> propertiesType) {
        this.propertiesType = Objects.requireNonNull(propertiesType, "param propertiesType can not be null");
        this.descriptor = descriptor;
        this.isSupportMultiStep = descriptor instanceof MultiStepsSupportHost;
    }

    /**
     * 是否支持多步骤配置插件
     *
     * @return
     * @see MultiStepsSupportHost  Descriptor需要实现该接口
     */
    public boolean isSupportMultiStep() {
        return isSupportMultiStep;
    }

    public final List<OneStepOfMultiSteps.BasicDesc> getStepDescriptionList() {
        return ((MultiStepsSupportHost) descriptor).getStepDescriptionList();
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

    @Override
    public JSON getInstancePropsJson(Object instance) {
        JSONObject vals = new JSONObject();
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


                    vals.put(entry.getKey(), o);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("fetchKeys:" + propertiesType.keySet().stream().collect(Collectors.joining(",")) + "，hasKeys:" + Arrays.stream(instance.getClass().getFields()).map((r) -> r.getName()).collect(Collectors.joining(",")), e);
        }
        return (vals);
    }

    @Override
    public <T> T accept(IVisitor visitor) {
        return visitor.visit(this);
    }
}
