/**
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.qlangtech.tis.extension.util;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.impl.PropertyType;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.ds.CMeta;
import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;
import org.apache.commons.beanutils.BeanUtilsBean2;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2023/10/15
 */
public class MultiItemsViewType {
    public final ViewType viewType;
    public final Optional<CMeta.ElementCreatorFactory> tupleFactory;
    //  private final Class<? extends Describable> hostPluginClazz;
    private List<String> elementPropertyKeys;

    public String getViewTypeToken() {
        return this.viewType.token;
    }

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
            PluginExtraProps.ParsePostMCols parsePostMCols = PluginExtraProps.parsePostMCols(elementCreator, msgHandler, context, keyColsMeta, mcols);
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
        private final PluginExtraProps.IPostSelectedItemsGetter postSelectedItemsGetter;
        /**
         * 将biz层 object 转化成前端可序列化实例
         */
        private final Function<Object, List<?>> bizSerializeFrontend;

        private ViewType(String token, PluginExtraProps.IPostSelectedItemsGetter postSelectedItemsGetter,
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
}
