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

package com.qlangtech.tis.plugin.datax;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.qlangtech.tis.datax.impl.DataxReader;
import com.qlangtech.tis.datax.impl.ESTableAlias;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.IPropertyType;
import com.qlangtech.tis.extension.PluginFormProperties;
import com.qlangtech.tis.extension.TISExtension;
import com.qlangtech.tis.extension.impl.BaseSubFormProperties;
import com.qlangtech.tis.extension.impl.EnumFieldMode;
import com.qlangtech.tis.extension.impl.PropertyType;
import com.qlangtech.tis.extension.impl.SuFormProperties;
import com.qlangtech.tis.manage.common.Option;
import com.qlangtech.tis.plugin.IdentityName;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.SubForm;
import com.qlangtech.tis.plugin.annotation.Validator;
import com.qlangtech.tis.plugin.ds.CMeta;
import com.qlangtech.tis.plugin.ds.ColumnMetaData;
import com.qlangtech.tis.plugin.ds.DataSourceMeta;
import com.qlangtech.tis.plugin.ds.ISelectedTab;
import com.qlangtech.tis.plugin.ds.TableNotFoundException;
import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;
import com.qlangtech.tis.runtime.module.misc.IFieldErrorHandler;
import com.qlangtech.tis.sql.parser.tuple.creator.EntityName;
import com.qlangtech.tis.util.impl.AttrVals;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.qlangtech.tis.runtime.module.misc.IFieldErrorHandler.joinField;

/**
 * @author: baisui 百岁
 * @create: 2021-04-08 13:29
 **/
public class SelectedTab implements Describable<SelectedTab>, ISelectedTab, IdentityName {
    private static final String KEY_TABLE_COLS = "tableRelevantCols";
    public static final String KEY_SOURCE_PROPS = "sourceProps";
    public static final String KEY_FIELD_COLS = "cols";
    public static final String KEY_FIELD_PRIMARY_KEYS = "primaryKeys";
    private static final Logger logger = LoggerFactory.getLogger(SelectedTab.class);

    // 针对增量构建流程中的属性扩展
    private transient SelectedTabExtend incrSourceProps;
    private transient SelectedTabExtend incrSinkProps;
    public transient SelectedTabExtend sourceProps;

    // 表名称
    @FormField(identity = true, ordinal = 0, type = FormFieldType.INPUTTEXT, validate = {Validator.require})
    public String name;

    @FormField(ordinal = 3, type = FormFieldType.ENUM, validate = {Validator.require})
    public List<String> primaryKeys;

    // 用户可以自己设置where条件
    @FormField(ordinal = 100, type = FormFieldType.INPUTTEXT)
    public String where;


    @Override
    public List<String> getPrimaryKeys() {
        return this.primaryKeys;
    }

    /**
     * candidate cols fetch by static method: getColsCandidate
     */
    @FormField(ordinal = 200, type = FormFieldType.MULTI_SELECTABLE, validate = {Validator.require})
    public List<CMeta> cols = Lists.newArrayList();


    public List<String> getColKeys() {
        return this.cols.stream().filter((c) -> !c.isDisable()).map((c) -> c.getName()).collect(Collectors.toList());
    }

    public static List<CMeta> getColsCandidate() {
        return getContextTableCols((cols) -> cols.stream());
    }

    // private transient List<CMeta> shadowCols = null;

    public SelectedTab(String name) {
        this.name = name;
    }

    public String identityValue() {
        return this.name;
    }

    public <T extends SelectedTabExtend> T getIncrSinkProps() {
        return (T) incrSinkProps;
    }

    public void setIncrSinkProps(SelectedTabExtend incrSinkProps) {
        this.incrSinkProps = incrSinkProps;
    }

    public SelectedTabExtend getSourceProps() {
        return this.sourceProps;
    }

    public void setSourceProps(SelectedTabExtend sourceProps) {
        this.sourceProps = sourceProps;
    }

    public <T extends SelectedTabExtend> T getIncrSourceProps() {
        return (T) this.incrSourceProps;
    }

    public <T extends SelectedTabExtend> List<T> getIncrExtProp() {
        List<T> result = Lists.newArrayList();
        if (this.getIncrSourceProps() != null) {
            result.add(this.getIncrSourceProps());
        }
        if (this.getIncrSinkProps() != null) {
            result.add(this.getIncrSinkProps());
        }
        return result;
    }

