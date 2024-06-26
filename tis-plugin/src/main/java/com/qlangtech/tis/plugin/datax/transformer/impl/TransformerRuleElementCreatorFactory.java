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

package com.qlangtech.tis.plugin.datax.transformer.impl;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.qlangtech.tis.extension.Descriptor.ParseDescribable;
import com.qlangtech.tis.extension.IPropertyType;
import com.qlangtech.tis.extension.SubFormFilter;
import com.qlangtech.tis.extension.impl.SuFormProperties;
import com.qlangtech.tis.plugin.ValidatorCommons;
import com.qlangtech.tis.plugin.datax.transformer.RecordTransformer;
import com.qlangtech.tis.plugin.datax.transformer.UDFDefinition;
import com.qlangtech.tis.plugin.ds.CMeta.ParsePostMCols;
import com.qlangtech.tis.plugin.ds.ColumnMetaData;
import com.qlangtech.tis.plugin.ds.ElementCreatorFactory;
import com.qlangtech.tis.plugin.ds.ViewContent;
import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;
import com.qlangtech.tis.runtime.module.misc.IFieldErrorHandler;
import com.qlangtech.tis.util.AttrValMap;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 *
 */
public class TransformerRuleElementCreatorFactory implements ElementCreatorFactory<RecordTransformer> {

    @Override
    public final ViewContent getViewContentType() {
        return ViewContent.TransformerRules;
    }

    @Override
    public RecordTransformer createDefault() {
        return new RecordTransformer();
    }

    @Override
    public void appendExternalJsonProp(IPropertyType propertyType, JSONObject biz) {
        this.setSelectedTab(biz);
    }

    public static void setSelectedTab(JSONObject biz) {
        SuFormProperties.SuFormGetterContext context = SuFormProperties.subFormGetterProcessThreadLocal.get();
        if (context == null || context.plugin == null) {
            throw new IllegalStateException(" can not get threadLocal bind instance subFormGetterProcessThreadLocal");
        }
        biz.put(SubFormFilter.PLUGIN_META_SUBFORM_DETAIL_ID_VALUE, context.getSubFormIdentityField());
    }

    @Override
    public RecordTransformer create(JSONObject targetCol, BiConsumer<String, String> errorProcess) {
        throw new UnsupportedOperationException();
    }


    @Override
    public ParsePostMCols<RecordTransformer> parsePostMCols(IPropertyType propertyType,
                                                            IControlMsgHandler msgHandler, Context context, String _keyColsMeta, JSONArray targetCols) {
        final String keyColsMeta = "rules";
        final String keyTarget = "target";
        final String keyUdf = "udf";
        ParsePostMCols<RecordTransformer> postMCols = new ParsePostMCols<>();
        RecordTransformer transformerRule = null;
        postMCols.validateFaild = false;

        // DataType dataType = null;
        UDFDefinition udf = null;
        JSONObject udfObj = null;
        AttrValMap valsMap = null;
        //  String targetColName = null;
        // 校验是否有相同的列的名称
        Map<String, List<Integer>> duplicateCols = Maps.newHashMap();
        for (int i = 0; i < targetCols.size(); i++) {
            final int index = i;
            JSONObject targetCol = targetCols.getJSONObject(index);
            udfObj = targetCol.getJSONObject(keyUdf);
            // targetColName = targetCol.getString(keyTarget);


//            if (!Validator.require.validate(msgHandler, context
//                    , IFieldErrorHandler.joinField(keyColsMeta, Collections.singletonList(index), keyTarget), targetColName)) {
//                postMCols.validateFaild = true;
//            } else {
//                List<Integer> cols = duplicateCols.get(targetColName);
//                if (cols == null) {
//                    cols = Lists.newArrayList();
//                    duplicateCols.put(targetColName, cols);
//                }
//                cols.add(index);
//            }

            transformerRule = new RecordTransformer();

//            dataType = CMeta.parseType(targetCol, (propKey, errMsg) -> {
//                msgHandler.addFieldError(context
//                        , IFieldErrorHandler.joinField(keyColsMeta, Collections.singletonList(index), propKey)
//                        , errMsg);
//                postMCols.validateFaild = true;
//            });

            if (udfObj == null) {
                msgHandler.addFieldError(context
                        , IFieldErrorHandler.joinField(keyColsMeta, Collections.singletonList(index), keyUdf)
                        , ValidatorCommons.MSG_EMPTY_INPUT_ERROR);
                postMCols.validateFaild = true;
            }
            if (udfObj == null) {
                continue;
            }

            for (Map.Entry<String, List<Integer>> entry : duplicateCols.entrySet()) {
                if (entry.getValue().size() > 1) {
                    for (Integer colIndex : entry.getValue()) {
                        msgHandler.addFieldError(context
                                , IFieldErrorHandler.joinField(keyColsMeta, Collections.singletonList(colIndex), keyTarget)
                                , "名称不能重复");
                    }
                    postMCols.validateFaild = true;
                    return postMCols;
                }
            }


            valsMap = AttrValMap.parseDescribableMap(Optional.empty(), udfObj);
            ParseDescribable describable = valsMap.createDescribable(msgHandler, context);
            udf = (UDFDefinition) describable.getInstance();
            transformerRule.setUdf(udf);
            // transformerRule.setType(dataType);
            //  transformerRule.setTarget(targetColName);
            postMCols.writerCols.add(transformerRule);
        }
        // postMCols.validateFaild = false;
        return postMCols;
    }
}
