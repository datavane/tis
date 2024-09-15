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


import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2022-02-19 08:50
 **/
public class DataType implements Serializable {

    public static final String KEY_UNSIGNED = "UNSIGNED";

    public static DataType createVarChar(int size) {
        if (size < 1) {
            throw new IllegalArgumentException("illegal param size can not small than 1");
        }
        return new DataType(JDBCTypes.VARCHAR, size);
    }

    private final JDBCTypes type;
    public final String typeName;
    private final int columnSize;
    private final boolean unsigned;

    // decimal 的小数位长度
    private Integer decimalDigits = -1;

    public final int getType() {
        return type.getType();
    }

    @JSONField(serialize = false)
    public final JDBCTypes getJdbcType() {
        return this.type;
    }

    public DataType(JDBCTypes type) {
        this(type, -1);
    }

    public static DataType getType(JDBCTypes jdbcType) {
        return DataTypeMeta.getDataTypeMeta(jdbcType).getType();
    }

    //    public DataType(int type, String typeName) {
    //        this(type, typeName, -1);
    //    }
    public DataType(JDBCTypes type, int columnSize) {
        this(type, type.getLiteria(), columnSize, false);
    }

    /**
     * @param type
     * @param columnSize
     * @see java.sql.Types
     */
    public DataType(JDBCTypes type, String typeName, int columnSize, boolean unsigned) {
        this.type = type;
        this.typeName = typeName;
        this.columnSize = columnSize;
        this.unsigned = unsigned;

    }

    public static DataType create(Integer type, String typeName, Integer columnSize) {
        return new DataType(JDBCTypes.parse(Objects.requireNonNull(type, "param type can not be null")) //
                , typeName //
                , Objects.requireNonNull(columnSize, "param columnSize can not be null") //
                , StringUtils.containsIgnoreCase(typeName,
                KEY_UNSIGNED));
    }


    /**
     * is UNSIGNED
     */
    @JSONField(serialize = false)
    public boolean isUnsigned() {
        return this.unsigned;
        // return StringUtils.containsIgnoreCase(this.type.literia, KEY_UNSIGNED);
    }

    @JSONField(serialize = false)
    public String getUnsignedToken() {
        if (this.isUnsigned()) {
            return DataType.KEY_UNSIGNED;
        }
        return StringUtils.EMPTY;
    }

    @JSONField(serialize = false)
    public DataXReaderColType getCollapse() {
        switch (this.type) {
            case INTEGER:
            case TINYINT:
            case SMALLINT:
            case BIGINT:
                return DataXReaderColType.Long;
            case FLOAT:
            case DOUBLE:
            case REAL:
            case DECIMAL:
            case NUMERIC:
                return DataXReaderColType.Double;
            case DATE:
            case TIME:
            case TIMESTAMP:
                return DataXReaderColType.Date;
            case BIT:
            case BOOLEAN:
                return DataXReaderColType.Boolean;
            case BLOB:
            case BINARY:
            case LONGVARBINARY:
            case VARBINARY:
                return DataXReaderColType.Bytes;
            default:
                return DataXReaderColType.STRING;
        }
    }

    // @JSONField(serialize = false)
    public String getTypeDesc() {
        final String type = this.accept(new TypeVisitor<String>() {
            @Override
            public String intType(DataType type) {
                return "int";
            }

            @Override
            public String bigInt(DataType type) {
                return "bigint";
            }

            @Override
            public String floatType(DataType type) {
                return "float";
            }

            @Override
            public String doubleType(DataType type) {
                return "double";
            }

            @Override
            public String decimalType(DataType type) {
                return "decimal(" + type.getColumnSize() + "," + type.getDecimalDigits() + ")";
            }

            @Override
            public String dateType(DataType type) {
                return "date";
            }

            @Override
            public String timeType(DataType type) {
                return "time";
            }

            @Override
            public String timestampType(DataType type) {
                return "timestamp";
            }

            @Override
            public String bitType(DataType type) {
                return "bit";
            }

            @Override
            public String blobType(DataType type) {
                return "blob(" + type.getColumnSize() + ")";
            }

            @Override
            public String varcharType(DataType type) {
                return "varchar(" + type.getColumnSize() + ")";
            }

            @Override
            public String tinyIntType(DataType dataType) {
                return "tinyint";
            }

            @Override
            public String smallIntType(DataType dataType) {
                return "smallint";
            }

            @Override
            public String boolType(DataType dataType) {
                return "bool";
            }
        });

        return (this.isUnsigned() ? StringUtils.lowerCase(KEY_UNSIGNED) + " " + type : type);
    }


