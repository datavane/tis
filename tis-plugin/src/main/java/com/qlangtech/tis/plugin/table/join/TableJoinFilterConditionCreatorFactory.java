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
package com.qlangtech.tis.plugin.table.join;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONType;
import com.google.common.collect.Lists;
import com.qlangtech.tis.extension.IPropertyType;
import com.qlangtech.tis.manage.common.Option;
import com.qlangtech.tis.plugin.annotation.Validator;
import com.qlangtech.tis.plugin.datax.SelectedTab;
import com.qlangtech.tis.plugin.ds.CMeta;
import com.qlangtech.tis.plugin.ds.ElementCreatorFactory;
import com.qlangtech.tis.plugin.ds.ViewContent;
import com.qlangtech.tis.runtime.module.misc.EnumTypeJson;
import com.qlangtech.tis.runtime.module.misc.EnumTypeJsonSerializer;
import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;
import com.qlangtech.tis.runtime.module.misc.IFieldErrorHandler;
import com.qlangtech.tis.runtime.module.misc.impl.BasicDelegateMsgHandler;
import com.qlangtech.tis.util.IPluginContext;
import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

import static com.qlangtech.tis.plugin.table.join.TableJoinMatchConditionCreatorFactory.KEY_SOURCE_TAB_COLS;
import static com.qlangtech.tis.plugin.table.join.TableJoinMatchConditionCreatorFactory.KEY_TARGET_TAB_COLS;
import static com.qlangtech.tis.plugin.table.join.TableJoinMatchConditionCreatorFactory.getTargetCols;

/**
 * Factory for TableJoinFilterCondition
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/01/23
 */
public class TableJoinFilterConditionCreatorFactory implements ElementCreatorFactory<TableJoinFilterCondition> {

    @Override
    public String getTuplesKey() {
        // Corresponds to _filterConditionList property in frontend component
        return "_filterConditionList";
    }

    @Override
    public CMeta.ParsePostMCols<TableJoinFilterCondition> parsePostMCols(IPropertyType propertyType,
                                                                         IControlMsgHandler msgHandler,
                                                                         Context context, String keyColsMeta,
                                                                         JSONArray targetCols) {
        if (targetCols == null) {
            throw new IllegalStateException("targetCols can not be null");
        }

        CMeta.ParsePostMCols<TableJoinFilterCondition> conditions = new CMeta.ParsePostMCols<>();

        for (int i = 0; i < targetCols.size(); i++) {
            int tupleIndex = i;
            TableJoinFilterCondition filterCondition = new TableJoinFilterCondition();

            BasicDelegateMsgHandler msgHandle = new BasicDelegateMsgHandler(msgHandler) {
                @Override
                public void addFieldError(Context context, String fieldName, String msg, Object... params) {
                    super.addFieldError(context, IFieldErrorHandler.joinField(keyColsMeta,
                            Collections.singletonList(tupleIndex), fieldName), msg, params);
                }
            };

            JSONObject tuple = targetCols.getJSONObject(i);

            final String VALUE_KEY = "name";
            String value = null;
            // Parse table type
            JSONObject tableType = tuple.getJSONObject("tableType");
            value = tableType.getString(VALUE_KEY);
            if (validateField(msgHandle, context, "tableType", value)) {
                filterCondition.setTableType(TableType.parse(value));
            }

            // Parse column name
            JSONObject columnName = tuple.getJSONObject("columnName");
            value = columnName.getString(VALUE_KEY);
            if (validateField(msgHandle, context, "columnName", value)) {
                filterCondition.setColumnName(value);
            }

            // Parse operator
            JSONObject operator = tuple.getJSONObject("operator");
            value = operator.getString(VALUE_KEY);
            if (validateField(msgHandle, context, "operator", value)) {
                filterCondition.setOperator(Operator.parse(value));
            }

            // Parse value type
            JSONObject valueType = tuple.getJSONObject("valueType");
            value = valueType.getString(VALUE_KEY);
            if (validateField(msgHandle, context, "valueType", value)) {
                filterCondition.setValueType(ValueType.parse(value));
            }

            // Parse value
            JSONObject valueProp = tuple.getJSONObject("value");
            value = valueProp.getString(VALUE_KEY);
            if (validateField(msgHandle, context, "value", value)) {
                filterCondition.setValue(value);
            }

            conditions.writerCols.add(filterCondition);
        }

        conditions.validateFaild = context.hasErrors();
        return conditions;
    }

    private boolean validateField(IFieldErrorHandler msgHandler, Context context, String fieldKey, String fieldData) {
        if (!Validator.require.validate(msgHandler, context, fieldKey, fieldData)) {
            return false;
        }
        if (!Validator.none_blank.validate(msgHandler, context, fieldKey, fieldData)) {
            return false;
        }
        return true;
    }
    @JSONType(serializer = EnumTypeJsonSerializer.class)
    public enum TableType implements EnumTypeJson<TableType> {
        Primary("primary", "主表"), Dimension("dimension", "维表");
        private String token;
        private String literia;

