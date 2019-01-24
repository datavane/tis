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
package com.qlangtech.tis.manage.common;

import java.util.HashMap;
import java.util.Iterator;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class YuntiPathInfo extends HashMap<String, String> {

    private static final long serialVersionUID = 1L;

    public static final String YUNTI_PATH = "yuntiPath";

    public static final String YUNTI_TOKEN = "yuntiToken";

    @SuppressWarnings("all")
    public YuntiPathInfo(String jsonText) {
        try {
            JSONTokener tokener = new JSONTokener(jsonText);
            JSONObject json = new JSONObject(tokener);
            Iterator it = json.keys();
            String key = null;
            while (it.hasNext()) {
                key = (String) it.next();
                this.put(key, json.getString(key));
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static String createYuntiPathInfo(String yuntiPath, String yuntiToken) {
        try {
            JSONObject json = new JSONObject();
            json.put(YUNTI_PATH, yuntiPath);
            json.put(YUNTI_TOKEN, yuntiToken);
            return json.toString();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public String getPath() {
        return this.get(YUNTI_PATH);
    }

    public String getUserToken() {
        return StringUtils.trimToEmpty(this.get(YUNTI_TOKEN));
    }
}
