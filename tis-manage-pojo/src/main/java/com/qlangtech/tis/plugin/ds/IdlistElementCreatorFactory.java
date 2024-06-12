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

import com.alibaba.citrus.turbine.Context;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.extension.IPropertyType;
import com.qlangtech.tis.plugin.ds.CMeta.ParsePostMCols;
import com.qlangtech.tis.runtime.module.misc.IFieldErrorHandler;

import java.util.function.BiConsumer;

/**
 *
 * //@see com.qlangtech.tis.extension.util.MultiItemsViewType.ViewFormatType
 */
public class IdlistElementCreatorFactory implements ElementCreatorFactory<CMeta> {
    @Override
    public CMeta createDefault() {
        return new CMeta();
    }

    @Override
    public CMeta create(JSONObject targetCol, BiConsumer<String, String> errorProcess) {
        CMeta cMeta = createDefault();
        String targetColName = targetCol.getString("name");
        boolean pk = targetCol.getBooleanValue("pk");
        cMeta.setDisable(targetCol.getBooleanValue("disable"));
        cMeta.setName(targetColName);
        cMeta.setPk(pk);
        return cMeta;
    }

    @Override
    public ParsePostMCols<CMeta> parsePostMCols(IPropertyType propertyType,
            IFieldErrorHandler msgHandler, Context context, String keyColsMeta, JSONArray targetCols) {
       throw new UnsupportedOperationException();
    }
}
