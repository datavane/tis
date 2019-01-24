/* 
 * The MIT License
 *
 * Copyright (c) 2018-2022, qinglangtech Ltd
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.qlangtech.tis.trigger.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
class JsonUtil {

    private JsonUtil() {
    }

    /**
     * 将map中的内容序反列化成一个map结果
     *
     * @param value
     * @return
     */
    public static TriggerParam deserialize(String value) {
        return deserialize(value, new TriggerParam());
    }

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
        return json.toString();
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        TriggerParam param = new TriggerParam();
        param.put("name", "bai\"sui");
        param.put("age", "12");
        // System.out.println(JsonUtil.serialize(param));
        param = deserialize(JsonUtil.serialize(param));
        System.out.println(param.get("name"));
        System.out.println(param.get("age"));
    }
}
