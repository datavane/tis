package com.qlangtech.tis.plugin.ds;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.collect.Maps;
import com.qlangtech.tis.plugin.IdentityName;
import org.apache.commons.lang.StringUtils;

import javax.management.Descriptor;
import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

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
public class CMeta implements Serializable, IColMetaGetter, IdentityName {


    public static final String FIELD_NAME = "name";



    public interface ElementCreatorFactory {


        CMeta createDefault();

        CMeta create(JSONObject targetCol);
    }

    public static CMeta create(String colName, JDBCTypes type) {
        return create(Optional.empty(), colName, type);
    }

    public static CMeta create(Optional<ElementCreatorFactory> elementCreator, String colName, JDBCTypes type) {
        CMeta cmeta = elementCreator.map((factory) -> {
            return factory.createDefault();
        }).orElse(new CMeta());// new CMeta();
        cmeta.setName(colName);
        cmeta.setType(DataTypeMeta.getDataTypeMeta(type).getType());
        return cmeta;
    }

    public static final String KEY_ELEMENT_CREATOR_FACTORY = "elementCreator";

    private String name;
    private DataType type;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {

        if (StringUtils.indexOf(name, "{") > -1) {
            throw new IllegalArgumentException("illegal param name:" + name);
        }

        this.name = name;
    }

    public DataType getType() {
        return type;
    }

    public void setType(DataType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "{" + "name='" + name + '\'' + ", type=" + type + '}';
    }

    @JSONField(serialize = false)
    @Override
    public Class<?> getDescribleClass() {
        return IdentityName.super.getDescribleClass();
    }
}
