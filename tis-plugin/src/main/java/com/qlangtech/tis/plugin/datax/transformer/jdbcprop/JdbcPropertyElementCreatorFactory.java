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

package com.qlangtech.tis.plugin.datax.transformer.jdbcprop;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qlangtech.tis.extension.Descriptor.ParseDescribable;
import com.qlangtech.tis.extension.Descriptor.PluginValidateResult;
import com.qlangtech.tis.extension.IPropertyType;
import com.qlangtech.tis.plugin.IdentityName;
import com.qlangtech.tis.plugin.ValidatorCommons;
import com.qlangtech.tis.plugin.datax.SelectedTab;
import com.qlangtech.tis.plugin.datax.transformer.TargetColumn;
import com.qlangtech.tis.plugin.datax.transformer.impl.TransformerRuleElementCreatorFactory;
import com.qlangtech.tis.plugin.datax.transformer.impl.VirtualTargetColumn;
import com.qlangtech.tis.plugin.datax.transformer.impl.VirtualTargetColumn.VirtualTargetColumnDesc;
import com.qlangtech.tis.plugin.ds.CMeta;
import com.qlangtech.tis.plugin.ds.CMeta.ParsePostMCols;
import com.qlangtech.tis.plugin.ds.DataType;
import com.qlangtech.tis.plugin.ds.ElementCreatorFactory;
import com.qlangtech.tis.plugin.ds.ViewContent;
import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;
import com.qlangtech.tis.runtime.module.misc.IFieldErrorHandler;
import com.qlangtech.tis.runtime.module.misc.impl.BasicDelegateMsgHandler;
import com.qlangtech.tis.util.AttrValMap;
import com.qlangtech.tis.util.DescriptorsJSON;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-06-10 09:20
 **/
public class JdbcPropertyElementCreatorFactory implements ElementCreatorFactory<TargetColType> {

    private static final String KEY_TARGET_COL = "name";

    @Override
    public ViewContent getViewContentType() {
        return ViewContent.JdbcTypeProps;
    }

    @Override
    public void appendExternalJsonProp(IPropertyType propertyType, JSONObject biz) {
        TransformerRuleElementCreatorFactory.setSelectedTab(biz);
        biz.put("isList", propertyType.isCollectionType());
        if (propertyType.isCollectionType()) {
            setPropertyInCollectionFieldType(biz);
        }
        List<CMeta> colsCandidate = SelectedTab.getSelectedCols();

        biz.put("sourceTabCols", colsCandidate);
        biz.put("dftStrType", DataType.createVarChar(32));
        // biz.put();
        // ElementCreatorFactory.super.appendExternalJsonProp(propertyType, biz);
    }

    protected void setPropertyInCollectionFieldType(JSONObject biz) {
        biz.put("dftListElementDesc", DescriptorsJSON.desc(new VirtualTargetColumnDesc()));
    }


