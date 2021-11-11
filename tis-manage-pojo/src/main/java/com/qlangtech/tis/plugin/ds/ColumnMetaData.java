/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.plugin.ds;

import com.google.common.base.Joiner;
import com.qlangtech.tis.manage.common.Option;
import org.apache.commons.collections.CollectionUtils;

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

    /**
     * @param key  column名字
     * @param type column类型 java.sql.Types
     */
    public ColumnMetaData(int index, String key, DataType type, boolean pk) {
        super(key, key);
        this.pk = pk;
        this.key = key;
        this.type = type;
        this.index = index;
    }

    public ReservedFieldType getSchemaFieldType() {
        return schemaFieldType;
    }

    public void setSchemaFieldType(ReservedFieldType schemaFieldType) {
        this.schemaFieldType = schemaFieldType;
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

    public static class DataType {
        public final int type;
        public final int columnSize;

        public DataType(int type) {
            this(type, -1);
        }

        public ISelectedTab.DataXReaderColType collapse() {
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
                default:
                    return visitor.varcharType(this);// "VARCHAR(" + type.columnSize + ")";
            }
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
