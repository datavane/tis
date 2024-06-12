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

package com.qlangtech.tis.plugin.datax.transformer.jdbcprop;

import com.alibaba.fastjson.annotation.JSONType;
import com.google.common.collect.Lists;
import com.qlangtech.tis.plugin.datax.transformer.TargetColumn;
import com.qlangtech.tis.plugin.datax.transformer.UDFDesc;

import java.util.List;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-06-12 16:56
 **/
@JSONType(serializer = PainTargetColumnSerializer.class)
public class PainTargetColumn extends TargetColumn {
    private final String colName;

    public PainTargetColumn(String colName) {
        this.colName = colName;
    }

    @Override
    public String getName() {
        return this.colName;
    }

    @Override
    public String identityValue() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<UDFDesc> getLiteria() {
        return Lists.newArrayList(new UDFDesc("column", this.colName));
    }
}
