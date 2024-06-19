package com.qlangtech.tis.plugin.ds;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.collect.Lists;
import com.qlangtech.tis.plugin.IdentityName;
import com.qlangtech.tis.plugin.ValidatorCommons;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;

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

/**
 * TODO 考虑可以和 ColumnMetaData 合并
 * <p>
 * //@see com.qlangtech.tis.plugin.ds.ColumnMetaData
 */
public class CMeta extends TypeBase implements Serializable, IColMetaGetter, IdentityName {


    public static final String FIELD_NAME = "name";
    public static final String KEY_COLUMN_SIZE = "columnSize";
    public static final String KEY_DECIMAL_DIGITS = "decimalDigits";

    private String name;

    @Override
    public String getName() {
        return this.name;
    }

    public static DataType parseType(JSONObject targetCol, BiConsumer<String, String> errorProcess) {
        boolean hasError = false;
        JSONObject type = null;
        type = Objects.requireNonNull(targetCol, "targetCol can not be null").getJSONObject("type");
        if (type == null) {
            errorProcess.accept(FIELD_NAME, ValidatorCommons.MSG_EMPTY_INPUT_ERROR);
            return null;
        }
        Integer jdbcType = type.getInteger("type");

        DataTypeMeta typeMeta = DataTypeMeta.getDataTypeMeta(JDBCTypes.parse(jdbcType));


        Integer colSize = 0;
        if ((colSize = type.getInteger(KEY_COLUMN_SIZE)) == null && typeMeta.isContainColSize()) {
            errorProcess.accept(KEY_COLUMN_SIZE, ValidatorCommons.MSG_EMPTY_INPUT_ERROR);
            hasError = true;
        }
        Integer decimalDigits = 0;
        if ((decimalDigits = type.getInteger(KEY_DECIMAL_DIGITS)) == null && typeMeta.isContainDecimalRange()) {
            errorProcess.accept(KEY_DECIMAL_DIGITS, ValidatorCommons.MSG_EMPTY_INPUT_ERROR);
            hasError = true;
        }

        if (hasError) {
            return null;
        }

        DataType dataType = DataType.create(jdbcType, type.getString("typeName"), colSize);
        dataType.setDecimalDigits(decimalDigits);
        return dataType;
    }


    public static CMeta create(String colName, JDBCTypes type) {
        return create(Optional.empty(), colName, type);
    }

    public static CMeta create(Optional<ElementCreatorFactory<CMeta>> elementCreator, String colName, JDBCTypes type) {
        CMeta cmeta = elementCreator.map((factory) -> {
            return factory.createDefault();
        }).orElse(new CMeta());// new CMeta();
        cmeta.setName(colName);
        cmeta.setType(DataTypeMeta.getDataTypeMeta(type).getType());
        return cmeta;
    }

    public static final String KEY_ELEMENT_CREATOR_FACTORY = "elementCreator";

    //  private String name;
    // private DataType type;
    private Boolean pk = false;

    private String comment;
    private boolean nullable;


    /**
     * 该列是否有效？
     */
    private boolean disable = false;

    public CMeta() {
    }

    public boolean isDisable() {
        return this.disable;
    }

    public void setDisable(boolean disable) {
        this.disable = disable;
    }

    @Override
    public String identityValue() {
        return this.getName();
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isNullable() {
        return nullable;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    /**
     * 是否是主键，有时下游writer表例如clickhouse如果选择自动建表脚本，则需要知道表中的主键信息
     *
     * @return
     */
    @Override
    public boolean isPk() {
        return this.pk;
    }

    public void setPk(Boolean pk) {
        if (pk == null) {
            return;
        }
        this.pk = pk;
    }


    public void setName(String name) {
        if (StringUtils.indexOf(name, "{") > -1) {
            throw new IllegalArgumentException("illegal param name:" + name);
        }
        this.name = name;
    }


    @Override
    public String toString() {
        return "{" + "name='" + this.getName() + '\'' + ", type=" + this.getType() + '}';
    }

    @JSONField(serialize = false)
    @Override
    public Class<?> getDescribleClass() {
        return IdentityName.super.getDescribleClass();
    }

    public static class ParsePostMCols<T extends IMultiElement> {
        public List<T> writerCols = Lists.newArrayList();
        public boolean validateFaild = false;
        public boolean pkHasSelected = false;
    }
}
