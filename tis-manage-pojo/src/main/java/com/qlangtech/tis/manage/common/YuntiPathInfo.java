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
package com.qlangtech.tis.manage.common;

import java.util.HashMap;
import java.util.Iterator;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
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
