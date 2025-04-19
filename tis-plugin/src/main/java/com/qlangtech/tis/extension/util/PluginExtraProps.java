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
package com.qlangtech.tis.extension.util;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.qlangtech.tis.extension.ElementPluginDesc;
import com.qlangtech.tis.extension.IPropertyType;
import com.qlangtech.tis.extension.impl.IOUtils;
import com.qlangtech.tis.extension.impl.PropertyType;
import com.qlangtech.tis.manage.common.TisUTF8;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.Validator;
import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;
import com.qlangtech.tis.util.DescriptorsJSON;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.qlangtech.tis.util.HeteroEnum.DATASOURCE;
import static com.qlangtech.tis.util.HeteroEnum.PARAMS_CONFIG;

/**
 * load extra prop desc like 'lable' and so on
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class PluginExtraProps extends HashMap<String, PluginExtraProps.Props> {

    private static final Logger logger = LoggerFactory.getLogger(PluginExtraProps.class);

    public static final String KEY_DFTVAL_PROP = "dftVal";
    public static final String KEY_PLACEHOLDER_PROP = "placeholder";
    /**
     * for: FormFieldType.DateTime
     */
    public static final String KEY_DATETIME_FORMAT = "dateTimeFormat";
    public static final String KEY_DISABLE = "disable";
    public static final String KEY_CREATOR = "creator";
    public static final String KEY_CREATOR_HETERO = "hetero";
    /**
     * <pre>
     *  export enum RouterAssistType {
     *   hyperlink = 'hyperlink',
     *   dbQuickManager = 'dbQuickManager',
     *   paramCfg = 'paramCfg'
     * }
     * </pre>
     */
    public static final String KEY_CREATOR_ASSIST_TYPE = "assistType";

    public enum RouterAssistType {
        hyperlink("hyperlink"),
        dbQuickManager("dbQuickManager"),
        paramCfg("paramCfg");
        private final String token;

        public static RouterAssistType parse(String token) {
            if (StringUtils.isEmpty(token)) {
                throw new IllegalArgumentException("param '" + KEY_CREATOR_ASSIST_TYPE + "' can not be empty");
            }
            for (RouterAssistType assistType : RouterAssistType.values()) {
                if (assistType.token.equals(token)) {
                    return assistType;
                }
            }
            throw new IllegalStateException("illegal routerAssistType:" + token);
        }

        private RouterAssistType(String token) {
            this.token = token;
        }
    }


    public static final String KEY_ROUTER_LINK = "routerLink";
    public static final String KEY_LABEL = "label";
    // 枚举过滤器,只对底层Describle类型的enum起效
    public static final String KEY_ENUM_FILTER = "subDescEnumFilter";

    private static Optional<PluginExtraProps> parseExtraProps(Class<?> pluginClazz) {
        return parseExtraProps(pluginClazz, Optional.empty());
    }

    public static Optional<PluginExtraProps> parseExtraProps(Class<?> pluginClazz, Optional<Field> subFormField) {
        String subformFieldName = StringUtils.EMPTY;
        if (subFormField.isPresent()) {
            subformFieldName = "." + subFormField.get().getName();
        }

        final String mdRes = pluginClazz.getSimpleName() + subformFieldName + ".md";
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
                    String fieldKey = StringUtils.trimToNull(StringUtils.substring(line, indexOf + 2));
                    if (propHelps.put(fieldKey, propHelp) != null) {
                        throw new IllegalStateException("field:" + fieldKey + " relevant propHelp can not be add " +
                                "twice");
                    }
                } else {
                    Objects.requireNonNull(propHelp, "propHelp can not be null,file:" + mdRes);
                    propHelp.append(line).append("\n");
                }
            }
            return null;
        });

        final String resourceName = pluginClazz.getSimpleName() + subformFieldName + ".json";
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

    public static Optional<PluginExtraProps> load(Class<?> clazz) {
        return load(Optional.empty(), clazz);
    }

    /**
     * field form extran descriptor
     *
     * @param
     * @return
     * @throws IOException
     */
    public static Optional<PluginExtraProps> load(Optional<ElementPluginDesc> desc, Class<?> clazz) {


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
        PluginExtraProps e = null;
        if (desc.isPresent() && MapUtils.isNotEmpty(e = desc.get().getFieldExtraDescs())) {
            if (ep != null) {
                ep.mergeProps(e, desc);
            } else {
                ep = e;
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

    private static JSONObject validate(JSONObject props, String propKey, Class<?> pluginClazz, String resourceName,
                                       boolean finalValidate) {
        String errDesc = createErrorMsg(propKey, pluginClazz, resourceName);
        Object creator = props.get(KEY_CREATOR);
        if (creator != null) {
            if (!(creator instanceof JSONObject)) {
                throw new IllegalStateException("prop creator must be type of JSONObject:" + errDesc);
            }
            if (finalValidate) {
                JSONObject creatorJ = (JSONObject) creator;

                //  Objects.requireNonNull(creatorJ.get(KEY_ROUTER_LINK), errDesc);
                Objects.requireNonNull(creatorJ.get(KEY_LABEL), errDesc);
                JSONObject pmeta = null;
                JSONArray plugins = creatorJ.getJSONArray("plugin");
                boolean assistTypeEmpty = StringUtils.isEmpty(creatorJ.getString(KEY_CREATOR_ASSIST_TYPE));
                if (plugins != null) {
                    for (int i = 0; i < plugins.size(); i++) {
                        pmeta = plugins.getJSONObject(i);
                        if (StringUtils.isBlank(pmeta.getString(KEY_CREATOR_HETERO))
                                || StringUtils.isBlank(pmeta.getString("descName"))
                            // 由于插件中参数不一定是必须的，所以先把以下校验去掉： "extraParam": "append_true"
                            //        || StringUtils.isBlank(pmeta.getString("extraParam"))
                        ) {
                            throw new IllegalStateException("pmeta is illegal:" + pmeta.toJSONString() + "," +
                                    "pluginClazz:" + pluginClazz.getName() + ",errDesc:" + errDesc);
                        }
                        /**
                         * 如果assitType 为空，则查看plugin 的KEY_CREATOR_HETERO 如果为 ‘params-cfg’ 则默认ASSIST_TYPE类型为 RouterAssistType.paramCfg
                         */
                        if (assistTypeEmpty) {
                            String hetero = pmeta.getString(KEY_CREATOR_HETERO);
                            if (StringUtils.isNotEmpty(hetero)) {
                                if (!DATASOURCE.identity.equals(hetero)) {
                                    creatorJ.put(KEY_CREATOR_ASSIST_TYPE, RouterAssistType.paramCfg.token);
                                    assistTypeEmpty = false;
                                }
                            }
                        }
                    }
                }

                if (assistTypeEmpty && StringUtils.isNotEmpty(creatorJ.getString(KEY_ROUTER_LINK))) {
                    creatorJ.put(KEY_CREATOR_ASSIST_TYPE, RouterAssistType.hyperlink.token);
                }

                /**
                 * 校验assistType
                 */
                try {
                    RouterAssistType.parse(creatorJ.getString(KEY_CREATOR_ASSIST_TYPE));
                } catch (Exception e) {
                    throw new RuntimeException(errDesc, e);
                }
            }
        }
        return props;
    }

    private static String createErrorMsg(String propKey, Class<?> pluginClazz, String resourceName) {
        return String.format("propKey:%s,package:%s,propKey:%s", propKey, pluginClazz.getPackage().getName(),
                resourceName);
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
        this.mergeProps(props, Optional.empty());
    }

    public void mergeProps(PluginExtraProps props, Optional<ElementPluginDesc> desc) {
        if (props == null) {
            throw new IllegalArgumentException("param props can not be null");
        }
        Props p = null;

        AtomicReference<Map<String, IPropertyType>> ppRef = new AtomicReference<>();
        for (Map.Entry<String, PluginExtraProps.Props> entry : props.entrySet()) {
            p = this.get(entry.getKey());
            if (p != null) {
                p.merge(entry.getValue());
            } else {
                if (desc.isPresent()) {
                    ElementPluginDesc elmtDesc = desc.get();


                    Map<String, IPropertyType> pp = ppRef.updateAndGet((pre) -> {
                        return (pre == null) ? PropertyType.buildPropertyTypes(Optional.empty(), elmtDesc.getElementDesc().clazz) : pre;
                    });
                    if (!pp.containsKey(entry.getKey())) {
                        throw new IllegalStateException("prop key:" + entry.getKey()
                                + " relevant prop must exist , " + "exist props keys:" + pp.keySet().stream().collect(Collectors.joining(",")));
                    }
                }
                this.put(entry.getKey(), entry.getValue());
            }
        }
    }

    public interface IPostSelectedItemsGetter {
        List<FormFieldType.SelectedItem> apply(PropertyType attrDesc, IControlMsgHandler msgHandler, Context context,
                                               JSONObject eprops);
    }


    public static class Props {
        public static final String KEY_HELP = "help";
        public static final String KEY_VALIDATOR = "validators";
        public static final String KEY_VIEW_TYPE = "viewtype";
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

        public List<ValidatorCfg> getExtraValidators() {
            Object v = props.get(KEY_VALIDATOR);
            if (v == null) {
                return Collections.emptyList();
            }
            if (v instanceof String) {
                return Collections.singletonList(ValidatorCfg.parse((String) v));
            }
            if (v instanceof JSONArray) {
                List<ValidatorCfg> result = Lists.newArrayList();
                for (Object validator : (JSONArray) v) {
                    result.add(ValidatorCfg.parse(String.valueOf(validator)));
                }
                return result;
            }
            throw new IllegalStateException("validate:" + v);
        }

        public static class ValidatorCfg {
            public final Validator validator;
            public final boolean disable;

            private static ValidatorCfg parse(String token) {
                String[] split = StringUtils.split(token, ":");
                if (split.length == 1) {
                    return new ValidatorCfg(Validator.parse(String.valueOf(split[0])));
                } else if (split.length == 2) {
                    return new ValidatorCfg(Validator.parse(String.valueOf(split[0])), KEY_DISABLE.equalsIgnoreCase(split[1]));
                }

                throw new IllegalStateException("in validate token:" + token);
            }

            public ValidatorCfg(Validator validator) {
                this(validator, false);
            }

            public ValidatorCfg(Validator validator, boolean disable) {
                this.validator = validator;
                this.disable = disable;
            }
        }

        public boolean isAdvance() {
            return props.getBooleanValue(DescriptorsJSON.KEY_ADVANCE);
        }

        private SimpleDateFormat dateFormat;

        @JSONField(serialize = false)
        public SimpleDateFormat getDateTimeFormat() {
            if (dateFormat == null) {
                Object p = props.get(KEY_DATETIME_FORMAT);
                if (p != null) {
                    dateFormat = new SimpleDateFormat(String.valueOf(p));
                } else {
                    throw new IllegalStateException("key:" + KEY_DATETIME_FORMAT + " can not be null");
                }
            }
            return dateFormat;
        }

        @JSONField(serialize = false)
        public String getPlaceholder() {
            Object p = props.get(KEY_PLACEHOLDER_PROP);
            if (p != null) {
                return String.valueOf(p);
            }
            return null;
            // return (String) props.get(KEY_PLACEHOLDER_PROP);
        }

        @JSONField(serialize = false)
        public Object getDftVal() {
            Object o = props.get(KEY_DFTVAL_PROP);
            return o == null ? null : o;
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
