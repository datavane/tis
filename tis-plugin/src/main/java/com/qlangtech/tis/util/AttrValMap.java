/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 *
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.util;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.plugin.annotation.Validator;
import com.qlangtech.tis.runtime.module.misc.IFieldErrorHandler;
import java.util.List;
import java.util.Map;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class AttrValMap {

    private final Map<String, JSONObject> /**
     * attrName
     */
    attrValMap;

    private final Descriptor descriptor;

    private IFieldErrorHandler msgHandler;

    public static List<AttrValMap> describableAttrValMapList(IFieldErrorHandler fieldErrorHandler, JSONArray itemsArray) {
        List<AttrValMap> describableAttrValMapList = Lists.newArrayList();
        AttrValMap describableAttrValMap = null;
        JSONObject itemObj = null;
        for (int i = 0; i < itemsArray.size(); i++) {
            itemObj = itemsArray.getJSONObject(i);
            describableAttrValMap = parseDescribableMap(fieldErrorHandler, itemObj);
            describableAttrValMapList.add(describableAttrValMap);
        }
        return describableAttrValMapList;
    }

    public static AttrValMap parseDescribableMap(IFieldErrorHandler fieldErrorHandler, com.alibaba.fastjson.JSONObject jsonObject) {
        String impl = null;
        Descriptor descriptor;
        impl = jsonObject.getString("impl");
        descriptor = TIS.get().getDescriptor(impl);
        if (descriptor == null) {
            throw new IllegalStateException("impl:" + impl + " can not find relevant ");
        }
        Object vals = jsonObject.get("vals");
        Map<String, JSONObject> attrValMap = Descriptor.parseAttrValMap(vals);
        // return descriptor.newInstance(attrValMap);
        return new AttrValMap(fieldErrorHandler, attrValMap, descriptor);
    }

    public AttrValMap(IFieldErrorHandler msgHandler, Map<String, JSONObject> attrValMap, Descriptor descriptor) {
        this.attrValMap = attrValMap;
        this.descriptor = descriptor;
        this.msgHandler = msgHandler;
    }

    /**
     * 校验表单输入内容
     *
     * @param context
     * @return true：校验没有错误 false：校验有错误
     */
    public boolean validate(Context context) {
        return this.descriptor.validate(msgHandler, context, attrValMap);
    }

    /**
     * 创建插件实例对象
     *
     * @return
     */
    public Descriptor.ParseDescribable createDescribable() {
        return this.descriptor.newInstance(attrValMap);
    }
}
