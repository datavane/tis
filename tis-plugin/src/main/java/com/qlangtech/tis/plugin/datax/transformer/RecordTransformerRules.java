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

import com.alibaba.datax.core.job.ITransformerBuildInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qlangtech.tis.datax.impl.DataxReader;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.TISExtension;
import com.qlangtech.tis.extension.impl.SuFormProperties;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.ds.DataSourceMeta;
import com.qlangtech.tis.plugin.ds.ContextParamConfig;
import com.qlangtech.tis.plugin.ds.IColMetaGetter;
import com.qlangtech.tis.plugin.ds.ISelectedTab;
import com.qlangtech.tis.plugin.ds.RunningContext;
import com.qlangtech.tis.util.HeteroEnum;
import com.qlangtech.tis.util.IPluginContext;
import com.qlangtech.tis.util.UploadPluginMeta;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 为一条记录Record定义的 Transformer 转化规则
 *
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2023-05-07 12:35
 **/
public class RecordTransformerRules implements Describable<RecordTransformerRules> {


    public static Function<String, RecordTransformerRules> transformerRulesLoader4Test;

    public ITransformerBuildInfo createTransformerBuildInfo(IPluginContext pluginContext) {
        DataxReader dataxReader = Objects.requireNonNull(DataxReader.load(pluginContext, pluginContext.getCollectionName())
                , "dataX:" + pluginContext.getCollectionName() + " relevant DataXReader can not be null");
        return createTransformerBuildInfo(dataxReader);
    }

    /**
     * @param pluginContext
     * @param dataxReader
     * @param tabs
     * @return Map<String, Map < String, Function < RunningContext, Object>>>
     */
    public static Map<String /*tableName*/, Map<String /*contextParamName*/, Function<RunningContext, Object>>> contextParamValsGetterMapper(
            IPluginContext pluginContext, DataxReader dataxReader, List<ISelectedTab> tabs) {
        Map<String, Map<String, Function<RunningContext, Object>>> contextParamValsGetterMapper = Maps.newHashMap();
        for (ISelectedTab tab : tabs) {
            RecordTransformerRules transformerRules = RecordTransformerRules.loadTransformerRules(pluginContext, tab.getName());
            ITransformerBuildInfo transformerBuildInfo
                    = transformerRules.createTransformerBuildInfo(Objects.requireNonNull(dataxReader, "dataxReader can not be null"));
            transformerBuildInfo.overwriteColsWithContextParams(tab.getCols());
            if (transformerBuildInfo.containContextParams()) {
                contextParamValsGetterMapper.put(tab.getName(), transformerBuildInfo.contextParamValsGetter());
            }
        }
        return contextParamValsGetterMapper;
    }


    private ITransformerBuildInfo createTransformerBuildInfo(DataxReader dataxReader) {
        if (dataxReader == null) {
            throw new IllegalArgumentException("param dataXReader can not be null");
        }
        final RecordTransformerRules transformers = this;
        if (CollectionUtils.isEmpty(transformers.rules)) {
            throw new IllegalStateException("transformer" + " can not be empty");
        }

        return new ITransformerBuildInfo() {
            OverwriteColsWithContextParams overwriteColsWithContextParams;
            OverwriteCols transformerWithoutContextParams;

            @Override
            public boolean containContextParams() {
                return this.overwriteColsWithContextParams != null
                        && CollectionUtils.isNotEmpty(overwriteColsWithContextParams.getContextParams());
            }

            @Override
            public Map<String, Object> contextParamVals(RunningContext runningContext) {
                Map<String, Function<RunningContext, Object>> valsGetter = contextParamValsGetter();
                Map<String, Object> contextParamVals = Maps.newHashMap();
                valsGetter.forEach((key, getter) -> {
                    contextParamVals.put(key, getter.apply(runningContext));
                });
                return contextParamVals;
            }

            @Override
            public <CONTEXT extends RunningContext> Map<String, Function<CONTEXT, Object>> contextParamValsGetter() {
                if (!containContextParams()) {
                    throw new IllegalStateException("must containContextParams");
                }
                Map<String, Function<CONTEXT, Object>> contextParamVals = Maps.newHashMap();
                List<ContextParamConfig> contextParms = overwriteColsWithContextParams.getContextParams();
                for (ContextParamConfig contextParam : contextParms) {
                    Function<CONTEXT, Object> valGetter = contextParam.valGetter();
                    contextParamVals.put(contextParam.getKeyName(), valGetter);
                }
                return contextParamVals;
            }

            @Override
            public List<IColMetaGetter> originColsWithContextParams() {
                return Objects.requireNonNull(transformerWithoutContextParams, "please execute method overwriteColsWithContextParams first")
                        .appendSourceContextParams(dataxReader, true).originCols();
            }

            @Override
            public List<IColMetaGetter> tranformerColsWithoutContextParams() {
                return Objects.requireNonNull(this.transformerWithoutContextParams, "please execute method overwriteColsWithContextParams first");
            }

            @Override
            public <T extends IColMetaGetter> List<IColMetaGetter> overwriteColsWithContextParams(List<T> sourceCols) {
                this.transformerWithoutContextParams = transformers.overwriteCols(sourceCols);
                this.overwriteColsWithContextParams = transformerWithoutContextParams.appendSourceContextParams(dataxReader);
                return overwriteColsWithContextParams.getCols();
            }
        };
    }

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
     * udf集合相关出参的列
     *
     * @return
     */
    public final List<String> relevantColKeys() {
        return relevantTypedOutterParamStream().map((tcol) -> tcol.getName()).collect(Collectors.toList());
        //  return this.rules.stream().flatMap((r) -> r.getUdf().outParameters().stream().map((tcol) -> tcol.getName())).collect(Collectors.toList());
    }