    public void setIncrSourceProps(SelectedTabExtend incrProps) {
        this.incrSourceProps = incrProps;
    }


    /**
     * 默认主键
     *
     * @return
     * @see EnumFieldMode
     */
    public static List<String> getDftPks() {
        return getContextTableCols((cols) -> cols.stream().filter((col) -> col.isPk())) //
                .stream().map((col) -> col.getName()).collect(Collectors.toList());
    }

    public SelectedTab() {
    }

    /**
     * 取得默认的表名称
     *
     * @return
     */
    public static String getDftTabName() {
        DataxReader dataXReader = DataxReader.getThreadBingDataXReader();
        if (dataXReader == null) {
            return StringUtils.EMPTY;
        }

        try {
            List<ISelectedTab> selectedTabs = dataXReader.getSelectedTabs();
            if (CollectionUtils.isEmpty(selectedTabs)) {
                return StringUtils.EMPTY;
            }
            for (ISelectedTab tab : selectedTabs) {
                return tab.getName();
            }
        } catch (Throwable e) {
            logger.warn(dataXReader.getDescriptor().getDisplayName() + e.getMessage());
        }

        return StringUtils.EMPTY;
    }

    public String getWhere() {
        return this.where;
    }


    public void setWhere(String where) {
        this.where = where;
    }

    public String getName() {
        return this.name;
    }

    public boolean isAllCols() {
        return this.cols.isEmpty();
    }

    public final List<CMeta> getCols() {
        return this.cols.stream().filter((c) -> !c.isDisable()).collect(Collectors.toList());
    }

    public boolean containCol(String col) {
        if (CollectionUtils.isEmpty(this.cols)) {
            return false;
        }

        for (CMeta c : this.cols) {
            if (StringUtils.equals(c.getName(), col)) {
                return true;
            }
        }
        return false;
        //   return cols != null && this.cols.contains(col);
    }

    public void setCols(List<String> cols) {
        //  this.cols = cols;
        throw new UnsupportedOperationException();
    }

    public static List<Option> getContextOpts(Function<List<ColumnMetaData>, Stream<ColumnMetaData>> func) {
        return getContextTableColsStream(func).collect(Collectors.toList());
    }

    public static List<CMeta> getContextTableCols(Function<List<ColumnMetaData>, Stream<ColumnMetaData>> func) {
        return getContextTableColsStream(func).map(ColumnMetaData::convert).collect(Collectors.toList());
    }


