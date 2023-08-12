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

import java.sql.Types;
import java.util.*;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2023-08-10 13:36
 **/
public class DataTypeMeta {
    private final DataType type;

    private final Optional<ColSizeRange> colSizeRange;
    private final Optional<DecimalRange> decimalRange;


//            case :
//            case :
//

//            case Types.:
//            case Types.:
//            case Types.REAL:
//            case Types.:
//
//            case Types.DATE:
//            case Types.TIME:
//            case Types.:
//            case Types.BIT:
//            case Types.BOOLEAN:
//            case Types.BLOB:
//            case Types.BINARY:
//            case Types.LONGVARBINARY:
//            case Types.VARBINARY:


    public static final DataTypeMeta[] typeMetas //
            = new DataTypeMeta[]{
            new DataTypeMeta(DataType.createVarChar(32), new ColSizeRange(1, 2000))
            , new DataTypeMeta(new DataType(Types.INTEGER, "INTEGER"))
            , new DataTypeMeta(new DataType(Types.TINYINT, "TINYINT"))
            , new DataTypeMeta(new DataType(Types.SMALLINT, "SMALLINT"))
            , new DataTypeMeta(new DataType(Types.BIGINT, "BIGINT"))
            , new DataTypeMeta(new DataType(Types.FLOAT, "FLOAT"))
            , new DataTypeMeta(new DataType(Types.DOUBLE, "DOUBLE"))
            , new DataTypeMeta(new DataType(Types.DECIMAL, "DECIMAL", 20).setDecimalDigits(2)
            , new ColSizeRange(1, 46)
            , new DecimalRange(1, 20))
            , new DataTypeMeta(new DataType(Types.DATE, "DATE"))
            , new DataTypeMeta(new DataType(Types.TIME, "TIME"))
            , new DataTypeMeta(new DataType(Types.TIMESTAMP, "TIMESTAMP"))
            , new DataTypeMeta(new DataType(Types.BIT, "BIT"))
            , new DataTypeMeta(new DataType(Types.BOOLEAN, "BOOLEAN"))
            , new DataTypeMeta(new DataType(Types.BLOB, "BLOB"), new ColSizeRange(1, 2000))
            , new DataTypeMeta(new DataType(Types.BINARY, "BINARY"), new ColSizeRange(1, 2000))
            , new DataTypeMeta(new DataType(Types.LONGVARBINARY, "LONGVARBINARY", 1000), new ColSizeRange(1, 2000))
            , new DataTypeMeta(new DataType(Types.VARBINARY, "VARBINARY", 1000), new ColSizeRange(1, 4000))
    };

    public static final Map<Integer, DataTypeMeta> typeMetasDic;

    static {
        Map<Integer, DataTypeMeta> dic = new HashMap<>();
        for (DataTypeMeta type : typeMetas) {
            dic.put(type.type.type, type);
        }
        typeMetasDic = Collections.unmodifiableMap(dic);
    }

    /**
     * @param type
     * @return
     * @see java.sql.Types
     */
    public static DataTypeMeta getDataTypeMeta(Integer type) {
        return Objects.requireNonNull(typeMetasDic.get(type)
                , "type:" + type + " relevant "
                        + DataTypeMeta.class.getSimpleName() + " can not be null");
    }

    public DataTypeMeta(DataType type) {
        this(type, Optional.empty(), Optional.empty());
    }

    public DataTypeMeta(DataType type, ColSizeRange colSizeRange) {
        this(type, Optional.of(colSizeRange), Optional.empty());
    }

    public DataTypeMeta(DataType type, ColSizeRange colSizeRange, DecimalRange decimalRange) {
        this(type, Optional.of(colSizeRange), Optional.of(decimalRange));
    }

    private DataTypeMeta(DataType type, Optional<ColSizeRange> colSizeRange, Optional<DecimalRange> decimalRange) {
        this.type = type;
        this.colSizeRange = colSizeRange;
        this.decimalRange = decimalRange;
    }

    public DataType getType() {
        return this.type;
    }

    public boolean isContainColSize() {
        return this.colSizeRange.isPresent();
    }

    public ColSizeRange getColsSizeRange() {
        return this.colSizeRange.orElse(null);
    }

    public boolean isContainDecimalRange() {
        return this.decimalRange.isPresent();
    }

    public DecimalRange getDecimalRange() {
        return this.decimalRange.orElse(null);
    }
}