    public <T> T accept(TypeVisitor<T> visitor) {
        switch (this.type) {
            case INTEGER: {
                return visitor.intType(this);
            }
            case TINYINT:
                return visitor.tinyIntType(this);
            case SMALLINT:
                return visitor.smallIntType(this);
            case BIGINT:
                return visitor.bigInt(this);
            case FLOAT:
            case REAL:
                return visitor.floatType(this);
            case DOUBLE:
                return visitor.doubleType(this);
            case DECIMAL:
            case NUMERIC:
                return visitor.decimalType(this);
            case DATE:
                return visitor.dateType(this);
            case TIME:
                return visitor.timeType(this);
            case TIMESTAMP:
                return visitor.timestampType(this);
            case BIT:
                return visitor.bitType(this);
            case BOOLEAN:
                return visitor.boolType(this);
            case BINARY: {
                if ("boolean".equalsIgnoreCase(this.typeName)) {
                    return visitor.boolType(this);
                }
            }
            case BLOB:
            case LONGVARBINARY:
            case VARBINARY:
                return visitor.blobType(this);
            case VARCHAR:
            case LONGNVARCHAR:
            case NVARCHAR:
            case LONGVARCHAR:
                // return visitor.varcharType(this);
            default:
                return visitor.varcharType(this);// "VARCHAR(" + type.columnSize + ")";
        }
    }

    public Integer getDecimalDigits() {
        if (this.decimalDigits < 0) {
            // 设置 默认值
            DataTypeMeta typeMeta = Objects.requireNonNull(DataTypeMeta.getDataTypeMeta(this.type),
                    "type:" + this.type + ", relevant type meta can not be null");
            if (typeMeta.isContainDecimalRange()) {
                this.decimalDigits = typeMeta.getType().decimalDigits;
            }
        }
        return this.decimalDigits == null ? 0 : decimalDigits;
    }

    public DataType setDecimalDigits(Integer decimalDigits) {
        this.decimalDigits = decimalDigits;
        return this;
    }


    @JSONField(serialize = false)
    public String getS() {
        return this.type.type + "," + this.getColumnSize() + "," + (this.decimalDigits != null ? this.decimalDigits :
                StringUtils.EMPTY);
    }

    private static final Pattern patternDataType = Pattern.compile("(-?\\d+),(-?\\d+),(-?\\d*)");

    /**
     * 反序列化
     *
     * @param ser
     * @return
     */
    public static DataType ds(String ser) {

        Matcher matcher = patternDataType.matcher(ser);
        if (!matcher.matches()) {
            throw new IllegalStateException("val is illegal:" + ser);
        }
        DataType type = new DataType(JDBCTypes.parse(Integer.parseInt(matcher.group(1))),
                Integer.parseInt(matcher.group(2)));
        String d = matcher.group(3);
        if (StringUtils.isNotEmpty(d)) {
            type.decimalDigits = Integer.parseInt(d);
        }
        return type;
    }

    @Override
    public String toString() {
        //        return "{" +
        //                "type=" + type +
        //                ",typeName=" + this.typeName +
        //                ", columnSize=" + columnSize +
        //                ", decimalDigits=" + decimalDigits +
        //                '}';
        return this.getS();
    }

    public int getColumnSize() {
        if (this.columnSize < 0) {
            // 设置 默认值
            DataTypeMeta typeMeta = Objects.requireNonNull(DataTypeMeta.getDataTypeMeta(this.type),
                    "type:" + this.type + " relevant type meta can not be null");
            if (typeMeta.isContainColSize()) {
                return typeMeta.getType().columnSize;
            }
        }
        return this.columnSize;
    }

    public interface TypeVisitor<T> {
        default T intType(DataType type) {
            return bigInt(type);
        }

        T bigInt(DataType type);

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

        default T boolType(DataType dataType) {
            return bitType(dataType);
        }
    }

    public static class DefaultTypeVisitor<T> implements TypeVisitor<T> {
        @Override
        public T bigInt(DataType type) {
            return null;
        }

        @Override
        public T doubleType(DataType type) {
            return null;
        }

        @Override
        public T dateType(DataType type) {
            return null;
        }

        @Override
        public T timestampType(DataType type) {
            return null;
        }

        @Override
        public T bitType(DataType type) {
            return null;
        }

        @Override
        public T blobType(DataType type) {
            return null;
        }

        @Override
        public T varcharType(DataType type) {
            return null;
        }

        @Override
        public T intType(DataType type) {
            return null;
        }

        @Override
        public T floatType(DataType type) {
            return null;
        }

        @Override
        public T decimalType(DataType type) {
            return null;
        }

        @Override
        public T timeType(DataType type) {
            return null;
        }

        @Override
        public T tinyIntType(DataType dataType) {
            return null;
        }

        @Override
        public T smallIntType(DataType dataType) {
            return null;
        }

        @Override
        public T boolType(DataType dataType) {
            return null;
        }
    }
}
