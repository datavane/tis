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

package com.qlangtech.tis.runtime.module.misc.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONType;
import com.google.common.collect.Lists;
import com.qlangtech.tis.manage.common.IAjaxResult;
import com.qlangtech.tis.runtime.module.misc.impl.DefaultFieldErrorHandler.FieldError;
import com.qlangtech.tis.runtime.module.misc.impl.DefaultFieldErrorHandler.ItemsErrors;

import java.util.List;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2025-04-06 09:54
 **/
@JSONType(serializer = ListDetailedItemsErrorsSerializer.class)
class ListDetailedItemsErrors extends ItemsErrors {
    public List<FieldError> fieldsErrorList = Lists.newArrayList();

    @Override
    public JSON serial2JSON() {
        return convertItemsErrorList((fieldsErrorList));
    }

    public static JSONArray convertItemsErrorList(List<FieldError> fieldErrors) {
        JSONArray ferrs = new JSONArray();
        JSONObject o = null;
        for (FieldError ferr : fieldErrors) {
            o = new JSONObject();
            o.put("name", ferr.getFieldName());
            if ((ferr.getMsg()) != null) {
                o.put("content", ferr.getMsg());
            }
            if (ferr.itemsErrorList != null) {
                JSONArray subErrs = new JSONArray();
                for (ItemsErrors itemErros : ferr.itemsErrorList) {
                    subErrs.add(itemErros.serial2JSON());
                }
                o.put(IAjaxResult.KEY_ERROR_FIELDS, subErrs);
            }
            ferrs.add(o);
        }
        return ferrs;
    }
}
