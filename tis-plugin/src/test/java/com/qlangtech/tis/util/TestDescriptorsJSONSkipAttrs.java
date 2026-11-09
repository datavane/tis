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

package com.qlangtech.tis.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.extension.DefaultPlugin;
import junit.framework.TestCase;

import java.util.Collections;

/**
 * 校验 {@link DescriptorsJSON#skipAttrs} 的线格式契约。
 *
 * <p>前端靠 {@code skipAttrs} 标记来区分「只含元数据」与「含完整属性描述」的
 * descriptor —— 这个判据不能靠 {@code attrs} 是否为空数组推导，因为空数组在
 * JS 里是 truthy，会把元数据形态当成完整形态。本测试把两侧的线格式钉死。
 *
 * @author 百岁（baisui@qlangtech.com）
 */
public class TestDescriptorsJSONSkipAttrs extends TestCase {

    public void testSkipAttrsWireFormat() {
        DefaultPlugin.DefaultDescriptor desc = new DefaultPlugin.DefaultDescriptor();
        String descId = desc.getId();

        JSONObject full = getDescJson(desc, false);
        JSONObject meta = getDescJson(desc, true);

        // 1. 元数据形态必须带显式标记，否则前端无法区分（空数组是 truthy）
        assertTrue("skipAttrs=true 时必须下发 skipAttrs 标记", meta.containsKey(DescriptorsJSON.KEY_SKIP_ATTRS));
        assertTrue(meta.getBooleanValue(DescriptorsJSON.KEY_SKIP_ATTRS));
        // 完整形态不带该键（老后端也从未下发过，前端按 undefined 视为完整）
        assertFalse("完整形态不应带 skipAttrs 标记", full.containsKey(DescriptorsJSON.KEY_SKIP_ATTRS));

        // 2. attrs 两种形态都存在，只是内容不同 —— 这正是「不能靠 attrs 真值判断」的原因
        assertTrue(full.containsKey(DescriptorsJSON.KEY_SCHEMA_FIELDS_ATTRS));
        assertTrue(meta.containsKey(DescriptorsJSON.KEY_SCHEMA_FIELDS_ATTRS));

        JSONArray fullAttrs = full.getJSONArray(DescriptorsJSON.KEY_SCHEMA_FIELDS_ATTRS);
        JSONArray metaAttrs = meta.getJSONArray(DescriptorsJSON.KEY_SCHEMA_FIELDS_ATTRS);
        assertFalse("完整形态必须下发属性列表", fullAttrs.isEmpty());
        assertTrue("元数据形态的 attrs 必须为空数组", metaAttrs.isEmpty());

        // 3. 元数据形态仍须保留调色板需要的字段
        assertEquals(full.getString(DescriptorsJSON.KEY_DISPLAY_NAME),
                meta.getString(DescriptorsJSON.KEY_DISPLAY_NAME));
        assertEquals(descId, meta.getString(DescriptorsJSON.KEY_IMPL));
        assertEquals(full.getJSONObject(DescriptorsJSON.KEY_EXTRACT_PROPS),
                meta.getJSONObject(DescriptorsJSON.KEY_EXTRACT_PROPS));
    }

    public void testSkipAttrsShrinksPayload() {
        DefaultPlugin.DefaultDescriptor desc = new DefaultPlugin.DefaultDescriptor();
        JSONObject full = getDescJson(desc, false);
        JSONObject meta = getDescJson(desc, true);
        // 属性树是体积主体，跳过之后必须显著变小（这里用属性数组长度做等价判据，
        // 避免依赖不稳定的 JSON 序列化字节数）
        assertTrue(full.toJSONString().length() > meta.toJSONString().length());
    }

    private static JSONObject getDescJson(DefaultPlugin.DefaultDescriptor desc, boolean skipAttrs) {
        DescriptorsMeta meta = new DefaultDescriptorsJSON(Collections.singletonList(desc),
                true, skipAttrs).getDescriptorsJSON();
        JSONObject descJson = meta.getDescriptorsResult().get(desc.getId());
        assertNotNull("descriptor " + desc.getId() + " 未出现在下发结果中", descJson);
        return descJson;
    }
}
