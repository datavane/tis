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
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.impl.PropertyType;
import com.qlangtech.tis.manage.common.Option;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.ds.CMeta;
import com.qlangtech.tis.plugin.ds.DataTypeMeta.IMultiItemsView;
import com.qlangtech.tis.plugin.ds.ElementCreatorFactory;
import com.qlangtech.tis.plugin.ds.IMultiElement;
import com.qlangtech.tis.plugin.ds.IdlistElementCreatorFactory;
import com.qlangtech.tis.plugin.ds.ViewContent;
import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;
import org.apache.commons.beanutils.BeanUtilsBean2;
import org.apache.commons.lang.StringUtils;

import java.beans.PropertyDescriptor;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.qlangtech.tis.manage.common.Option.KEY_VALUE;

/**
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2023/10/15
 */
public class MultiItemsViewType implements IMultiItemsView {
    public static final String keyColsMeta = "colsMeta";
    public final ViewFormatType viewType;
    public final ElementCreatorFactory tupleFactory;
    private final PropertyType propertyType;
    private List<String> elementPropertyKeys;

    private static String getStrProp(PluginExtraProps.Props props, String key) {
        return props.getProps().getString(key);
    }

    public static MultiItemsViewType createMultiItemsViewType(PropertyType propertyType) {
        return createMultiItemsViewType(propertyType, Objects.requireNonNull(propertyType.extraProp,
                "propertyType" + ".extraProp can not be null"));
    }

    public static MultiItemsViewType createMultiItemsViewType(PropertyType propertyType, PluginExtraProps.Props props) {
        //        if (this.multiItemsViewType == null) {
        // PluginExtraProps.Props props = propertyType.extraProp;
        ElementCreatorFactory elementCreator = null;
        ViewFormatType formatType = ViewFormatType.parse(getStrProp(Objects.requireNonNull(props,
                "props can not be " + "null"), PluginExtraProps.Props.KEY_VIEW_TYPE));
        try {
            String selectElementCreator = getStrProp(props, CMeta.KEY_ELEMENT_CREATOR_FACTORY);
            if (StringUtils.isEmpty(selectElementCreator)) {
                if (formatType == ViewFormatType.IdList) {
                    elementCreator = new IdlistElementCreatorFactory();
                } else {
                    throw new IllegalStateException("param " + CMeta.KEY_ELEMENT_CREATOR_FACTORY + " can not be " +
                            "empty,formatType:" + formatType + ",property:" + propertyType.f);
                }
            } else {
                elementCreator = ((ElementCreatorFactory) //
                        TIS.get().getPluginManager().uberClassLoader.loadClass(selectElementCreator).newInstance());
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return new MultiItemsViewType(propertyType, formatType, elementCreator);
        // }
        // return this.multiItemsViewType;
    }

    public final String getViewTypeToken() {
        return this.viewType.token;
    }

    public MultiItemsViewType(PropertyType propertyType, ViewFormatType viewType, ElementCreatorFactory tupleFactory) {
        this.viewType = viewType;
        this.tupleFactory = Objects.requireNonNull(tupleFactory, "tupleFactory can not be null");
        this.propertyType = propertyType;
    }

    public List<FormFieldType.SelectedItem> getPostSelectedItems(PropertyType attrDesc, IControlMsgHandler msgHandler
            , Context context, JSONObject eprops) {
        return this.viewType.getPostSelectedItems(attrDesc, msgHandler, context, eprops);
    }

    @Override
    public void appendExternalJsonProp(JSONObject biz) {

        this.tupleFactory.appendExternalJsonProp(this.propertyType, biz);
    }

    @Override
    public List<String> getElementPropertyKeys() {
        if (elementPropertyKeys == null) {
            try {
                PropertyDescriptor[] propertyDescs =
                        BeanUtilsBean2.getInstance().getPropertyUtils().getPropertyDescriptors(tupleFactory.createDefault(new JSONObject()));
                elementPropertyKeys = Lists.newArrayList();
                for (PropertyDescriptor desc : propertyDescs) {
                    elementPropertyKeys.add(desc.getName());
                }
                elementPropertyKeys = Collections.unmodifiableList(this.elementPropertyKeys);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return elementPropertyKeys;
    }

    @Override
    public ViewContent getViewContent() {
        return tupleFactory.getViewContentType();
    }


    public List<?> serialize2Frontend(Object bizInstance) {
        return viewType.serialize2Frontend(bizInstance);
    }

    @Override
    public String toString() {
        return "{" + "viewType=" + viewType + ", tupleFactory=" + tupleFactory.getClass().getSimpleName() + '}';
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
    public enum ViewFormatType {
        IdList("idlist" //
                , (attrDesc, msgHandler, context, eprops) -> {

            JSONArray enums = eprops.getJSONArray(Descriptor.KEY_ENUM_PROP);
            if (enums == null) {
                enums = new JSONArray();
            }
            JSONObject select = null;
            int selected = 0;
            List<FormFieldType.SelectedItem> selectedItems = Lists.newArrayList();
            FormFieldType.SelectedItem item = null;
            Object val = null;
            for (int i = 0; i < enums.size(); i++) {
                select = enums.getJSONObject(i);
                val = select.get(Option.KEY_VALUE);
                if (val.getClass() != String.class) {
                    throw new IllegalStateException("val:" + val + " must be type of String,but now is " + val.getClass());
                }
                item = new FormFieldType.SelectedItem(select.getString(Option.KEY_LABEL) //
                        , String.valueOf(val) //
                        , select.containsKey(Option.keyChecked) && select.getBoolean(Option.keyChecked));
                if (item.isChecked()) {
                    selected++;
                }
                selectedItems.add(item);
            }
            return selectedItems;
        } //
                , (obj) -> {
            List<CMeta> mulitOpt = (List<CMeta>) obj;
            return mulitOpt.stream().map((c) -> c.getName()).collect(Collectors.toList());
        }), TupleList("tuplelist" //
                , (attrDesc, msgHandler, context, eprops) -> {

            ElementCreatorFactory elementCreator = attrDesc.getCMetaCreator();
            JSONArray mcols = Objects.requireNonNull(eprops, "eprops can not be null")//
                    .getJSONObject(Descriptor.KEY_ENUM_PROP) //
                    .getJSONArray(elementCreator.getTuplesKey());
            CMeta.ParsePostMCols<?> parsePostMCols = elementCreator.parsePostMCols(attrDesc, msgHandler, context,
                    attrDesc.f.getName(), mcols);
            if (parsePostMCols.validateFaild) {
                return Collections.emptyList();
            }
            return parsePostMCols.writerCols.stream() //
                    .map((cmeta) -> new FormFieldType.SelectedItem(cmeta)).collect(Collectors.toList());
        } //
                , (obj) -> {
            return (List<IMultiElement>) obj;
        });

        private final String token;
        private final PluginExtraProps.IPostSelectedItemsGetter postSelectedItemsGetter;
        /**
         * 将biz层 object 转化成前端可序列化实例
         */
        private final Function<Object, List<?>> bizSerializeFrontend;

        private ViewFormatType(String token, PluginExtraProps.IPostSelectedItemsGetter postSelectedItemsGetter,
                               Function<Object, List<?>> bizSerializeFrontend) {
            this.token = token;
            this.postSelectedItemsGetter = postSelectedItemsGetter;
            this.bizSerializeFrontend = bizSerializeFrontend;
        }

        public static ViewFormatType parse(String token) {
            if (StringUtils.isEmpty(token)) {
                return ViewFormatType.IdList;
            }
            for (ViewFormatType t : ViewFormatType.values()) {
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
