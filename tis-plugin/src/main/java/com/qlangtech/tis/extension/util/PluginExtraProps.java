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
import com.qlangtech.tis.IPluginEnum;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.ElementPluginDesc;
import com.qlangtech.tis.extension.IPropertyType;
import com.qlangtech.tis.extension.impl.IOUtils;
import com.qlangtech.tis.extension.impl.PropertyType;
import com.qlangtech.tis.extension.util.AbstractPropAssist.MarkdownHelperContent;
import com.qlangtech.tis.manage.common.Option;
import com.qlangtech.tis.manage.common.TisUTF8;
import com.qlangtech.tis.plugin.IEndTypeGetter;
import com.qlangtech.tis.plugin.IdentityName;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.Validator;
import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;
import com.qlangtech.tis.util.DescriptorsJSON;
import com.qlangtech.tis.util.HeteroEnum;
import com.qlangtech.tis.util.UploadPluginMeta;
import org.apache.commons.collections.CollectionUtils;
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
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.qlangtech.tis.plugin.IEndTypeGetter.EndType.KEY_END_TYPE;
import static com.qlangtech.tis.util.HeteroEnum.DATASOURCE;

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
    public static final String KEY_PLUGIN = "plugin";
    public static final String KEY_CREATOR_HETERO = "hetero";
    public static final String KEY_DESC_NAME = "descName";

    public static final String KEY_ROUTER_LINK = "routerLink";
    // 枚举过滤器,只对底层Describle类型的enum起效
    public static final String KEY_ENUM_FILTER = "subDescEnumFilter";
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
        hyperlink("hyperlink"), dbQuickManager("dbQuickManager"), paramCfg("paramCfg");
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


                    pps = new Props(o.getJSONObject(propKey));

                    //                    pps.getRefCreator().ifPresent((creator) -> {
                    //                        Props.validate(creator, propKey, pluginClazz, resourceName, false);
                    //                    });


                    StringBuffer asynHelp = null;
                    if ((asynHelp = propHelps.get(propKey)) != null) {
                        pps.tagAsynHelp(new MarkdownHelperContent(asynHelp.toString()));
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


        PluginExtraProps ep = visitAncestorsClass(clazz, (c, extraProps, finalChild) -> {
            Optional<PluginExtraProps> nxtExtraProps = parseExtraProps(c);
            if (nxtExtraProps.isPresent()) {
                if (extraProps == null) {
                    extraProps = nxtExtraProps.get();
                } else {
                    extraProps.mergeProps(nxtExtraProps.get());
                }
            }
            if (finalChild && extraProps != null) {
                extraProps.forEach((k, prop) -> {
                    //  prop.setFieldRefCreateor();
                });
            }
            return extraProps;
        });


        if (ep != null) {
            String resourceName = clazz.getSimpleName() + ".json";
            for (Map.Entry<String, PluginExtraProps.Props> entry : ep.entrySet()) {
                Optional<FieldRefCreateor> refCreator = entry.getValue().getRefCreator();
                refCreator.ifPresent((createor) -> {
                    PluginExtraProps.Props.validate(createor, entry.getKey(), clazz, resourceName, true);
                });

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
            extraProps = clazzVisitor.process(targetClass, extraProps, i == 0);
        }
        return extraProps;
    }


    public interface IClassVisitor<T> {
        /**
         *
         * @param clazz
         * @param extraProps
         * @param finalChild 是否是最后一个子类
         * @return
         */
        T process(Class<?> clazz, T extraProps, boolean finalChild);
    }

    //    private static JSONObject validate(JSONObject props, String propKey, Class<?> pluginClazz, String
    //    resourceName,
    //                                       boolean finalValidate) {
    //        String errDesc = createErrorMsg(propKey, pluginClazz, resourceName);
    //        Object creator = props.get(KEY_CREATOR);
    //        if (creator != null) {
    //            if (!(creator instanceof JSONObject)) {
    //                throw new IllegalStateException("prop creator must be type of JSONObject:" + errDesc);
    //            }
    //            if (finalValidate) {
    //                JSONObject creatorJ = (JSONObject) creator;
    //
    //                //  Objects.requireNonNull(creatorJ.get(KEY_ROUTER_LINK), errDesc);
    //                Objects.requireNonNull(creatorJ.get(KEY_LABEL), errDesc);
    //                JSONObject pmeta = null;
    //                JSONArray plugins = creatorJ.getJSONArray(KEY_PLUGIN);
    //                boolean assistTypeEmpty = StringUtils.isEmpty(creatorJ.getString(KEY_CREATOR_ASSIST_TYPE));
    //                if (plugins != null) {
    //                    for (int i = 0; i < plugins.size(); i++) {
    //                        pmeta = plugins.getJSONObject(i);
    //                        if (StringUtils.isBlank(pmeta.getString(KEY_CREATOR_HETERO))
    //                                || StringUtils.isBlank(pmeta.getString(KEY_DESC_NAME))
    //                            // 由于插件中参数不一定是必须的，所以先把以下校验去掉： "extraParam": "append_true"
    //                            //        || StringUtils.isBlank(pmeta.getString("extraParam"))
    //                        ) {
    //                            throw new IllegalStateException("pmeta is illegal:" + pmeta.toJSONString() + "," +
    //                                    "pluginClazz:" + pluginClazz.getName() + ",errDesc:" + errDesc);
    //                        }
    //                        /**
    //                         * 如果assitType 为空，则查看plugin 的KEY_CREATOR_HETERO 如果为 ‘params-cfg’ 则默认ASSIST_TYPE类型为
    //                         RouterAssistType.paramCfg
    //                         */
    //                        if (assistTypeEmpty) {
    //                            String hetero = pmeta.getString(KEY_CREATOR_HETERO);
    //                            if (StringUtils.isNotEmpty(hetero)) {
    //                                if (!DATASOURCE.identity.equals(hetero)) {
    //                                    creatorJ.put(KEY_CREATOR_ASSIST_TYPE, RouterAssistType.paramCfg.token);
    //                                    assistTypeEmpty = false;
    //                                }
    //                            }
    //                        }
    //                    }
    //                }
    //
    //                if (assistTypeEmpty && StringUtils.isNotEmpty(creatorJ.getString(KEY_ROUTER_LINK))) {
    //                    creatorJ.put(KEY_CREATOR_ASSIST_TYPE, RouterAssistType.hyperlink.token);
    //                }
    //
    //                /**
    //                 * 校验assistType
    //                 */
    //                try {
    //                    RouterAssistType.parse(creatorJ.getString(KEY_CREATOR_ASSIST_TYPE));
    //                } catch (Exception e) {
    //                    throw new RuntimeException(errDesc, e);
    //                }
    //            }
    //        }
    //        return props;
    //    }

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
                        return (pre == null) ? PropertyType.buildPropertyTypes(Optional.empty(),
                                elmtDesc.getElementDesc().clazz) : pre;
                    });
                    if (!pp.containsKey(entry.getKey())) {
                        throw new IllegalStateException("prop key:" + entry.getKey() + " relevant prop must exist , " + "exist props keys:" + pp.keySet().stream().collect(Collectors.joining(",")));
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


    public static class FieldRefCreateor {
        String label;
        RouterAssistType assistType = null;

        /**
         * 下拉列表选项
         */
        private Supplier<List<Option>> selectableOpts = () -> Collections.emptyList();

        private Supplier<Object> dftValGetter = () -> null;

        List<CandidatePlugin> candidatePlugins = Lists.newArrayList();

        public FieldRefCreateor() {
        }

        public String getLabel() {
            return label;
        }

        public List<Option> getValOptions() {
            return this.selectableOpts.get();
        }

        public void setSelectableOpts(Supplier<List<Option>> selectableOpts) {
            this.selectableOpts = selectableOpts;
        }

        public List<CandidatePlugin> getCandidatePlugins() {
            return this.candidatePlugins;
        }

        public void addCandidatePlugin(CandidatePlugin candidate) {
            this.candidatePlugins.add(candidate);
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public Object getDftValue() {
            return dftValGetter.get();
        }

        public void setDftValGetter(Supplier<Object> dftValGetter) {
            this.dftValGetter = dftValGetter;
        }

        public RouterAssistType getAssistType() {
            return assistType;
        }

        public void setAssistType(RouterAssistType assistType) {
            this.assistType = assistType;
        }
    }

    public static class CandidatePlugin {
        protected static final String KEY_DISABLE_PLUGIN_INSTALL = "disablePluginInstall";
        protected static final String KEY_INSTALLED = "installed";
        private final String displayName;
        private String description;
        // private String targetPluginCategory;
        private String hetero;
        private final Optional<String> targetItemDesc;
        /**
         * 可以为空
         */
        private Optional<Descriptor> descriptor;


        /**
         *
         * @param displayName
         * @param hetero
         */
        public CandidatePlugin(String displayName, Optional<String> targetItemDesc, String hetero) {
            if (StringUtils.isEmpty(displayName)) {
                throw new IllegalArgumentException("displayName can not be empty");
            }
            this.displayName = displayName;
            // this.description = displayName;
            this.targetItemDesc = Objects.requireNonNull(targetItemDesc, "targetItemDesc can not be null");
            //  this.targetPluginCategory = StringUtils.defaultIfEmpty(targetPluginCategory, displayName);
            this.hetero = hetero;
        }

        public void setExtraProps(Optional<IEndTypeGetter.EndType> endType, JSONObject option) {
            option.put(KEY_DISABLE_PLUGIN_INSTALL, false);
            option.put("extendpoint", this.getHetero().getExtensionPoint().getName());
            endType.ifPresent((et) -> {
                option.put(KEY_END_TYPE, et.getVal());
            });
            option.put(KEY_INSTALLED, this.getInstalledPluginDescriptor() != null);
            Descriptor<?> installedDesc = this.getInstalledPluginDescriptor();
            if (installedDesc != null) {
                option.put("version", installedDesc.getId());
            }
        }

        public String getDescription() {
            return this.description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public static JSONArray convertOptionsArray(Optional<IEndTypeGetter.EndType> endType,
                                                    List<CandidatePlugin> candidatePlugins) {
            JSONArray optionsArray = new JSONArray();
            for (int i = 0; i < candidatePlugins.size(); i++) {
                CandidatePlugin candidate = candidatePlugins.get(i);
                JSONObject option = new JSONObject();
                option.put("index", i);
                option.put("name", candidate.getDisplayName());
                option.put("description", candidate.getDescription());

                candidate.setExtraProps(endType, option);
                optionsArray.add(option);
            }
            return optionsArray;
        }

        //        private static void setExtraProps(Optional<IEndTypeGetter.EndType> endType, JSONObject option,
        //        CandidatePlugin candidate) {
        //            option.put("extendpoint", candidate.getHetero().getExtensionPoint().getName());
        //            endType.ifPresent((et) -> {
        //                option.put("endType", et.getVal());
        //            });
        //            option.put("installed", candidate.getInstalledPluginDescriptor() != null);
        //            Descriptor<?> installedDesc = candidate.getInstalledPluginDescriptor();
        //            if (installedDesc != null) {
        //                option.put("version", installedDesc.getId());
        //            }
        //        }

        public void validate(String errDesc, Class<?> pluginClazz) {
            if (StringUtils.isBlank(this.hetero) || StringUtils.isBlank(this.displayName)
                // 由于插件中参数不一定是必须的，所以先把以下校验去掉： "extraParam": "append_true"
                //        || StringUtils.isBlank(pmeta.getString("extraParam"))
            ) {
                throw new IllegalStateException("pmeta is illegal,hetero:" + this.hetero + ",displayName:" + this.displayName + "," + "pluginClazz:" + pluginClazz.getName() + ",errDesc:" + errDesc);
            }
        }

        //        public void setDescriptor(Descriptor descriptor) {
        //            this.descriptor = descriptor;
        //        }


        public String getTargetItemDesc() {
            return this.targetItemDesc.orElse(this.getDisplayName());
        }

        public String getDisplayName() {
            return this.displayName;
        }

        /**
         * 根据existOpts中可选项，选择最大的后缀+1，生成最新主键
         *
         * @param existOpts
         * @return
         */
        public <T extends IdentityName> IdentityName createNewPrimaryFieldValue(List<T> existOpts) {
            //            String descName = StringUtils.lowerCase(this.getDisplayName());
            //            Pattern pattern = Pattern.compile(descName + "-?(\\d+)");
            //            Matcher matcher = null;
            //            int maxSufix = 1;
            //            for (IdentityName opt : existOpts) {
            //                matcher = pattern.matcher(StringUtils.lowerCase(opt.identityValue()));
            //                if (matcher.matches()) {
            //                    int curr;
            //                    if ((curr = Integer.valueOf(matcher.group(1))) >= maxSufix) {
            //                        maxSufix = curr + 1;
            //                    }
            //                }
            //            }
            //            return descName + "-" + maxSufix;
            return IdentityName.createNewPrimaryFieldValue(this.getDisplayName(), existOpts);
        }

        public Descriptor getInstalledPluginDescriptor() {
            return this.getInstalledPluginDescriptor(false);
        }

        public Descriptor getInstalledPluginDescriptor(boolean useFresh) {
            if (useFresh || descriptor == null) {
                IPluginEnum hetero = this.getHetero();
                List<Descriptor> descriptors = hetero.descriptors();
                for (Descriptor d : descriptors) {
                    if (this.getDisplayName().equals(d.getDisplayName())) {
                        descriptor = Optional.of(d);
                        return d;
                    }
                }
                descriptor = Optional.empty();
            }

            return descriptor.orElse(null);
        }

        //        public void setDisplayName(String displayName) {
        //            this.displayName = displayName;
        //        }

        public IPluginEnum getHetero() {
            return HeteroEnum.of(this.hetero);
        }

        @Override
        public String toString() {
            return "displayName='" + displayName + '\'' + ", hetero='" + hetero + '\'';
        }
    }


    public static class Props {
        public static final String KEY_HELP = "help";
        public static final String KEY_VALIDATOR = "validators";
        public static final String KEY_VIEW_TYPE = "viewtype";
        public static final String KEY_ASYNC_HELP = "asyncHelp";
        private final JSONObject props;
        private MarkdownHelperContent asynHelp;

        private Optional<FieldRefCreateor> _fieldRefCreateor;

        public Props(JSONObject props) {
            this.props = props;
        }

        //        private void setFieldRefCreateor() {
        //            this._fieldRefCreateor = Optional.ofNullable(createFieldRefCreateor(this.props));
        //        }

        private static FieldRefCreateor createFieldRefCreateor(JSONObject props) {
            FieldRefCreateor createor = null;
            Object creator = props.get(KEY_CREATOR);
            if (creator != null) {
                if (!(creator instanceof JSONObject)) {
                    throw new IllegalStateException("prop creator must be type of JSONObject,but is " + creator.getClass());
                }
                createor = new FieldRefCreateor();

                JSONObject creatorJ = (JSONObject) creator;
                createor.setLabel(creatorJ.getString(Option.KEY_LABEL));
                //  Objects.requireNonNull(creatorJ.get(KEY_ROUTER_LINK), errDesc);
                // Objects.requireNonNull(creatorJ.get(KEY_LABEL), errDesc);
                JSONObject pmeta = null;
                JSONArray plugins = creatorJ.getJSONArray(KEY_PLUGIN);
                boolean assistTypeEmpty = StringUtils.isEmpty(creatorJ.getString(KEY_CREATOR_ASSIST_TYPE));
                if (plugins != null) {
                    for (int i = 0; i < plugins.size(); i++) {
                        pmeta = plugins.getJSONObject(i);

                        /**
                         * 如果assitType 为空，则查看plugin 的KEY_CREATOR_HETERO 如果为 ‘params-cfg’ 则默认ASSIST_TYPE类型为
                         * RouterAssistType.paramCfg
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

                        createor.addCandidatePlugin(new CandidatePlugin( //
                                pmeta.getString(KEY_DESC_NAME)//
                                , Optional.ofNullable(pmeta.getString(UploadPluginMeta.KEY_TARGET_PLUGIN_DESC))//
                                , pmeta.getString(KEY_CREATOR_HETERO)));
                    }
                }

                if (assistTypeEmpty && StringUtils.isNotEmpty(creatorJ.getString(KEY_ROUTER_LINK))) {
                    creatorJ.put(KEY_CREATOR_ASSIST_TYPE, RouterAssistType.hyperlink.token);
                }
                createor.setAssistType(RouterAssistType.parse(creatorJ.getString(KEY_CREATOR_ASSIST_TYPE)));


                createor.setSelectableOpts(() -> {
                    JSONArray enums = props.getJSONArray(Descriptor.KEY_ENUM_PROP);
                    JSONObject option = null;
                    if (CollectionUtils.isEmpty(enums)) {
                        return Collections.emptyList();
                    }
                    List<Option> opts = Lists.newArrayList();
                    for (int idx = 0; idx < enums.size(); idx++) {
                        option = enums.getJSONObject(idx);
                        opts.add(Option.create(option));
                    }
                    return opts;
                });

                if (props.containsKey(KEY_DFTVAL_PROP)) {
                    createor.setDftValGetter(() -> {
                        return props.get(KEY_DFTVAL_PROP);
                    });
                }

            }
            return createor;
        }

        public static void validate(FieldRefCreateor creatorJ, String propKey, Class<?> pluginClazz,
                                    String resourceName, boolean finalValidate) {
            String errDesc = createErrorMsg(propKey, pluginClazz, resourceName);

            if (finalValidate) {
                Objects.requireNonNull(creatorJ.getLabel(), errDesc);
                // JSONObject pmeta = null;
                List<CandidatePlugin> plugins = creatorJ.getCandidatePlugins();//.getJSONArray(KEY_PLUGIN);
                for (CandidatePlugin plugin : plugins) {
                    plugin.validate(errDesc, pluginClazz);
                }

                Objects.requireNonNull(creatorJ.getAssistType(), errDesc);
            }

        }

        /**
         * 解析creator部分
         * <pre>
         * "eprops": {
         * 	"help": "描述：Hadoop hdfs文件系统namenode节点地址。格式：hdfs://ip:端口；例如：hdfs://127.0.0.1:9000",
         * 	"creator": {
         *  	"plugin": [{
         * 			"hetero": "fs",
         * 			"descName": "HDFS"
         *        },
         *       {
         * 			"hetero": "fs",
         * 	    	"descName": "Aliyun-Jindo-HDFS"
         *      }
         * 	],
         * 	"label": "管理",
         * 	"assistType": "paramCfg"
         *  }
         * }
         *
         * </pre>
         *
         * @return
         */
        @JSONField(serialize = false)
        public Optional<FieldRefCreateor> getRefCreator() {
            if (this._fieldRefCreateor == null) {
                this._fieldRefCreateor = Optional.ofNullable(createFieldRefCreateor(this.props));
            }
            return this._fieldRefCreateor;
        }

        @JSONField(serialize = false)
        public String getAsynHelp() {
            if (this.asynHelp == null) {
                return null;
            }
            return this.asynHelp.getContent().toString();
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
                    return new ValidatorCfg(Validator.parse(String.valueOf(split[0])),
                            KEY_DISABLE.equalsIgnoreCase(split[1]));
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
        public void tagAsynHelp(MarkdownHelperContent asynHelp) {
            props.put(KEY_ASYNC_HELP, true);
            props.remove(KEY_HELP);
            this.asynHelp = asynHelp;
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
                MarkdownHelperContent tpl = this.asynHelp;
                this.asynHelp = new MarkdownHelperContent(p.asynHelp);
                if (tpl != null) {
                    this.asynHelp = this.asynHelp.append(tpl);
                }
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
