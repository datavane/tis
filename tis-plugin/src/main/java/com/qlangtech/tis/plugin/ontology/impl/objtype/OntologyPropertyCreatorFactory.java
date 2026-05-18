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
package com.qlangtech.tis.plugin.ontology.impl.objtype;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qlangtech.tis.extension.IPropertyType;
import com.qlangtech.tis.extension.OneStepOfMultiSteps;
import com.qlangtech.tis.manage.common.Option;
import com.qlangtech.tis.plugin.IdentityName;
import com.qlangtech.tis.plugin.ds.CMeta;
import com.qlangtech.tis.plugin.ds.ColumnMetaData;
import com.qlangtech.tis.plugin.ds.ElementCreatorFactory;
import com.qlangtech.tis.plugin.ds.ViewContent;
import com.qlangtech.tis.plugin.ontology.OntologyProperty;
import com.qlangtech.tis.plugin.ontology.OntologyPropertyTypeRef;
import com.qlangtech.tis.plugin.ontology.OntologyType;
import com.qlangtech.tis.plugin.ontology.impl.typeref.DefaultPropertyTypeRef;
import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;
import com.qlangtech.tis.runtime.module.misc.IFieldErrorHandler;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/5/14
 * @see ObjectTypeProperties
 */
public class OntologyPropertyCreatorFactory implements ElementCreatorFactory<OntologyProperty> {

    private static final String KEY_NAME = "name";

    @Override
    public final String getTuplesKey() {
        return "_props";
    }

    @Override
    public CMeta.ParsePostMCols<OntologyProperty>
    parsePostMCols(IPropertyType propertyType, IControlMsgHandler msgHandler, Context context, String keyColsMeta,
                   JSONArray targetCols) {
        CMeta.ParsePostMCols<OntologyProperty> parseResult = new CMeta.ParsePostMCols<>();
        for (int i = 0; i < targetCols.size(); i++) {
            JSONObject o = (JSONObject) targetCols.get(i);
            parseResult.writerCols.add(OntologyPropertyJsonSerializer.deserialize(msgHandler, context, keyColsMeta, i
                    , o));
        }


        parseResult.validateFaild = context.hasErrors();
        if (parseResult.isVaild()) {

            Map<String, Collection<Integer>> duplicateCols = Maps.newHashMap();
            for (int i = 0; i < parseResult.writerCols.size(); i++) {
                OntologyProperty prop = parseResult.writerCols.get(i);
                if (prop == null || prop.name == null) {
                    continue;
                }
                Collection<Integer> sameKeys = duplicateCols.get(prop.name);
                if (sameKeys == null) {
                    sameKeys = Lists.newArrayList();
                    duplicateCols.put(prop.name, sameKeys);
                }
                sameKeys.add(i);
            }
            for (Map.Entry<String, Collection<Integer>> entry : duplicateCols.entrySet()) {
                if (entry.getValue().size() > 1) {
                    for (Integer duplicateIndex : entry.getValue()) {
                        msgHandler.addFieldError(context
                                , IFieldErrorHandler.joinField(keyColsMeta, Collections.singletonList(duplicateIndex)
                                        , KEY_NAME)
                                , IdentityName.MSG_ERROR_NAME_DUPLICATE);
                        parseResult.validateFaild = true;
                    }
                }
            }
        }
        return parseResult;
    }

    @Override
    public ViewContent getViewContentType() {
        return ViewContent.OntologyProps;
    }

    @Override
    public void appendExternalJsonProp(IPropertyType propertyType, JSONObject biz) {
        //ElementCreatorFactory.super.appendExternalJsonProp(propertyType, biz);
        ObjectTypeProfile objTypeProfile = OneStepOfMultiSteps.getPreviousStepInstance(ObjectTypeProfile.class, false);
        if (objTypeProfile != null) {
            // 说明是创建流程，更新流程'objTypeProfile' 为null
            List<ColumnMetaData> cols =
                    Objects.requireNonNull(objTypeProfile.binding, "binding can not be null").resolveTabCols();
            biz.put(ColumnMetaData.KEY_COLS_METADATA
                    , cols.stream().map((col) -> {
                        OntologyProperty prop = new OntologyProperty();
                        prop.name = col.getName();
                        DefaultPropertyTypeRef dftType = new DefaultPropertyTypeRef();
                        dftType.type = OntologyType.convert(col.getType()).getValue();
                        prop.typeRef = dftType;
                        prop.nullable = col.isNullable();
                        prop.pk = col.isPk();
                        prop.description = col.getComment();
                        return prop;
                    }).toList());
            biz.put("ontologyTypes", Option.toJson(OntologyType.availableTypes()));
        }
    }

    @Override
    public OntologyProperty createDefault(JSONObject targetCol) {
        return new OntologyProperty();
    }

    @Override
    public OntologyProperty create(JSONObject targetCol, BiConsumer<String, String> errorProcess) {
        return createDefault(targetCol);
    }
}