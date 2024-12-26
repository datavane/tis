/**
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.qlangtech.tis.plugin.ds;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.qlangtech.tis.extension.IPropertyType;
import com.qlangtech.tis.plugin.annotation.Validator;
import com.qlangtech.tis.plugin.datax.SelectedTab;
import com.qlangtech.tis.plugin.ds.CMeta.ParsePostMCols;
import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;
import com.qlangtech.tis.runtime.module.misc.IFieldErrorHandler;
import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * //@see com.qlangtech.tis.extension.util.MultiItemsViewType.ViewFormatType
 */
public class IdlistElementCreatorFactory implements ElementCreatorFactory<CMeta> {
    @Override
    public CMeta createDefault() {
        return new CMeta();
    }

    @Override
    public CMeta create(JSONObject targetCol, BiConsumer<String, String> errorProcess) {
        CMeta cMeta = createDefault();
        String targetColName = targetCol.getString("name");
        boolean pk = targetCol.getBooleanValue("pk");
        cMeta.setDisable(targetCol.getBooleanValue("disable"));
        cMeta.setName(targetColName);
        cMeta.setPk(pk);
        return cMeta;
    }

    @Override
    public ParsePostMCols<CMeta> parsePostMCols(IPropertyType propertyType,
                                                IControlMsgHandler msgHandler, Context context, String keyColsMeta, JSONArray targetCols) {
        if (targetCols == null) {
            throw new IllegalArgumentException("param targetCols can not be null");
        }
        CMeta.ParsePostMCols postMCols = new CMeta.ParsePostMCols();
        CMeta colMeta = null;


        String targetColName = null;
        DataType dataType = null;

        Map<String, Integer> existCols = Maps.newHashMap();
        Integer previousColIndex = null;
        boolean pk;
        for (int i = 0; i < targetCols.size(); i++) {
            JSONObject targetCol = targetCols.getJSONObject(i);
            int index = targetCol.getInteger("index") - 1;
            pk = targetCol.getBooleanValue("pk");
            targetColName = targetCol.getString("name");
            if (StringUtils.isNotBlank(targetColName) && (previousColIndex = existCols.put(targetColName, index)) != null) {
                msgHandler.addFieldError(context, keyColsMeta + "[" + previousColIndex + "]", "内容不能与第" + index + "行重复");
                msgHandler.addFieldError(context, keyColsMeta + "[" + index + "]", "内容不能与第" + previousColIndex + "行重复");
                // return false;
                postMCols.validateFaild = true;
                return postMCols;
            }
            if (!Validator.require.validate(msgHandler, context, keyColsMeta + "[" + index + "]", targetColName)) {
                postMCols.validateFaild = true;
            } else if (!Validator.db_col_name.validate(msgHandler, context, keyColsMeta + "[" + index + "]",
                    targetColName)) {
                postMCols.validateFaild = true;
            }


            colMeta = this.create(targetCol, (propKey, errMsg) -> {
                msgHandler.addFieldError(context
                        , IFieldErrorHandler.joinField(SelectedTab.KEY_FIELD_COLS, Collections.singletonList(index), propKey)
                        , errMsg);
                postMCols.validateFaild = true;
            });

            if (pk) {
                postMCols.pkHasSelected = true;
            }

            dataType = CMeta.parseType(targetCol, (propKey, errMsg) -> {
                msgHandler.addFieldError(context
                        , IFieldErrorHandler.joinField(SelectedTab.KEY_FIELD_COLS, Collections.singletonList(index), propKey)
                        , errMsg);
                postMCols.validateFaild = true;
            });

            if (dataType != null) {
                colMeta.setType(dataType);
                postMCols.writerCols.add(colMeta);
            }
        }

        return postMCols;
    }
}
