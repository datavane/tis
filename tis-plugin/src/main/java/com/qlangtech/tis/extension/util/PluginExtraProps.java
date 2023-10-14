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
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.IPropertyType;
import com.qlangtech.tis.extension.impl.IOUtils;
import com.qlangtech.tis.extension.impl.PropertyType;
import com.qlangtech.tis.manage.common.TisUTF8;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.Validator;
import com.qlangtech.tis.plugin.ds.CMeta;
import com.qlangtech.tis.plugin.ds.DataType;
import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;
import com.qlangtech.tis.runtime.module.misc.IFieldErrorHandler;
import com.qlangtech.tis.util.DescriptorsJSON;
import org.apache.commons.beanutils.BeanUtilsBean2;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
    public static final String KEY_DISABLE = "disable";
    public static final String KEY_CREATOR = "creator";

    public static final String KEY_ROUTER_LINK = "routerLink";
    public static final String KEY_LABEL = "label";
    // 枚举过滤器,只对底层Describle类型的enum起效
    public static final String KEY_ENUM_FILTER = "subDescEnumFilter";
    // private static final Parser mdParser = Parser.builder().build();

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
    public static Optional<PluginExtraProps> load(Optional<Descriptor.ElementPluginDesc> desc, Class<?> clazz) {


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

    public static ParsePostMCols parsePostMCols(Optional<CMeta.ElementCreatorFactory> elementCreator,
                                                IFieldErrorHandler msgHandler, Context context, String keyColsMeta,
                                                JSONArray targetCols) {
        if (targetCols == null) {
            throw new IllegalArgumentException("param targetCols can not be null");
        }
        ParsePostMCols postMCols = new ParsePostMCols();
        CMeta colMeta = null;

        JSONObject targetCol = null;
        int index;
        String targetColName = null;
        DataType dataType = null;

        //    if (targetCols.size() < 1) {
        //      msgHandler.addFieldError(context, fieldKey, "Writer目标表列不能为空");
        //      return false;
        //    }
        Map<String, Integer> existCols = Maps.newHashMap();
        //   boolean validateFaild = false;
        Integer previousColIndex = null;
        boolean pk;
        // boolean pkHasSelected = false;
        JSONObject type = null;
        for (int i = 0; i < targetCols.size(); i++) {
            targetCol = targetCols.getJSONObject(i);
            index = targetCol.getInteger("index");
            pk = targetCol.getBooleanValue("pk");
            targetColName = targetCol.getString("name");
            if (StringUtils.isNotBlank(targetColName) && (previousColIndex = existCols.put(targetColName, index)) != null) {
                msgHandler.addFieldError(context, keyColsMeta + "[" + previousColIndex + "]", "内容不能与第" + index + "行重复");
                msgHandler.addFieldError(context, keyColsMeta + "[" + index + "]", "内容不能与第" + previousColIndex + "行重复");
                // return false;
                postMCols.validateFaild = true;
                return postMCols;
            }
            if (!Validator.require.validate(msgHandler, context, keyColsMeta + "[" + index + "]", targetColName)) {
                postMCols.validateFaild = true;
            } else if (!Validator.db_col_name.validate(msgHandler, context, keyColsMeta + "[" + index + "]",
                    targetColName)) {
                postMCols.validateFaild = true;
            }


            colMeta = elementCreator.isPresent() ? elementCreator.get().create(targetCol) : new CMeta();
            colMeta.setDisable(targetCol.getBooleanValue("disable"));
            colMeta.setName(targetColName);
            colMeta.setPk(pk);
            if (pk) {
                postMCols.pkHasSelected = true;
            }
            //{"s":"3,12,2","typeDesc":"decimal(12,2)","columnSize":12,"typeName":"VARCHAR","unsigned":false,
            // "decimalDigits":4,"type":3,"unsignedToken":""}
            dataType = DataType.parseType(targetCol);

            //            dataType = DataType.create(type.getInteger("type"), type.getString("typeName"), type
            //            .getInteger(
            //                    "columnSize"));
            //
            //            dataType.setDecimalDigits(type.getInteger("decimalDigits"));
            // DataType dataType = targetCol.getObject("type", DataType.class);
            // colMeta.setType(ISelectedTab.DataXReaderColType.parse(targetCol.getString("type")));
            colMeta.setType(dataType);
            postMCols.writerCols.add(colMeta);
        }

        return postMCols;
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
                if (plugins != null) {
                    for (int i = 0; i < plugins.size(); i++) {
                        pmeta = plugins.getJSONObject(i);
                        if (StringUtils.isBlank(pmeta.getString("hetero")) || StringUtils.isBlank(pmeta.getString(
                                "descName"))
                            // 由于插件中参数不一定是必须的，所以先把以下校验去掉： "extraParam": "append_true"
                            //        || StringUtils.isBlank(pmeta.getString("extraParam"))
                        ) {
                            throw new IllegalStateException("pmeta is illegal:" + pmeta.toJSONString() + "," +
                                    "pluginClazz:" + pluginClazz.getName());
                        }
                    }
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

    public void mergeProps(PluginExtraProps props, Optional<Descriptor.ElementPluginDesc> desc) {
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
                    Descriptor.ElementPluginDesc elmtDesc = desc.get();


                    Map<String, IPropertyType> pp = ppRef.updateAndGet((pre) -> {
                        return (pre == null) ? Descriptor.buildPropertyTypes(Optional.empty(), elmtDesc.getElementDesc().clazz) : pre;
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

    public static class MultiItemsViewType {
        public final ViewType viewType;
        public final Optional<CMeta.ElementCreatorFactory> tupleFactory;
        //  private final Class<? extends Describable> hostPluginClazz;
        private List<String> elementPropertyKeys;


        public MultiItemsViewType(ViewType viewType, Optional<CMeta.ElementCreatorFactory> tupleFactory) {
            this.viewType = viewType;
            this.tupleFactory = Objects.requireNonNull(tupleFactory, "tupleFactory can not be null");
            // this.hostPluginClazz = hostPluginClazz;
        }

        public List<FormFieldType.SelectedItem> getPostSelectedItems(PropertyType attrDesc,
                                                                     IControlMsgHandler msgHandler, Context context,
                                                                     JSONObject eprops) {
            return this.viewType.getPostSelectedItems(attrDesc, msgHandler, context, eprops);
        }

        public List<String> getElementPropertyKeys() {
            if (elementPropertyKeys == null) {
                elementPropertyKeys = tupleFactory.map((factory) -> {
                    try {
                        return Lists.newArrayList(BeanUtilsBean2.getInstance().describe(factory.createDefault()).keySet());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }).orElseGet(() -> new ArrayList<>());
            }
            return elementPropertyKeys;
        }

        public List<?> serialize2Frontend(Object bizInstance) {
            return viewType.serialize2Frontend(bizInstance);
        }

        @Override
        public String toString() {
            return "{" +
                    "viewType=" + viewType +
                    ", tupleFactory=" + tupleFactory.map((tf) -> tf.getClass().getSimpleName()).orElse("empty") +
                    '}';
        }
    }

    /**
     * Fieldtype 为 MULTI_SELECTABLE 类型显示的类型
     * <ol>
     *     <li>IdList：显示为Id列表</li>
     *     <li>TupleList：显示为table列表</li>
     * </ol>
     *
     * @see FormFieldType
     */
    public enum ViewType {
        IdList("idlist" //
                , (attrDesc, msgHandler, context, eprops) -> {
            final String keyChecked = "checked";
            JSONArray enums = eprops.getJSONArray(Descriptor.KEY_ENUM_PROP);
            if (enums == null) {
                enums = new JSONArray();
                //   throw new IllegalStateException("enums of prop can not be null");
            }
            JSONObject select = null;
            int selected = 0;
            List<FormFieldType.SelectedItem> selectedItems = Lists.newArrayList();
            FormFieldType.SelectedItem item = null;
            Object val = null;
            for (int i = 0; i < enums.size(); i++) {
                select = enums.getJSONObject(i);
                val = select.get("val");
                if (val.getClass() != String.class) {
                    throw new IllegalStateException("val:" + val + " must be type of String,but now is " + val.getClass());
                }
                item = new FormFieldType.SelectedItem(select.getString(PluginExtraProps.KEY_LABEL) //
                        , String.valueOf(val) //
                        , select.containsKey(keyChecked) && select.getBoolean(keyChecked));
                if (item.isChecked()) {
                    selected++;
                }
                selectedItems.add(item);
            }
            return selectedItems;
        } // start bizSerializeFrontend
                , (obj) -> {
            List<CMeta> mulitOpt = (List<CMeta>) obj;
            return mulitOpt.stream().map((c) -> c.getName()).collect(Collectors.toList());
        }), TupleList("tuplelist" //
                , (attrDesc, msgHandler, context, eprops) -> {

            Optional<CMeta.ElementCreatorFactory> elementCreator = attrDesc.getCMetaCreator();

            String keyColsMeta = "";
            JSONArray mcols = eprops.getJSONObject(Descriptor.KEY_ENUM_PROP).getJSONArray("_mcols");
            ParsePostMCols parsePostMCols = parsePostMCols(elementCreator, msgHandler, context, keyColsMeta, mcols);
            if (parsePostMCols.validateFaild) {
                return Collections.emptyList();
            }
            //List<FormFieldType.SelectedItem>
            return parsePostMCols.writerCols.stream() //
                    .map((cmeta) -> new FormFieldType.SelectedItem(cmeta)).collect(Collectors.toList());
        } // start bizSerializeFrontend
                , (obj) -> {
            return (List<CMeta>) obj;
        });

        private final String token;
        private final IPostSelectedItemsGetter postSelectedItemsGetter;
        /**
         * 将biz层 object 转化成前端可序列化实例
         */
        private final Function<Object, List<?>> bizSerializeFrontend;

        private ViewType(String token, IPostSelectedItemsGetter postSelectedItemsGetter,
                         Function<Object, List<?>> bizSerializeFrontend) {
            this.token = token;
            this.postSelectedItemsGetter = postSelectedItemsGetter;
            this.bizSerializeFrontend = bizSerializeFrontend;
        }

        public static ViewType parse(String token) {
            if (StringUtils.isEmpty(token)) {
                return ViewType.IdList;
            }
            for (ViewType t : ViewType.values()) {
                if (t.token.equalsIgnoreCase(token)) {
                    return t;
                }
            }
            throw new IllegalStateException("token value:" + token + " is invalid");
        }

        /**
         * 将biz层 object 转化成前端可序列化实例
         */
        public List<?> serialize2Frontend(Object bizInstance) {
            return bizSerializeFrontend.apply(bizInstance);
        }

        public List<FormFieldType.SelectedItem> getPostSelectedItems(PropertyType attrDesc,
                                                                     IControlMsgHandler msgHandler, Context context,
                                                                     JSONObject eprops) {
            return this.postSelectedItemsGetter.apply(attrDesc, msgHandler, context, eprops);
        }
    }

    public interface IPostSelectedItemsGetter {
        List<FormFieldType.SelectedItem> apply(PropertyType attrDesc, IControlMsgHandler msgHandler, Context context,
                                               JSONObject eprops);
    }


    public static class Props {
        public static final String KEY_HELP = "help";
        public static final String KEY_VIEW_TYPE = "viewtype";
        private static final String KEY_ASYNC_HELP = "asyncHelp";
        private final JSONObject props;
        private String asynHelp;

        /**
         * // Example: the descriptor for cols element of selectedTab
         * // The host plugin class for the cols element
         * //  hostPluginClazz 宿主plugin Class
         */
//        private final Map<Class<? extends Describable>, HostedExtraProps> hostedExtraProps = new HashMap<Class<? extends Describable>, HostedExtraProps>() {
//            @Override
//            public HostedExtraProps get(Object key) {
//                HostedExtraProps val = super.get(key);
//                if (val == null) {
//                    val = new HostedExtraProps(new Props(new JSONObject()));
//                    logger.warn("key:" + key + " relevant extraProp");
//                    this.put((Class<? extends Describable>) key, val);
//                }
//                return val;
//            }
//        };
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

        public boolean isAdvance() {
            return props.getBooleanValue(DescriptorsJSON.KEY_ADVANCE);
        }

        /**
         * Fieldtype 为 MULTI_SELECTABLE 类型显示的类型
         *
         * @return
         */
//        @JSONField(serialize = false)
//        public MultiItemsViewType multiItemsViewType(Class<? extends Describable> hostPluginClazz) {
//
//            final HostedExtraProps hostExtraProp = (this.hostedExtraProps.get(hostPluginClazz));
//            if (hostExtraProp == null) {
//                throw new IllegalStateException("hostPluginClazz:" + hostPluginClazz + " relevant plugin class can not be null,relevant hostProps keys:"
//                        + this.hostedExtraProps.keySet().stream().map((clazz) -> clazz.getName()).collect(Collectors.joining(",")));
//            }
//
//            if (hostExtraProp.multiItemsViewType == null) {
//                Optional<CMeta.ElementCreatorFactory> elementCreator = Optional.empty();
//                try {
//                    String selectElementCreator = hostExtraProp.getStrProp(CMeta.KEY_ELEMENT_CREATOR_FACTORY);
//                    if (StringUtils.isNotEmpty(selectElementCreator)) {
//                        elementCreator = Optional.of((CMeta.ElementCreatorFactory) //
//                                TIS.get().getPluginManager().uberClassLoader.loadClass(selectElementCreator).newInstance());
//                    }
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                }
//                hostExtraProp.multiItemsViewType = new MultiItemsViewType(ViewType.parse(hostExtraProp.getStrProp(KEY_VIEW_TYPE)),
//                        elementCreator, hostPluginClazz);
//            }
//
//            return hostExtraProp.multiItemsViewType;
//        }

//        @JSONField(serialize = false)
//        public Optional<CMeta.ElementCreatorFactory> getCMetaCreator(Class<? extends Describable> hostPluginClazz) {
//            return this.multiItemsViewType(hostPluginClazz).tupleFactory;
//        }
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

        // private Optional<CMeta.ElementCreatorFactory> elementCreator;

//        public void merge(Class<? extends Describable> hostedPluginClazz, Props p) {
//            this.hostedExtraProps.put(hostedPluginClazz, new HostedExtraProps(p));
//        }

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

    public static class ParsePostMCols {
        public List<CMeta> writerCols = Lists.newArrayList();
        public boolean validateFaild = false;
        public boolean pkHasSelected = false;
    }

}
