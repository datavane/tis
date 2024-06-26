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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qlangtech.tis.datax.IDataXNameAware;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.TISExtension;
import com.qlangtech.tis.extension.impl.SuFormProperties;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.Validator;
import com.qlangtech.tis.plugin.datax.transformer.jdbcprop.TargetColType;
import com.qlangtech.tis.plugin.ds.IColMetaGetter;
import com.qlangtech.tis.util.HeteroEnum;
import com.qlangtech.tis.util.IPluginContext;
import com.qlangtech.tis.util.UploadPluginMeta;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 为一条记录Record定义的 Transformer 转化规则
 *
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2023-05-07 12:35
 **/
public class RecordTransformerRules implements Describable<RecordTransformerRules> {


    public static Function<String, RecordTransformerRules> transformerRulesLoader4Test;

    /**
     * 加载基于数据通道的表转换（Transformer）规则
     *
     * @param pluginCtx
     * @param tableName
     * @return
     */
    public static RecordTransformerRules loadTransformerRules(IPluginContext pluginCtx, String tableName) {

        if (StringUtils.isEmpty(tableName)) {
            throw new IllegalArgumentException("param tableName can not be empty");
        }

        if (transformerRulesLoader4Test != null) {
            return transformerRulesLoader4Test.apply(tableName);
        }

        String rawContent = HeteroEnum.TRANSFORMER_RULES.identity + ":require,"
                + SuFormProperties.SuFormGetterContext.FIELD_SUBFORM_ID + "_" + tableName;


        for (RecordTransformerRules trule
                : HeteroEnum.TRANSFORMER_RULES.getPlugins(pluginCtx
                , UploadPluginMeta.parse(rawContent))) {
            return trule;
        }

        return null;
    }

    @FormField(ordinal = 1, type = FormFieldType.MULTI_SELECTABLE, validate = {})
    public List<RecordTransformer> rules = Lists.newArrayList();

    /**
     * udf集合相关的列
     *
     * @return
     */
    public final List<String> relevantColKeys() {
        return this.rules.stream().flatMap((r) -> r.getUdf().outParameters().stream().map((tcol) -> tcol.getName())).collect(Collectors.toList());
    }

    public final List<OutputParameter> relevantTypedColKeys() {
        return this.rules.stream().flatMap((r) -> r.getUdf().outParameters().stream()).collect(Collectors.toList());
    }

    public static List<RecordTransformer> getRules() {
//        RecordTransformer t = new RecordTransformer();
//
//        t.setType(DataType.createVarChar(32));
//        t.setTarget(StringUtils.EMPTY);
////        CopyValUDF cpUdf = new CopyValUDF();
////        cpUdf.from = "name";
//        t.setUdf(null);
        return Lists.newArrayList();
    }

    /**
     * 将原未经Transformer处理的cols，变化成经过Transformer装饰过的cols（添加了新的虚拟列），或者其他
     *
     * @param sourceCols
     * @return
     */
    public <T extends IColMetaGetter> List<IColMetaGetter> overwriteCols(List<T> sourceCols) {
        List<IColMetaGetter> rewriterResult = Lists.newArrayList();
        Map<String, Integer> col2IdxBuilder = Maps.newHashMap();
        int idx = 0;
        for (IColMetaGetter col : sourceCols) {
            rewriterResult.add(col);
            col2IdxBuilder.put(col.getName(), (idx++));
        }

        for (OutputParameter colType : this.relevantTypedColKeys()) {
            if (colType.isVirtual()) {
                getExistColIdx(true, colType, col2IdxBuilder);
                // 新增虚拟列
                col2IdxBuilder.put(colType.getName(), (idx++));
                rewriterResult.add(colType);
            } else {
                // 替换已有列
                Integer existIdx = getExistColIdx(false, colType, col2IdxBuilder);
                rewriterResult.set(existIdx, colType);
            }
        }

        return rewriterResult;
    }

    private static Integer getExistColIdx(boolean mustBeNull, OutputParameter colType, Map<String, Integer> col2IdxBuilder) {
        Integer existIdx = col2IdxBuilder.get(colType.getName());

        if (mustBeNull) {
            // 必须为空
            if (existIdx != null) {
                throw new IllegalStateException("colName:" + colType.getName() + " relevant table col conf must be null");
            }
        } else {
            // 不能为空
            if (existIdx == null) {
                throw new IllegalStateException("colName:" + colType.getName() + " relevant table col conf can not be null");
            }
        }

        return existIdx;
    }


    @TISExtension
    public static class DefaultDescriptor extends Descriptor<RecordTransformerRules> {
        public DefaultDescriptor() {
            super();
        }
    }

}
