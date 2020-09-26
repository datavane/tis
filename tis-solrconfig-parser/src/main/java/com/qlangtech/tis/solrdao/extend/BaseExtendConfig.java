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
package com.qlangtech.tis.solrdao.extend;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 基础配置
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2018年10月12日
 */
public class BaseExtendConfig {

    public static final Pattern PATTERN_PARMS = Pattern.compile("(\\w+?)=([^\\s]+)");

    protected final Map<String, String> params = new HashMap<String, String>();

    public BaseExtendConfig(String args) {
        Matcher matcher = PATTERN_PARMS.matcher(args);
        while (matcher.find()) {
            params.put(matcher.group(1), matcher.group(2));
        }
    }

    public final Map<String, String> getParams() {
        return this.params;
    }

    public final void putParam(String key, String value) {
        params.put(key, value);
    }

    public String getParam(String key) {
        return params.get(key);
    }
}