    /**
     * 取得对应表所有出参集合
     *
     * @return
     */
    public final List<OutputParameter> relevantTypedOutterColKeys() {
        return relevantTypedOutterParamStream().collect(Collectors.toList());
    }

    public final Set<InParamer> relevantInColKeys() {
        return this.rules.stream().flatMap((r) -> r.getUdf().inParameters().stream()).collect(Collectors.toSet());
    }

    private Stream<OutputParameter> relevantTypedOutterParamStream() {
        return this.rules.stream().flatMap((r) -> r.getUdf().outParameters().stream());
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

//    public <T extends IColMetaGetter> List<IColMetaGetter> overwriteCols(List<T> sourceCols) {
//        return overwriteCols(sourceCols, null);
//    }

    public class OverwriteCols extends ArrayList<IColMetaGetter> {
        //  private final List<IColMetaGetter> cols;
        /**
         * 保存被替换的原有值类型
         */
        private ConcurrentMap<Integer, IColMetaGetter> previous;

        public OverwriteCols() {
            //  this.cols = cols;
            this(Collections.emptyList());
        }

        public OverwriteCols(List<IColMetaGetter> cols) {
            //  this.cols = cols;
            super(cols);
        }

        public List<IColMetaGetter> getCols() {
            return this;
        }

        private ConcurrentMap<Integer, IColMetaGetter> getPrevious() {
            if (this.previous == null) {
                this.previous = Maps.newConcurrentMap();
            }
            return this.previous;
        }

        public List<IColMetaGetter> originCols() {
            List<IColMetaGetter> originCols = Lists.newArrayList();
            IColMetaGetter metaGetter = null;
            for (int idx = 0; idx < this.size(); idx++) {
                metaGetter = this.getOrigin(idx);
                if (metaGetter != null) {
                    originCols.add(metaGetter);
                } else {
                    originCols.add(this.get(idx));
                }
            }
            return originCols;
        }


        private IColMetaGetter getOrigin(Integer idx) {
            return this.getPrevious().get(idx);
        }

        @Override
        public IColMetaGetter set(int index, IColMetaGetter element) {
            IColMetaGetter previous = super.set(index, element);
            if (previous != null) {
                this.getPrevious().putIfAbsent(index, previous);
            }
            return previous;
        }

        public OverwriteColsWithContextParams appendSourceContextParams(DataSourceMeta dsMeta) {
            return this.appendSourceContextParams(dsMeta, false);
        }

        /**
         * @param dsMeta
         * @param origin 取得最原始的字段类型
         * @return
         */
        public OverwriteColsWithContextParams appendSourceContextParams(DataSourceMeta dsMeta, boolean origin) {
            if (dsMeta == null) {
                throw new IllegalArgumentException("param dsMeta can not be null");
            }
            List<IColMetaGetter> rewriterResult = Lists.newArrayList(origin ? this.originCols() : (this));
            // 查看绑定入参
            Map<String, ContextParamConfig> dbContextParams = dsMeta.getDBContextParams();
            List<ContextParamConfig> contextParams = Lists.newArrayList();
            ContextParamConfig contextParam = null;
            for (InParamer inParamer : relevantInColKeys()) {
                if (inParamer.isContextParams()) {
                    contextParam = Objects.requireNonNull(dbContextParams.get(inParamer.getKey())
                            , "inParamer:" + inParamer.getKey() + " relevant ContextParam can not be null");
                    contextParams.add(contextParam);
                    rewriterResult.add(IColMetaGetter.create(inParamer.getKey(), contextParam.getDataType()));
                }
            }
            return new OverwriteColsWithContextParams(rewriterResult, contextParams);
        }
    }

    public class OverwriteColsWithContextParams extends OverwriteCols {
        private final List<ContextParamConfig> contextParams;

        public OverwriteColsWithContextParams(List<IColMetaGetter> cols, List<ContextParamConfig> contextParams) {
            super(cols);
            this.contextParams = contextParams;
        }

        public List<ContextParamConfig> getContextParams() {
            return this.contextParams;
        }
    }

    /**
     * 将原未经Transformer处理的cols，变化成经过Transformer装饰过的cols（添加了新的虚拟列），或者其他
     *
     * @param sourceCols
     * @return
     */
    public <T extends IColMetaGetter> OverwriteCols overwriteCols(List<T> sourceCols) {
        OverwriteCols rewriterResult = new OverwriteCols();
        Map<String, Integer> col2IdxBuilder = Maps.newHashMap();
        int idx = 0;
        for (IColMetaGetter col : sourceCols) {
            rewriterResult.add(col);
            col2IdxBuilder.put(col.getName(), (idx++));
        }

        for (OutputParameter colType : this.relevantTypedOutterColKeys()) {
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
