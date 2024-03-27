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
package com.qlangtech.tis.trigger.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.google.common.collect.Lists;
import com.qlangtech.tis.extension.impl.IOUtils;
import com.qlangtech.tis.util.DescriptorsJSONResult;
import com.qlangtech.tis.web.start.TisAppLaunch;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2012-11-14
 */
public class JsonUtil {

    private JsonUtil() {
    }

    /**
     * 将map中的内容序反列化成一个map结果
     *
     * @param value
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T extends HashMap<String, String>> T deserialize(String value, T object) {
        try {
            JSONTokener tokener = new JSONTokener(value);
            JSONObject json = new JSONObject(tokener);
            Iterator it = json.keys();
            String key = null;
            while (it.hasNext()) {
                key = (String) it.next();
                object.put(key, json.getString(key));
            }
            return object;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将一个map对象序列化成一个json字符串
     *
     * @param param
     * @return
     */
    public static String serialize(Map<String, String> param) {
        JSONObject json = new JSONObject(param);
        return toString(json);
    }

    static {

        com.alibaba.fastjson.serializer.ObjectSerializer serializer = new ObjectSerializer() {
            @Override
            public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
                try {
                    //  SerializeWriter out = serializer.out;

                    UnCacheString value = (UnCacheString) object;
                    Objects.requireNonNull(value, "callable of " + fieldName + " can not be null");

                    //  out.writeString(value.getValue());

                    serializer.write(value.getValue());

                } catch (Exception e) {
                    throw new IOException(e);
                }
            }
        };

        ObjectWriter descSerializer = new ObjectWriter() {
            @Override
            public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
                DescriptorsJSONResult value = (DescriptorsJSONResult) object;
                Objects.requireNonNull(value, "callable of " + fieldName + " can not be null");
                jsonWriter.writeRaw(value.toJSONString());
            }

        };

        com.alibaba.fastjson2.JSON.register(DescriptorsJSONResult.class, descSerializer);
        SerializeConfig.global.put(UnCacheString.class, serializer);
        //  SerializeConfig.global.put(DescriptorsJSONResult.class, descSerializer);
    }

    public static <T> T[] toArray(Class<T> elementClazz, JSONArray ms) {
        T[] t = (T[]) Array.newInstance(elementClazz, ms.size());

        int i = 0;
        for (Object e : ms.toArray()) {
            t[i++] = (T) e;
        }

        return t;
    }

    /**
     * 比较两个json 是否相等
     *
     * @param o1
     * @param o2
     * @param ignorePaths example: Sets.newHashSet("/exec/taskSerializeNum","/exec/jobInfo[]/taskSerializeNum")
     * @return
     */
    public static boolean objEquals(com.alibaba.fastjson.JSONObject o1, com.alibaba.fastjson.JSONObject o2, Set<String> ignorePaths) {
        StringBuffer vistPath = new StringBuffer();
        return objEquals(o1, o2, Collections.unmodifiableSet(ignorePaths), vistPath);
    }

    private static boolean objEquals(com.alibaba.fastjson.JSONObject o1, com.alibaba.fastjson.JSONObject o2, Set<String> ignorePaths, StringBuffer vistPath) {
        for (Map.Entry<String, Object> entry1 : o1.entrySet()) {
            Object prop2 = o2.get(entry1.getKey());
            if (!compareEqual(entry1.getValue(), prop2, ignorePaths, (new StringBuffer(vistPath)).append("/").append(entry1.getKey()))) {
                return false;
            }
        }
        return true;
    }

    private static boolean compareEqual(Object prop1, Object prop2, Set<String> ignorePaths, StringBuffer vistPath) {
        if (ignorePaths.contains(vistPath.toString())) {
            return true;
        }
        if (prop2 instanceof com.alibaba.fastjson.JSONObject
                && prop1 instanceof com.alibaba.fastjson.JSONObject) {
            if (!objEquals((com.alibaba.fastjson.JSONObject) prop1, (com.alibaba.fastjson.JSONObject) prop2, ignorePaths, vistPath)) {
                return false;
            }
        } else if (prop2 instanceof JSONArray && prop1 instanceof JSONArray) {
            JSONArray a1 = (JSONArray) prop1;
            JSONArray a2 = (JSONArray) prop2;
            if (a1.size() != a2.size()) {
                return false;
            }
            for (int i = 0; i < a1.size(); i++) {
                if (!compareEqual(a1.get(i), a2.get(i), ignorePaths, (new StringBuffer(vistPath)).append("[]"))) {
                    return false;
                }
            }
        } else {
            if (!prop2.equals(prop1)) {
                return false;
            }
        }
        return true;
    }


    public static final class UnCacheString<T> {
        private final Callable<T> valGetter;

        public UnCacheString(Callable<T> valGetter) {
            this.valGetter = valGetter;
        }

        public T getValue() {
            try {
                return this.valGetter.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static String toString(Object json, boolean prettyFormat) {

        List<SerializerFeature> features = Lists.newArrayList(SerializerFeature.DisableCircularReferenceDetect);
        if (prettyFormat) {
            features.add(SerializerFeature.PrettyFormat);
        }

        return com.alibaba.fastjson.JSON.toJSONString(
                json, features.toArray(new SerializerFeature[features.size()]));
    }

    public static String toString(Object json) {
        return toString(json, TisAppLaunch.isTestMock());
    }

    public static void assertJSONEqual(Class<?> invokeClass, String assertFileName, DescriptorsJSONResult actual, IAssert azzert) {
        assertJSONEqual(invokeClass, assertFileName, actual.toJSONString(), azzert);
    }

    public static void assertJSONEqual(Class<?> invokeClass, String assertFileName, String actual, IAssert azzert) {

//        String expectJson = com.alibaba.fastjson.JSON.toJSONString(
//                JSON.parseObject(IOUtils.loadResourceFromClasspath(invokeClass, assertFileName))
//                , SerializerFeature.DisableCircularReferenceDetect, SerializerFeature.PrettyFormat, SerializerFeature.MapSortField);
//        System.out.println(assertFileName + "\n" + expectJson);
//        String actualJson = com.alibaba.fastjson.JSON.toJSONString(JSON.parseObject(actual)
//                , SerializerFeature.DisableCircularReferenceDetect, SerializerFeature.PrettyFormat, SerializerFeature.MapSortField);
//        azzert.assertEquals("assertFile:" + assertFileName, expectJson, actualJson);


        assertJSONEqual(invokeClass, assertFileName, JSON.parseObject(actual), azzert);
    }

    public static void assertJSONEqual(Class<?> invokeClass, String assertFileName, com.alibaba.fastjson.JSONObject actual, IAssert azzert) {
        String expectJson = com.alibaba.fastjson.JSON.toJSONString(
                JSON.parseObject(IOUtils.loadResourceFromClasspath(invokeClass, assertFileName))
                , SerializerFeature.DisableCircularReferenceDetect, SerializerFeature.PrettyFormat, SerializerFeature.MapSortField);
        System.out.println(assertFileName + "\n" + expectJson);
        String actualJson = com.alibaba.fastjson.JSON.toJSONString(actual
                , SerializerFeature.DisableCircularReferenceDetect, SerializerFeature.PrettyFormat, SerializerFeature.MapSortField);
        azzert.assertEquals("assertFile:" + assertFileName, expectJson, actualJson);
    }

    public interface IAssert {
        public void assertEquals(String message, String expected, String actual);
    }


}