    @Override
    public ParsePostMCols<TargetColType> parsePostMCols(IPropertyType propertyType,
                                                        IControlMsgHandler msgHandler, Context context, String keyColsMeta, JSONArray targetCols) {
        boolean isCollection = propertyType.isCollectionType();

        ParsePostMCols<TargetColType> result = new ParsePostMCols<>();
        if (isCollection) {
            final int[] index = new int[1];
            Map<String, Collection<Integer>> duplicateCols = Maps.newHashMap();
            for (Object e : targetCols) {
                try {
                    JSONObject element = (JSONObject) e;
                    Object targetCol = element.get(KEY_TARGET_COL);

                    /**
                     * 这里处理分为两种场景 1，2
                     */
                    VirtualTargetColumn vcol = null;
                    if (targetCol instanceof String) {
                        throw new UnsupportedOperationException();
//                        /***************************************
//                         *  1：JSONSplitterUDF 表单提交
//                         **************************************/
//                        final String target = (String) targetCol;
//
//                        if (StringUtils.isEmpty(target)) {
//                            msgHandler.addFieldError(context //
//                                    , IFieldErrorHandler.joinField(keyColsMeta, Collections.singletonList(index[0]), KEY_TARGET_COL) //
//                                    , ValidatorCommons.MSG_EMPTY_INPUT_ERROR);
//                            result.validateFaild = true;
//                        }
//
//                        Collection<Integer> sameKeys = duplicateCols.get(target);
//                        if (sameKeys == null) {
//                            sameKeys = Lists.newArrayList();
//                            duplicateCols.put(target, sameKeys);
//                        }
//                        sameKeys.add(index[0]);
//
//                        vcol = new VirtualTargetColumn();
//                        vcol.name = target;
                    } else if (targetCol instanceof JSONObject) {
                        /***************************************
                         *  2：RecordTransformerRules 表单提交
                         **************************************/
                        AttrValMap attrVals = AttrValMap.parseDescribableMap(Optional.empty(), (JSONObject) targetCol);

                        BasicDelegateMsgHandler msgHandle = new BasicDelegateMsgHandler(msgHandler) {
                            @Override
                            public void addFieldError(Context context, String fieldName, String msg, Object... params) {
                                super.addFieldError(context
                                        , IFieldErrorHandler.joinField(keyColsMeta, Collections.singletonList(index[0]), fieldName)
                                        , msg, params);
                            }
                        };

                        PluginValidateResult targetColValidate = attrVals.validate(msgHandle, context, false);
                        if (targetColValidate.isValid()) {
                            vcol = targetColValidate.newInstance();
                            Collection<Integer> sameKeys = duplicateCols.get(vcol.getName());
                            if (sameKeys == null) {
                                sameKeys = Lists.newArrayList();
                                duplicateCols.put(vcol.getName(), sameKeys);
                            }
                            sameKeys.add(index[0]);
                        } else {
                            result.validateFaild = true;
                        }


                    } else {
                        throw new IllegalStateException("unsupported type:" + (targetCol == null ? "null" : targetCol.getClass().getSimpleName()));
                    }


                    DataType type = CMeta.parseType(element, (propKey, errMsg) -> {
                        msgHandler.addFieldError(context
                                , IFieldErrorHandler.joinField(keyColsMeta, Collections.singletonList(index[0]), propKey)
                                , errMsg);
                        result.validateFaild = true;
                    });
                    TargetColType targetColType = createDefault();

                    targetColType.setTarget(vcol);
                    targetColType.setType(type);
                    result.writerCols.add(targetColType);
                } finally {
                    index[0]++;
                }
            }

            for (Map.Entry<String, Collection<Integer>> entry : duplicateCols.entrySet()) {
                if (entry.getValue().size() > 1) {
                    for (Integer duplicateIndex : entry.getValue()) {
                        msgHandler.addFieldError(context //
                                , IFieldErrorHandler.joinField(keyColsMeta, Collections.singletonList(duplicateIndex), KEY_TARGET_COL) //
                                , IdentityName.MSG_ERROR_NAME_DUPLICATE);
                        result.validateFaild = true;
                    }
                }
            }
        } else {
            this.parseSinglePojo(msgHandler, context, keyColsMeta, targetCols, result);
        }

        return result;
    }

    private void parseSinglePojo(IControlMsgHandler pluginContext
            , Context context, String keyColsMeta, JSONArray targetCols, ParsePostMCols<TargetColType> result) {
        for (Object e : targetCols) {
            JSONObject element = (JSONObject) e;
            //  JSONObject type = element.getJSONObject("type");
            JSONObject target = element.getJSONObject(KEY_TARGET_COL);
            if (target == null) {
                pluginContext.addFieldError(context, keyColsMeta, ValidatorCommons.MSG_EMPTY_INPUT_ERROR);
                result.validateFaild = true;
                break;
            }
            DataType type = CMeta.parseType(element, (propKey, errMsg) -> {
//                    msgHandler.addFieldError(context
//                            , IFieldErrorHandler.joinField(keyColsMeta, Collections.singletonList(index), propKey)
//                            , errMsg);
                result.validateFaild = true;
            });

            AttrValMap valsMap = AttrValMap.parseDescribableMap(Optional.empty(), target);

            ParseDescribable describable = valsMap.createDescribable(pluginContext, context);
            TargetColumn targetCol = (TargetColumn) describable.getInstance();
            TargetColType targetColType = this.createDefault();
            targetColType.setTarget(targetCol);
            targetColType.setType(type);
            result.writerCols.add(targetColType);
            break;
        }
    }

    /**
     * 根据目标属性是否是List类型，创建
     *
     * @return
     * @see com.qlangtech.tis.plugin.datax.transformer.jdbcprop.TargetColType
     */
    @Override
    public TargetColType createDefault() {
        return new TargetColType();
    }

    @Override
    public TargetColType create(JSONObject targetCol, BiConsumer<String, String> errorProcess) {
        return null;
    }
}
