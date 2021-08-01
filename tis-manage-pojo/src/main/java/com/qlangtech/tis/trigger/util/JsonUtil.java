/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.trigger.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.*;
import com.qlangtech.tis.extension.impl.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
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
                    SerializeWriter out = serializer.out;

                    UnCacheString value = (UnCacheString) object;
                    Objects.requireNonNull(value, "callable of " + fieldName + " can not be null");

                    out.writeString(value.getValue());
                } catch (Exception e) {
                    throw new IOException(e);
                }
            }
        };

        SerializeConfig.globalInstance.put(UnCacheString.class, serializer);
    }

    public static final class UnCacheString {
        private final Callable<String> valGetter;

        public UnCacheString(Callable<String> valGetter) {
            this.valGetter = valGetter;
        }

        public String getValue() throws Exception {
            return this.valGetter.call();
        }
    }

    public static String toString(Object json) {


        return com.alibaba.fastjson.JSON.toJSONString(
                json, SerializerFeature.DisableCircularReferenceDetect, SerializerFeature.PrettyFormat);
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
