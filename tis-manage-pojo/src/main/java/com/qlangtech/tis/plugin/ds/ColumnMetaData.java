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

import java.io.Serializable;
import java.sql.Types;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class ColumnMetaData extends Option {

    public static final String KEY_COLS_METADATA = "cols-metadata";

    public static StringBuffer buildExtractSQL(String tableName, List<ColumnMetaData> cols) {
        return buildExtractSQL(tableName, false, cols);
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

    public DataType getType() {
        return type;
    }

    public boolean isPk() {
        return this.pk;
    }


    @Override
    public String toString() {
        return "ColumnMetaData{" +
                "key='" + key + '\'' +
                ", type=" + type +
                ", index=" + index +
                ", schemaFieldType=" + schemaFieldType +
                ", pk=" + pk +
                '}';
    }

    public static class DataType implements Serializable {

        public final int type;
        public final int columnSize;
        // decimal 的小数位长度
        private Integer decimalDigits;

        public DataType(int type) {
            this(type, -1);
        }

        public ISelectedTab.DataXReaderColType getCollapse() {
            switch (this.type) {
                case Types.INTEGER:
                case Types.TINYINT:
                case Types.SMALLINT:
                case Types.BIGINT:
                    return ISelectedTab.DataXReaderColType.Long;
                case Types.FLOAT:
                case Types.DOUBLE:
                case Types.DECIMAL:
                    return ISelectedTab.DataXReaderColType.Double;
                case Types.DATE:
                case Types.TIME:
                case Types.TIMESTAMP:
                    return ISelectedTab.DataXReaderColType.Date;
                case Types.BIT:
                case Types.BOOLEAN:
                    return ISelectedTab.DataXReaderColType.Boolean;
                case Types.BLOB:
                case Types.BINARY:
                case Types.LONGVARBINARY:
                case Types.VARBINARY:
                    return ISelectedTab.DataXReaderColType.Bytes;
                default:
                    return ISelectedTab.DataXReaderColType.STRING;
            }
        }

        public <T> T accept(TypeVisitor<T> visitor) {
            switch (this.type) {
                case Types.INTEGER:
                    return visitor.intType(this);
                case Types.TINYINT:
                    return visitor.tinyIntType(this);
                case Types.SMALLINT:
                    return visitor.smallIntType(this);
                case Types.BIGINT:
                    return visitor.longType(this);
                case Types.FLOAT:
                    return visitor.floatType(this);
                case Types.DOUBLE:
                    return visitor.doubleType(this);
                case Types.DECIMAL:
                    return visitor.decimalType(this);
                case Types.DATE:
                    return visitor.dateType(this);
                case Types.TIME:
                    return visitor.timeType(this);
                case Types.TIMESTAMP:
                    return visitor.timestampType(this);
                case Types.BIT:
                case Types.BOOLEAN:
                    return visitor.bitType(this);
                case Types.BLOB:
                case Types.BINARY:
                case Types.LONGVARBINARY:
                case Types.VARBINARY:
                    return visitor.blobType(this);
                case Types.VARCHAR:
                case Types.LONGNVARCHAR:
                case Types.NVARCHAR:
                case Types.LONGVARCHAR:
                    // return visitor.varcharType(this);
                default:
                    return visitor.varcharType(this);// "VARCHAR(" + type.columnSize + ")";
            }
        }

        public Integer getDecimalDigits() {
            return decimalDigits;
        }

        public void setDecimalDigits(Integer decimalDigits) {
            this.decimalDigits = decimalDigits;
        }

        /**
         * @param type       java.sql.Types
         * @param columnSize
         */
        public DataType(int type, int columnSize) {
            this.type = type;
            this.columnSize = columnSize;
        }

        @Override
        public String toString() {
            return "{" +
                    "type=" + type +
                    ", columnSize=" + columnSize +
                    ", decimalDigits=" + decimalDigits +
                    '}';
        }
    }

    public interface TypeVisitor<T> {
        default T intType(DataType type) {
            return longType(type);
        }

        T longType(DataType type);

        default T floatType(DataType type) {
            return doubleType(type);
        }

        T doubleType(DataType type);

        default T decimalType(DataType type) {
            return doubleType(type);
        }

        T dateType(DataType type);

        default T timeType(DataType type) {
            return timestampType(type);
        }

        T timestampType(DataType type);

        T bitType(DataType type);

        T blobType(DataType type);

        T varcharType(DataType type);

        default T tinyIntType(DataType dataType) {
            return intType(dataType);
        }

        default T smallIntType(DataType dataType) {
            return intType(dataType);
        }
    }
}
