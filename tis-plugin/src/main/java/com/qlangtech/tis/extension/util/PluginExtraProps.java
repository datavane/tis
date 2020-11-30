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
package com.qlangtech.tis.extension.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.qlangtech.tis.manage.common.TisUTF8;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

/**
 * load extra prop desc like 'lable' and so on
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class PluginExtraProps extends HashMap<String, JSONObject> {

    public static PluginExtraProps load(Class<?> pluginClazz) throws IOException {
        try (InputStream i = pluginClazz.getResourceAsStream(pluginClazz.getSimpleName() + ".json")) {
            if (i == null) {
                return null;
            }
            PluginExtraProps props = JSON.parseObject(i, TisUTF8.get(), PluginExtraProps.class);
            return props;
        }
    }

    public PluginExtraProps() {
    }

    public Prop getProp(String key) {
        JSONObject props = this.get(key);
        if (props == null) {
            return null;
        } else {
            return new Prop(props);
        }

    }

    public static class Prop {
        private final JSONObject props;

        public Prop(JSONObject props) {
            this.props = props;
        }

        @JSONField(serialize = false)
        public String getLable() {
            return (String) props.get("label");
        }

        @JSONField(serialize = false)
        public String getHelpUrl() {
            return (String) props.get("helpUrl");
        }

        @JSONField(serialize = false)
        public String getPlaceholder() {
            return (String) props.get("placeholder");
        }

        public JSONObject getProps() {
            return this.props;
        }
    }
}
