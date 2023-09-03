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

/**
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2023/9/1
 */
public enum JDBCTypes {
    CHAR(Types.CHAR, "CHAR") //
    , LONGNVARCHAR(Types.LONGNVARCHAR, "LONGNVARCHAR") //
    , NVARCHAR(Types.NVARCHAR, "NVARCHAR") //
    , REAL(Types.REAL, "REAL") //
    , NUMERIC(Types.NUMERIC, "NUMERIC") //
    , VARCHAR(Types.VARCHAR, "VARCHAR") //
    , INTEGER(Types.INTEGER, "INTEGER") //
    , TINYINT(Types.TINYINT, "TINYINT") //
    , SMALLINT(Types.SMALLINT, "SMALLINT") //
    , BIGINT(Types.BIGINT, "BIGINT") //
    , FLOAT(Types.FLOAT, "FLOAT") //
    , DOUBLE(Types.DOUBLE, "DOUBLE") //
    , DECIMAL(Types.DECIMAL, "DECIMAL") //
    , DATE(Types.DATE, "DATE") //
    , TIME(Types.TIME, "TIME") //
    , TIMESTAMP(Types.TIMESTAMP, "TIMESTAMP") //
    , BIT(Types.BIT, "BIT") //
    , LONGVARCHAR(Types.LONGVARCHAR, "LONGVARCHAR") //
    , BOOLEAN(Types.BOOLEAN, "BOOLEAN") //
    , BLOB(Types.BLOB, "BLOB")//
    , BINARY(Types.BINARY, "BINARY")//
    , LONGVARBINARY(Types.LONGVARBINARY, "LONGVARBINARY")//
    , VARBINARY(Types.VARBINARY, "VARBINARY");


    final int type;
    final String literia;

    public static JDBCTypes parse(int type) {
        for (JDBCTypes t : JDBCTypes.values()) {
            if (t.type == type) {
                return t;
            }
        }

        throw new IllegalArgumentException("illegal type value:" + type);
    }

    private JDBCTypes(int type, String literia) {
        this.type = type;
        this.literia = literia;
    }

    public int getType() {
        return type;
    }

    public String getLiteria() {
        return literia;
    }

    @Override
    public String toString() {
        return "type=" + type + ", literia='" + literia + '\'';
    }
}