    private static Stream<ColumnMetaData> getContextTableColsStream(Function<List<ColumnMetaData>,
            Stream<ColumnMetaData>> func) {
        SuFormProperties.SuFormGetterContext context = SuFormProperties.subFormGetterProcessThreadLocal.get();
        if (context == null || context.plugin == null) {
            List<ColumnMetaData> empt = Collections.emptyList();
            return empt.stream();
        }
        Describable plugin = Objects.requireNonNull(context.plugin, "context.plugin can not be null");
        if (!(plugin instanceof DataSourceMeta)) {
            throw new IllegalStateException("plugin must be type of " + DataSourceMeta.class.getName() + ", now type "
                    + "of " + plugin.getClass().getName());
        }
        DataSourceMeta dsMeta = (DataSourceMeta) plugin;
        List<ColumnMetaData> cols = context.getContextAttr(KEY_TABLE_COLS, (key) -> {
            try {
                return dsMeta.getTableMetadata(false, EntityName.parse(context.getSubFormIdentityField()));
            } catch (TableNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
        return func.apply(cols);
    }


    @TISExtension
    public static class DefaultDescriptor extends Descriptor<SelectedTab> implements SubForm.ISubFormItemValidate,
            FormFieldType.IMultiSelectValidator {


        @Override
        protected final boolean verify(IControlMsgHandler msgHandler, Context context, PostFormVals postFormVals) {
            return this.validateAll(msgHandler, context, postFormVals);
        }

        @Override
        public boolean validate(IFieldErrorHandler msgHandler, Optional<IPropertyType.SubFormFilter> subFormFilter,
                                Context context, String fieldName, List<FormFieldType.SelectedItem> items) {

            if (SelectedTab.KEY_FIELD_COLS.equals(fieldName)) {
                int selectCount = 0;
                for (FormFieldType.SelectedItem item : items) {
                    if (item.isChecked()) {
                        selectCount++;
                    }
                }
                if (selectCount < 1) {
                    msgHandler.addFieldError(context, fieldName, "请选择需要的列");
                    return false;
                }
            }

            return true;
        }

        @Override
        protected final boolean validateAll(IControlMsgHandler msgHandler, Context context, PostFormVals postFormVals) {

            //   SelectedTab tab = ;// this.newInstance(null, postFormVals.rawFormData, Optional.empty());
            //SelectedTab tab = plugin.getInstance();
            //            if (tab.cols.isEmpty()) {
            //                msgHandler.addFieldError(context, SelectedTab.KEY_FIELD_COLS, "请选择");
            //                return false;
            //            }
            return this.validateAll(msgHandler, context, (SelectedTab) postFormVals.newInstance());
        }

        @Override
        public boolean validateSubFormItems(IControlMsgHandler msgHandler, Context context,
                                            BaseSubFormProperties props, IPropertyType.SubFormFilter filter,
                                            AttrVals formData) {

            formData.vistAttrValMap((tab, item) -> {
                if (item instanceof JSONObject) {
                    msgHandler.addErrorMessage(context, "请为表‘" + tab + "’设置必要属性");
                }
            });

            if (context.hasErrors()) {
                return false;
            }

            Integer maxReaderTabCount = Integer.MAX_VALUE;
            try {
                maxReaderTabCount =
                        Integer.parseInt(filter.uploadPluginMeta.getExtraParam(ESTableAlias.MAX_READER_TABLE_SELECT_COUNT));
            } catch (Throwable e) {

            }

            if (formData.size() > maxReaderTabCount) {
                msgHandler.addErrorMessage(context, "导入表不能超过" + maxReaderTabCount + "张");
                return false;
            }

            return true;
        }

        public PluginFormProperties getPluginFormPropertyTypes(Optional<IPropertyType.SubFormFilter> subFormFilter) {

            return super.getPluginFormPropertyTypes(subFormFilter);
        }

        public final boolean validateSubForm(IControlMsgHandler msgHandler, Context context, SelectedTab tab) {
            return validateAll(msgHandler, context, tab);
        }

        protected boolean validateAll(IControlMsgHandler msgHandler, Context context, SelectedTab tab) {
            Descriptor<SelectedTab> tabDesc = tab.getDescriptor();
            tabDesc.getExtractProps();
            List<String> lackPks = Lists.newArrayList();
            List<String> colKeys = tab.getColKeys();
            for (String pk : tab.primaryKeys) {
                if (!colKeys.contains(pk)) {
                    lackPks.add(pk);
                }
            }
            if (lackPks.size() > 0) {
                PropertyType colProp = (PropertyType) tab.getDescriptor().getPropertyType(KEY_FIELD_COLS);
                final List<String> fieldNames = Lists.newArrayList();

                colProp.multiSelectablePropProcess((viewType) -> {
                    switch (viewType.viewType) {
                        case IdList:
                            fieldNames.add(KEY_FIELD_COLS);// = Lists.newArrayList();
                            break;
                        case TupleList:
                            List<CMeta> tabCols = tab.cols;
                            AtomicInteger index = new AtomicInteger();
                            Map<String, Integer> colsIndex //
                                    = tabCols.stream().collect(Collectors.toMap((c) -> c.getName(),
                                    (c) -> index.getAndIncrement()));
                            for (String lackKey : lackPks) {
                                fieldNames.add(joinField(KEY_FIELD_COLS,
                                        Collections.singletonList(colsIndex.get(lackKey)), CMeta.FIELD_NAME));
                            }

                            break;
                        default:
                            throw new IllegalStateException("unhandle view type:" + viewType);
                    }
                    return null;
                });


                for (String fieldName : fieldNames) {
                    msgHandler.addFieldError(context, fieldName, "由于" + String.join(",", lackPks) + "选为主键," +
                            "因此需要将它（们）选上");
                }

                return false;
            }

            return true;
        }
    }


}
