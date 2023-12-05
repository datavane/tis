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

package com.qlangtech.tis.sql.parser.exception;

import com.facebook.presto.sql.tree.NodeLocation;


import java.util.Optional;

/**
 * 代表在编写joinSql,的sql语句中中存在的错误
 *
 * @author: baisui 百岁
 * @create: 2020-09-30 14:09
 **/
public class TisSqlFormatException extends RuntimeException {
    private final Optional<NodeLocation> location;
    private final Optional<String> sqlScript;

    public TisSqlFormatException(String msg, Optional<NodeLocation> location) {
        this(msg, Optional.empty(), location);
    }

    public TisSqlFormatException(String msg, Optional<String> sqlScript, Optional<NodeLocation> location) {
        super(msg);
        this.location = location;
        this.sqlScript = sqlScript;
    }

    public String summary() {
        StringBuffer summary = new StringBuffer(this.getMessage());
        if (location.isPresent()) {
            summary.append(",位置，行:" + location.get().getLineNumber() + ",列:" + location.get().getColumnNumber());
        }
        if (sqlScript.isPresent()) {
            summary.append("\n").append(sqlScript.get());
        }
        return summary.toString();
    }
}
