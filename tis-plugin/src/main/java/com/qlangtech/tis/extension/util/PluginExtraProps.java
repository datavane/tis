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
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.qlangtech.tis.extension.impl.IOUtils;
import com.qlangtech.tis.manage.common.TisUTF8;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * load extra prop desc like 'lable' and so on
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class PluginExtraProps extends HashMap<String, PluginExtraProps.Props> {
    public static final String KEY_DFTVAL_PROP = "dftVal";
    public static final String KEY_DISABLE = "disable";
    public static final String KEY_CREATOR = "creator";

    public static final String KEY_ROUTER_LINK = "routerLink";
    public static final String KEY_LABEL = "label";
    // private static final Parser mdParser = Parser.builder().build();

    private static Optional<PluginExtraProps> parseExtraProps(Class<?> pluginClazz) {

        final String mdRes = pluginClazz.getSimpleName() + ".md";
        final Map<String, StringBuffer> propHelps = Maps.newHashMap();
        IOUtils.loadResourceFromClasspath(pluginClazz, mdRes, false, (input) -> {
            LineIterator lines = org.apache.commons.io.IOUtils.lineIterator(input, TisUTF8.get());
            String line = null;
            StringBuffer propHelp = null;
            int indexOf;
            while (lines.hasNext()) {
                line = lines.nextLine();

                if ((indexOf = StringUtils.indexOf(line, "##")) > -1) {
                    propHelp = new StringBuffer();
                    propHelps.put(StringUtils.trimToNull(StringUtils.substring(line, indexOf + 2)), propHelp);
                } else {
                    Objects.requireNonNull(propHelp, "propHelp can not be null");
                    propHelp.append(line).append("\n");
                }
            }
            return null;
        });

        String resourceName = pluginClazz.getSimpleName() + ".json";
        try {
            try (InputStream i = pluginClazz.getResourceAsStream(resourceName)) {
                if (i == null) {
                    return Optional.empty();
                }
                JSONObject o = JSON.parseObject(i, TisUTF8.get(), JSONObject.class);
                PluginExtraProps props = new PluginExtraProps();
                Props pps = null;
                for (String propKey : o.keySet()) {
                    pps = new Props(validate(o.getJSONObject(propKey), propKey, pluginClazz, resourceName, false));
                    StringBuffer asynHelp = null;
                    if ((asynHelp = propHelps.get(propKey)) != null) {
                        pps.tagAsynHelp(asynHelp);
                    }
                    props.put(propKey, pps);
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

        PluginExtraProps ep = visitAncestorsClass(clazz, (c, extraProps) -> {
            Optional<PluginExtraProps> nxtExtraProps = parseExtraProps(c);
            if (nxtExtraProps.isPresent()) {
                if (extraProps == null) {
                    extraProps = nxtExtraProps.get();
                } else {
                    extraProps.mergeProps(nxtExtraProps.get());
                }
            }
            return extraProps;
        });

        if (ep != null) {
            String resourceName = clazz.getSimpleName() + ".json";
            for (Map.Entry<String, PluginExtraProps.Props> entry : ep.entrySet()) {
                validate(entry.getValue().props, entry.getKey(), clazz, resourceName, true);
            }
        }

        return Optional.ofNullable(ep);
    }

    public static <T> T visitAncestorsClass(Class<?> clazz, IClassVisitor<T> clazzVisitor) {
        List allSuperclasses = Lists.newArrayList(clazz);
        allSuperclasses.addAll(ClassUtils.getAllSuperclasses(clazz));
        T extraProps = null;
        Class targetClass = null;
        for (int i = allSuperclasses.size() - 2; i >= 0; i--) {
            targetClass = (Class) allSuperclasses.get(i);
            extraProps = clazzVisitor.process(targetClass, extraProps);
        }
        return extraProps;
    }

    public interface IClassVisitor<T> {
        T process(Class<?> clazz, T extraProps);
    }

    private static JSONObject validate(JSONObject props, String propKey, Class<?> pluginClazz, String resourceName, boolean finalValidate) {
        String errDesc = createErrorMsg(propKey, pluginClazz, resourceName);
        Object creator = props.get(KEY_CREATOR);
        if (creator != null) {
            if (!(creator instanceof JSONObject)) {
                throw new IllegalStateException("prop creator must be type of JSONObject:" + errDesc);
            }
            if (finalValidate) {
                JSONObject creatorJ = (JSONObject) creator;
               // Objects.requireNonNull(creatorJ.get(KEY_ROUTER_LINK), errDesc);
                Objects.requireNonNull(creatorJ.get(KEY_LABEL), errDesc);
                JSONObject pmeta = null;
                JSONArray plugins = creatorJ.getJSONArray("plugin");
                if (plugins != null) {
                    for (int i = 0; i < plugins.size(); i++) {
                        pmeta = plugins.getJSONObject(i);
                        if (StringUtils.isBlank(pmeta.getString("hetero"))
                                || StringUtils.isBlank(pmeta.getString("descName"))
                                || StringUtils.isBlank(pmeta.getString("extraParam"))
                        ) {
                            throw new IllegalStateException("pmeta is illegal:" + pmeta.toJSONString() + ",pluginClazz:" + pluginClazz.getName());
                        }
                    }
                }
            }
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
        public static final String KEY_HELP = "help";
        private static final String KEY_ASYNC_HELP = "asyncHelp";
        private final JSONObject props;
        private String asynHelp;

        public Props(JSONObject props) {
            this.props = props;
        }

        @JSONField(serialize = false)
        public String getAsynHelp() {
            return this.asynHelp;
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
            return (String) props.get(KEY_HELP);
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

        /**
         * 标记帮助内容从服务端异步获取
         */
        public void tagAsynHelp(StringBuffer asynHelp) {
            props.put(KEY_ASYNC_HELP, true);
            props.remove(KEY_HELP);
            this.asynHelp = asynHelp.toString();
        }

        public boolean isAsynHelp() {
            return props.getBooleanValue(KEY_ASYNC_HELP);
        }

        public boolean getBoolean(String key) {
            return this.props.getBooleanValue(key);
        }

        public JSONObject getProps() {
            return this.props;
        }

        public void merge(Props p) {
            jsonMerge(props, p.props);
            if (p.isAsynHelp()) {
                this.asynHelp = p.asynHelp;
            }
        }

        private static final Pattern PATTERN_PARENT = Pattern.compile("(.+?)\\-parent");

        private void jsonMerge(JSONObject to, JSONObject from) {

            final Map<String, JSONObject> parentMap = Maps.newHashMap();
            Set<String> removeKeys = Sets.newHashSet();
            to.forEach((key, val) -> {
                Matcher matcher = PATTERN_PARENT.matcher(key);
                if (!matcher.matches()) {
                    return;
                }
                removeKeys.add(key);
                String parentKey = matcher.group(1);
                if (!(val instanceof JSONObject)) {
                    throw new IllegalStateException("key:" + key + " relevant val must be 'JSONObject'");
                }
                parentMap.put(parentKey, (JSONObject) val);
            });
            removeKeys.forEach((removeKey) -> {
                to.remove(removeKey);
            });

            from.forEach((key, val) -> {


                if (val instanceof JSONObject) {
                    Object toProp = to.get(key);
                    if (toProp != null && toProp instanceof JSONObject) {
                        jsonMerge((JSONObject) toProp, (JSONObject) val);
                    } else {
                        to.put(key, val);
                    }
                } else if (val instanceof JSONArray) {
                    JSONArray arys = (JSONArray) val;
                    JSONObject pval = parentMap.get(key);
                    if (pval != null) {
                        JSONArray narys = new JSONArray();
                        JSONObject o = null;
                        for (int i = 0; i < arys.size(); i++) {
                            o = (JSONObject) pval.clone();
                            jsonMerge(o, arys.getJSONObject(i));
                            narys.add(o);
                        }
                        arys = narys;
                    }
                    to.put(key, arys);
                } else {
                    to.put(key, val);
                }
            });
        }
    }
}
