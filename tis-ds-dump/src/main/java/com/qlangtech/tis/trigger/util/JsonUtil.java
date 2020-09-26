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
package com.qlangtech.tis.trigger.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2012-11-14
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
