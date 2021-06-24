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
import org.apache.commons.lang.ClassUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * load extra prop desc like 'lable' and so on
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class PluginExtraProps extends HashMap<String, PluginExtraProps.Props> {
    public static final String KEY_DFTVAL_PROP = "dftVal";
    public static final String KEY_DISABLE = "disable";


    private static Optional<PluginExtraProps> parseExtraProps(Class<?> pluginClazz) {
        String resourceName = pluginClazz.getSimpleName() + ".json";
        try {
            try (InputStream i = pluginClazz.getResourceAsStream(resourceName)) {
                if (i == null) {
                    return Optional.empty();
                }
                JSONObject o = JSON.parseObject(i, TisUTF8.get(), JSONObject.class);
                PluginExtraProps props = new PluginExtraProps();
                for (String propKey : o.keySet()) {
                    props.put(propKey, new Props(validate(o.getJSONObject(propKey), propKey, pluginClazz, resourceName)));
                }
                return Optional.of(props);
            }
        } catch (Exception e) {
            throw new RuntimeException("resourceName:" + resourceName, e);
        }
    }

    /**
     * field form extran descriptor
     *
     * @param
     * @return
     * @throws IOException
     */
    public static Optional<PluginExtraProps> load(Class<?> clazz) {
        List allSuperclasses = ClassUtils.getAllSuperclasses(clazz);
        allSuperclasses.add(clazz);
        PluginExtraProps extraProps = null;
        Class targetClass = null;
        Optional<PluginExtraProps> nxtExtraProps;
        for (Object c : allSuperclasses) {
            targetClass = (Class) c;
            nxtExtraProps = parseExtraProps(targetClass);
            if (nxtExtraProps.isPresent()) {
                if (extraProps == null) {
                    extraProps = nxtExtraProps.get();
                } else {
                    extraProps.mergeProps(nxtExtraProps.get());
                }
            }
        }
        return Optional.ofNullable(extraProps);
    }

    private static JSONObject validate(JSONObject props, String propKey, Class<?> pluginClazz, String resourceName) {
        String errDesc = createErrorMsg(propKey, pluginClazz, resourceName);
        Object creator = props.get("creator");
        if (creator != null) {
            if (!(creator instanceof JSONObject)) {
                throw new IllegalStateException("prop creator must be type of JSONObject:" + errDesc);
            }
//                "creator": {
//                    "routerLink": "/base/departmentlist",
//                            "label": "部门管理"
//                }
            JSONObject creatorJ = (JSONObject) creator;
            Objects.requireNonNull(creatorJ.get("routerLink"), errDesc);
            Objects.requireNonNull(creatorJ.get("label"), errDesc);
        }
        return props;
    }

    private static String createErrorMsg(String propKey, Class<?> pluginClazz, String resourceName) {
        return String.format("propKey:%s,package:%s,propKey:%s", propKey, pluginClazz.getPackage().getName(), resourceName);
    }


    public PluginExtraProps() {
    }


    public Props getProp(String key) {
        Props props = this.get(key);
        if (props == null) {
            return null;
        } else {
            return props;
        }

    }

    public void mergeProps(PluginExtraProps props) {
        if (props == null) {
            throw new IllegalArgumentException("param props can not be null");
        }
        Props p = null;
        for (Map.Entry<String, PluginExtraProps.Props> entry : props.entrySet()) {
            p = this.get(entry.getKey());
            if (p != null) {
                p.merge(entry.getValue());
            } else {
                this.put(entry.getKey(), entry.getValue());
            }
        }
    }

    public static class Props {
        private final JSONObject props;

        public Props(JSONObject props) {
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
        public String getHelpContent() {
            return (String) props.get("help");
        }


        @JSONField(serialize = false)
        public String getPlaceholder() {
            return (String) props.get("placeholder");
        }

        @JSONField(serialize = false)
        public String getDftVal() {
            Object o = props.get(KEY_DFTVAL_PROP);
            return o == null ? null : String.valueOf(o);
        }

        public boolean getBoolean(String key) {
            return this.props.getBooleanValue(key);
        }

        public JSONObject getProps() {
            return this.props;
        }

        public void merge(Props p) {
            p.props.forEach((key, val) -> {
                props.put(key, val);
            });
        }
    }
}
