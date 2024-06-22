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

package com.qlangtech.tis.plugin.datax.transformer;

import com.qlangtech.tis.plugin.datax.transformer.jdbcprop.TargetColType;
import com.qlangtech.tis.plugin.ds.DataType;
import com.qlangtech.tis.plugin.ds.IColMetaGetter;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-06-22 11:15
 **/
public class OutputParameter implements IColMetaGetter {
    private final String name;
    private final boolean virtual;
    private final DataType type;

    public static OutputParameter create(TargetColType colType) {
        return create(colType.getName(), colType);
    }

    public static OutputParameter create(String rename, TargetColType colType) {
        return new OutputParameter(rename, colType.isVirtual(), colType.getType());
    }

    private OutputParameter(String name, boolean virtual, DataType type) {
        this.name = name;
        this.virtual = virtual;
        this.type = type;
    }

    @Override
    public boolean isPk() {
        return false;
    }

    public String getName() {
        return this.name;
    }

    public boolean isVirtual() {
        return this.virtual;
    }

    public DataType getType() {
        return type;
    }
}
