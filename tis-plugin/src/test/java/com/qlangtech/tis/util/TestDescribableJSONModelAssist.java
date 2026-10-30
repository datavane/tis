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
import com.qlangtech.tis.manage.common.Option;
import com.qlangtech.tis.manage.common.TestChild;
import com.qlangtech.tis.manage.common.UserProfile;
import com.qlangtech.tis.plugin.IEndTypeGetter;
import com.qlangtech.tis.plugin.IdentityName;
import junit.framework.TestCase;

/**
 * {@link DescribableJSON#getItemJson(PluginFormProperties)} 对 enumMode=model_assist 字段
 * 只下发「当前值」那一条枚举描述的约定：
 * <ul>
 *     <li>每个该模式字段最多一条，说明前端行内展示已选值不需要再请求全量枚举；</li>
 *     <li>值为 null 时整个 extractProps 不出现（否则 createEnumOptions 会在 e.name() 处 NPE）；</li>
 *     <li>普通 ENUM 字段（非 model_assist）不参与，避免把本可由 eprops 拿到的选项重复下发。</li>
 * </ul>
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2026/10/29
 * @see EnumFieldMode#MODEL_ASSIST
 */
public class TestDescribableJSONModelAssist extends TestCase {

    private static final String KEY_FIELD_RIGHT_ICON = "rightIcon";
    private static final String KEY_FIELD_SEX = "sex";

    public void testOnlyCurrentValDispatched() throws Exception {
        UserProfile profile = createProfile();
        profile.rightIcon = IEndTypeGetter.EndType.MySQL;

        JSONObject item = new DescribableJSON<>(profile).getItemJson();

        JSONObject enumDesc = getEnumDesc(item);
        assertNotNull("model_assist 字段的值必须随 item 下发", enumDesc);
        JSONArray opts = enumDesc.getJSONArray(KEY_FIELD_RIGHT_ICON);
        assertNotNull(opts);
        assertEquals("只下发当前值这一条，不能下发全量枚举", 1, opts.size());

        JSONObject opt = opts.getJSONObject(0);
        assertEquals(IEndTypeGetter.EndType.MySQL.getVal(), opt.getString(Option.KEY_VALUE));
        assertEquals(IEndTypeGetter.EndType.MySQL.name(), opt.getString(Option.KEY_LABEL));
        assertEquals(IEndTypeGetter.EndType.MySQL.getVal(), opt.getString(Option.KEY_END_TYPE));

        // 普通 ENUM 字段不走 model_assist，选项仍由 descriptor 的 eprops 提供
        assertNull(enumDesc.getJSONArray(KEY_FIELD_SEX));

        // item 的其它键不受影响
        assertNotNull(item.getJSONObject(AttrValMap.PLUGIN_EXTENSION_VALS));
        assertEquals(profile.identityValue(), item.getString(IdentityName.PLUGIN_IDENTITY_NAME));
    }

    public void testNullValDispatchedNothing() throws Exception {
        UserProfile profile = createProfile();
        profile.rightIcon = null;

        JSONObject item = new DescribableJSON<>(profile).getItemJson();
        // 未选值时不能把 null 传进 createEnumOptions（会在 e.name() 处 NPE）
        assertNull("未选值时不应下发 extractProps", item.get(DescriptorsJSON.KEY_EXTRACT_PROPS));
        assertNotNull(item.getJSONObject(AttrValMap.PLUGIN_EXTENSION_VALS));
    }

    private static JSONObject getEnumDesc(JSONObject item) {
        JSONObject extractProps = item.getJSONObject(DescriptorsJSON.KEY_EXTRACT_PROPS);
        if (extractProps == null) {
            return null;
        }
        return extractProps.getJSONObject(com.qlangtech.tis.extension.Descriptor.KEY_ENUM_PROP);
    }

    private static UserProfile createProfile() {
        UserProfile profile = new UserProfile();
        profile.name = "model_assist_test_user";
        profile.sex = TestChild.Sex.male;
        return profile;
    }
}
