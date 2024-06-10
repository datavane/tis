/**
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.qlangtech.tis.plugin.datax.transformer.jdbcprop;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.plugin.ds.CMeta.ParsePostMCols;
import com.qlangtech.tis.plugin.ds.ElementCreatorFactory;
import com.qlangtech.tis.plugin.ds.TypeBase;
import com.qlangtech.tis.plugin.ds.ViewContent;
import com.qlangtech.tis.runtime.module.misc.IFieldErrorHandler;

import java.util.function.BiConsumer;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-06-10 09:20
 **/
public class JdbcPropertyElementCreatorFactory implements ElementCreatorFactory<TypeBase> {

    @Override
    public ViewContent getViewContentType() {
        return ViewContent.JdbcTypeProps;
    }

    @Override
    public ParsePostMCols<TypeBase> parsePostMCols(
            IFieldErrorHandler msgHandler, Context context, String keyColsMeta, JSONArray targetCols) {
        return null;
    }

    /**
     * 根据目标属性是否是List类型，创建
     * @see com.qlangtech.tis.plugin.datax.transformer.jdbcprop.TargetColType
     * @see com.qlangtech.tis.plugin.datax.transformer.jdbcprop.VirtualColType
     * @return
     */
    @Override
    public TypeBase createDefault() {
        return null;
    }

    @Override
    public TypeBase create(JSONObject targetCol, BiConsumer<String, String> errorProcess) {
        return null;
    }
}
