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

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2023/9/12
 */
public class TestDataTypeMeta {

    @Test
    public void testCreateViewBiz() throws Exception {
        List<ColMeta> cms = new ArrayList<>();

        try (InputStream metaAssert = TestDataTypeMeta.class.getResourceAsStream("data_type_meta_assert.json")) {
            Assert.assertEquals( //
                    json2String(IOUtils.toString(metaAssert, Charset.forName("utf-8"))) //
                    , json2String(DataTypeMeta.createViewBiz(cms)));
        }

    }

    private static String json2String(Object obj) {

        if (obj instanceof String) {
            JSONObject json = com.alibaba.fastjson.JSON.parseObject((String) obj);
            return biz2String(json);
        } else {
            return json2String(biz2String(obj));
        }


    }

    private static String biz2String(Object obj) {
        if (obj instanceof String) {
            throw new IllegalArgumentException("obj:" + obj + " can not be String ");
        }
        return com.alibaba.fastjson.JSON.toJSONString(obj, SerializerFeature.DisableCircularReferenceDetect,
                SerializerFeature.PrettyFormat);
    }


}
