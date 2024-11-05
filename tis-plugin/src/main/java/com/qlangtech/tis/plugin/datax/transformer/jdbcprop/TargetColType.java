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
import com.qlangtech.tis.plugin.datax.transformer.OutputParameter;
import com.qlangtech.tis.plugin.datax.transformer.PluginLiteria;
import com.qlangtech.tis.plugin.datax.transformer.TargetColumn;
import com.qlangtech.tis.plugin.datax.transformer.UDFDesc;
import com.qlangtech.tis.plugin.ds.IColMetaGetter;
import com.qlangtech.tis.plugin.ds.TypeBase;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * https://github.com/alibaba/fastjson/wiki/JSONType_serializer
 *
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-06-10 10:10
 * @see JdbcPropertyElementCreatorFactory
 **/
@JSONType(serializer = TargetColTypeSerializer.class)
public final class TargetColType extends TypeBase implements PluginLiteria, Serializable {

    TargetColumn target;

    public static OutputParameter create(TargetColType colType) {
        return create(colType.getName(), colType);
    }

    public static OutputParameter create(String rename, TargetColType colType) {
        return OutputParameter.create(rename, colType.isVirtual(), colType.getType());
    }

    public boolean isVirtual() {
        return Objects.requireNonNull(target, "prop target can not be null").isVirtual();
    }

    @Override
    public List<UDFDesc> getLiteria() {

        List<UDFDesc> result = Lists.newArrayList(target.getLiteria());
        result.forEach((desc) -> {
            desc.addPair("type", this.getType().getTypeDesc());
        });
        return result;
    }

    @Override
    public String getName() {
        return target.getName();
    }

    public void setTarget(TargetColumn target) {
        this.target = target;
    }
}
