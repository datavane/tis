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

package com.qlangtech.tis.plugin.datax.transformer;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.datax.common.element.ColumnAwareRecord;
import com.alibaba.fastjson2.annotation.JSONField;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.plugin.IEndTypeGetter;
import com.qlangtech.tis.plugin.IPluginStore.ManipuldateProcessor;
import com.qlangtech.tis.plugin.IdentityName;
import com.qlangtech.tis.plugin.datax.SelectedTab;
import com.qlangtech.tis.plugin.ds.CMeta;
import com.qlangtech.tis.util.IPluginContext;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * DataX 的UDF 定义 <br/>
 * https://github.com/alibaba/DataX/blob/master/transformer/doc/transformer.md
 * <p>
 * https://www.processon.com/diagraming/66580c3ee57102599640d679
 *
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2023-05-07 12:35
 **/
public abstract class UDFDefinition implements Describable<UDFDefinition>, ManipuldateProcessor, PluginLiteriaDesc, Serializable {

    protected static final String KEY_FROM = "from";
    protected static final String KEY_TO = "to";

    public final String getImpl() {
        return this.getClass().getName();
    }

    /**
     * 函数的出参
     *
     * @return
     */
    public abstract List<OutputParameter> outParameters();

    /**
     * 函数的入参数
     *
     * @return
     */
    public abstract List<InParamer> inParameters();

    /**
     * 对记录进行处理
     *
     * @param record
     */
    public abstract void evaluate(ColumnAwareRecord record);


    public static List<IdentityName> colsCandidate() {
        List<CMeta> colsCandidate = SelectedTab.getSelectedCols();
        return colsCandidate.stream().collect(Collectors.toList());
    }

    @Override
    public void manipuldateProcess(IPluginContext pluginContext, Optional<Context> context) {

        try {
            // 直接传输到前端UI上
            Context c = context.orElseThrow(() -> new IllegalStateException("context must be present"));

//            Map<String, Object> biz = Maps.newHashMap();
//            biz.put("item", (new DescribableJSON(this)).getItemJson());
//            biz.put("itemLiteria", this.getLiteria());

            pluginContext.setBizResult(c, this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @JSONField(serialize = false)
    @Override
    public final Descriptor<UDFDefinition> getDescriptor() {
        Descriptor<UDFDefinition> descriptor = Describable.super.getDescriptor();
        if (!BasicUDFDesc.class.isAssignableFrom(descriptor.getClass())) {
            throw new IllegalStateException("desc:" + descriptor.getClass().getSimpleName() + " must extend from " + BasicUDFDesc.class.getName());
        }
        return descriptor;
    }


    protected static abstract class BasicUDFDesc extends Descriptor<UDFDefinition> implements IEndTypeGetter {
        public BasicUDFDesc() {
            super();
        }

        @Override
        public final EndType getEndType() {
            EndType endType = getTransformerEndType();
            if (endType.category != EndTypeCategory.Transformer) {
                throw new IllegalStateException("endtype:" + endType + " category must be " + EndTypeCategory.Transformer);
            }
            return endType;
        }

        protected abstract EndType getTransformerEndType();

    }
}