        static TableType parse(String token) {
            if (StringUtils.isEmpty(token)) {
                throw new IllegalArgumentException("param token can not be empty");
            }
            for (TableType type : TableType.values()) {
                if (StringUtils.equals(type.token, token)) {
                    return type;
                }
            }
            throw new IllegalStateException("token:" + token + " can not match any token");
        }

        private Option opt() {
            return new Option(this.literia, this.token);
        }

        private TableType(String token, String literia) {
            this.token = token;
            this.literia = literia;
        }

        @Override
        public String getToken() {
            return this.token;
        }
    }
    @JSONType(serializer = EnumTypeJsonSerializer.class)
    public enum Operator implements EnumTypeJson<Operator> {
        EQUAL("=", "等于"),
        NOT_EQUAL("<>", "不等于"),
        GREATER_THAN(">", "大于"),
        LESS_THAN("<", "小于"),
        GREATER_THAN_OR_EQUAL(">=", "大于等于"),
        LESS_THAN_OR_EQUAL("<=", "小于等于");

        private String token;
        private String literia;

        static Operator parse(String token) {
            if (StringUtils.isEmpty(token)) {
                throw new IllegalArgumentException("param token can not be empty");
            }
            for (Operator op : Operator.values()) {
                if (StringUtils.equals(op.token, token)) {
                    return op;
                }
            }
            throw new IllegalStateException("token:" + token + " can not match any operator");
        }

        private Option opt() {
            return new Option(this.literia, this.token);
        }

        private Operator(String token, String literia) {
            this.token = token;
            this.literia = literia;
        }

        @Override
        public String getToken() {
            return token;
        }
    }
    @JSONType(serializer = EnumTypeJsonSerializer.class)
    public enum ValueType implements EnumTypeJson<ValueType> {
        STRING("string", "字符串"),
        NUMBER("number", "数字"),
        BOOLEAN("boolean", "布尔");

        private String token;
        private String literia;

        static ValueType parse(String token) {
            if (StringUtils.isEmpty(token)) {
                throw new IllegalArgumentException("param token can not be empty");
            }
            for (ValueType type : ValueType.values()) {
                if (StringUtils.equals(type.token, token)) {
                    return type;
                }
            }
            throw new IllegalStateException("token:" + token + " can not match any value type");
        }

        private Option opt() {
            return new Option(this.literia, this.token);
        }

        private ValueType(String token, String literia) {
            this.token = token;
            this.literia = literia;
        }

        @Override
        public String getToken() {
            return this.token;
        }
    }

    @Override
    public void appendExternalJsonProp(IPropertyType propertyType, JSONObject biz) {
        // Add metadata for operators
        List<Option> operators = Lists.newArrayList();
        for (Operator op : Operator.values()) {
            operators.add(op.opt());
        }
        biz.put("operators", Option.toJson(operators));

        // Add metadata for value types
        List<Option> valueTypes = Lists.newArrayList();
        for (ValueType type : ValueType.values()) {
            valueTypes.add(type.opt());
        }
        biz.put("valueTypes", Option.toJson(valueTypes));

        // Add metadata for table types
        List<Option> tableTypes = Lists.newArrayList();
        tableTypes.add(TableType.Primary.opt());
        tableTypes.add(TableType.Dimension.opt());
        biz.put("tableTypes", Option.toJson(tableTypes));

        biz.put(KEY_TARGET_TAB_COLS, getTargetCols());

        // 放在客户端的payload中传到服务端来的，然后再往客户端回传回去
        JSONObject post = IPluginContext.getThreadLocalInstance().getJSONPostContent();
        if (post != null) {
            biz.put(KEY_SOURCE_TAB_COLS, post.getJSONArray(KEY_SOURCE_TAB_COLS));
        } else {
            List<CMeta> colsCandidate = SelectedTab.getColsCandidate();
            biz.put(KEY_SOURCE_TAB_COLS, colsCandidate);
        }
    }

    private Option createOption(String value, String label) {
        return new Option(label, value);
        //        JSONObject opt = new JSONObject();
        //        opt.put("value", value);
        //        opt.put("label", label);
        //        return opt;
    }

    @Override
    public ViewContent getViewContentType() {
        return ViewContent.TableJoinFilterCondition;
    }

    @Override
    public TableJoinFilterCondition createDefault(JSONObject targetCol) {
        return new TableJoinFilterCondition();
    }

    @Override
    public TableJoinFilterCondition create(JSONObject targetCol, BiConsumer<String, String> errorProcess) {
        return new TableJoinFilterCondition();
    }
}
