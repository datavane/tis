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
package com.qlangtech.tis.plugin.ds;

import com.google.common.base.Joiner;
import com.qlangtech.tis.manage.common.Option;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.CaseInsensitiveMap;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class ColumnMetaData extends Option implements IColMetaGetter {

    public static final String KEY_COLS_METADATA = "cols-metadata";

    public static Map<String, ColumnMetaData> toMap(List<ColumnMetaData> cols) {
        TreeMap<String, ColumnMetaData> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        result.putAll(cols.stream().collect(Collectors.toMap((c) -> c.getName(), (c) -> c)));
        return result;
    }

    public static StringBuffer buildExtractSQL(String tableName, List<ColumnMetaData> cols) {
        return buildExtractSQL(tableName, false, cols);
    }

    public static CMeta convert(ColumnMetaData c) {
        return c.convert();
    }

    public static List<ColumnMetaData> convert(List<CMeta> cs) {
        int[] index = new int[1];
        return cs.stream().map((cm) -> {
            //int index, String key, DataType type, boolean pk, boolean nullable
            return new ColumnMetaData(index[0]++, cm.getName(), cm.getType(), cm.isPk(), cm.isNullable());

        }).collect(Collectors.toList());

//        ColumnMetaData c = this;
//        CMeta cmeta = createCmeta();
//        cmeta.setName(c.getName());
//        cmeta.setComment(c.getComment());
//        cmeta.setPk(c.isPk());
//        cmeta.setType(c.getType());
//        cmeta.setNullable(c.isNullable());
//
//        return c.convert();
    }

    public static void fillSelectedTabMeta(ISelectedTab tab,
                                           Function<ISelectedTab, Map<String, ColumnMetaData>> tableColsMetaGetter) {
        Map<String, ColumnMetaData> colsMeta = tableColsMetaGetter.apply(tab);
        ColumnMetaData colMeta = null;
        if (colsMeta.size() < 1) {
            throw new IllegalStateException("table:" + tab.getName() + " relevant cols meta can not be null");
        }
        List<CMeta> cols = tab.getCols();
        for (CMeta col : cols) {
            colMeta = colsMeta.get(col.getName());
            if (colMeta != null) {
                col.setPk(colMeta.isPk());
                col.setType(colMeta.getType());
                col.setComment(colMeta.getComment());
                col.setNullable(colMeta.isNullable());
//                throw new IllegalStateException("col:" + col.getName() + " can not find relevant 'col' on "
//                        + tab.getName() + ",exist Keys:[" + colsMeta.keySet().stream().collect(Collectors.joining(",")) + "]");
            }

        }
    }

    public static StringBuffer buildExtractSQL(String tableName, boolean useAlias, List<ColumnMetaData> cols) {
        if (CollectionUtils.isEmpty(cols)) {
            throw new IllegalStateException("tableName:" + tableName + "");
        }
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT ");
        sql.append(Joiner.on(",").join(cols.stream().map((r) -> {
            if (useAlias) {
                return "a." + r.getKey();
            } else {
                return r.getKey();
            }
        }).iterator())).append("\n");
        sql.append("FROM ").append(tableName);
        if (useAlias) {
            sql.append(" AS a");
        }
        return sql;
    }

    private final String key;

    private final DataType type;


    private final int index;
    private ReservedFieldType schemaFieldType;

    // private final String dbType;
    // private final String hiveType;
    // 是否是主键
    private final boolean pk;

    private final boolean nullable;

    /**
     * 列的注释
     */
    private transient String comment;


    /**
     * @param index
     * @param key      column名字
     * @param type     column类型 java.sql.Types
     * @param pk
     * @param nullable 是否可空
     */
    public ColumnMetaData(int index, String key, DataType type, boolean pk, boolean nullable) {
        super(key, key);
        this.pk = pk;
        this.key = key;
        this.type = type;
        this.index = index;
        this.nullable = nullable;
    }

    public ColumnMetaData(int index, String key, DataType type, boolean pk) {
        this(index, key, type, pk, !pk);
    }

    public String getComment() {
        return this.comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public ReservedFieldType getSchemaFieldType() {
        return schemaFieldType;
    }

    public void setSchemaFieldType(ReservedFieldType schemaFieldType) {
        this.schemaFieldType = schemaFieldType;
    }

    public boolean isNullable() {
        return this.nullable;
    }

    public int getIndex() {
        return index;
    }

    public String getKey() {
        return key;
    }

    @Override
    public DataType getType() {
        return type;
    }

    @Override
    public boolean isPk() {
        return this.pk;
    }


    private CMeta convert() {
        ColumnMetaData c = this;
        CMeta cmeta = createCmeta();
        cmeta.setName(c.getName());
        cmeta.setComment(c.getComment());
        cmeta.setPk(c.isPk());
        cmeta.setType(c.getType());
        cmeta.setNullable(c.isNullable());
        return cmeta;
    }

    protected CMeta createCmeta() {
        return new CMeta();
    }

    @Override
    public String toString() {
        return "ColumnMetaData{" + "key='" + key + '\'' + ", type=" + type + ", index=" + index + ", schemaFieldType" + "=" + schemaFieldType + ", pk=" + pk + '}';
    }

}
