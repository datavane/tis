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

package com.qlangtech.tis.plugin.ontology.impl.objtype;

import com.alibaba.citrus.turbine.Context;
import com.google.common.collect.Lists;
import com.qlangtech.tis.extension.DescriptorUseableShortComment;
import com.qlangtech.tis.extension.TISExtension;
import com.qlangtech.tis.plugin.IPluginStore;
import com.qlangtech.tis.plugin.IdentityName;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.Validator;
import com.qlangtech.tis.plugin.ds.DataSourceFactory;
import com.qlangtech.tis.plugin.items.manager.ListItemsManager;
import com.qlangtech.tis.plugin.items.manager.ListItemsManagerAction;
import com.qlangtech.tis.plugin.ontology.OntologyObjectType;
import com.qlangtech.tis.plugin.ontology.impl.OntologyPluginMeta;
import com.qlangtech.tis.plugin.ontology.impl.binding.BindingSwitchReport;
import com.qlangtech.tis.plugin.ontology.impl.binding.DefaultBindingSwitcher;
import com.qlangtech.tis.plugin.ontology.impl.binding.OntologyBindingSwitcher;
import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;
import com.qlangtech.tis.util.IPluginContext;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 批量修改Object Type 的dataSource
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/5/18
 */
public class ObjectTypeChangeBinding extends ListItemsManager implements IPluginStore.ManipuldateProcessor {
    private static final Logger logger = LoggerFactory.getLogger(ObjectTypeChangeBinding.class);
    public static final String ACTION_CHANGE_BINDING = "changeBinding";

    private static final OntologyBindingSwitcher BINDING_SWITCHER = new DefaultBindingSwitcher();

    @FormField(ordinal = 0, type = FormFieldType.ENUM, validate = {Validator.require})
    public String dbName;

    public DataSourceFactory loadDataSource() {
        return DataSourceFactory.load(this.dbName);
    }

    @Override
    public void execute(IPluginContext pluginContext, Context context) {
        OntologyPluginMeta pluginMeta = OntologyPluginMeta.createPluginMeta();
        OntologyObjectType beChange = null;
        DataSourceFactory targetFactory = this.loadDataSource();
        for (IdentityName objTypeName : this.getTargetIds()) {
            beChange = OntologyObjectType.loadDetail(pluginMeta.getDomain(), objTypeName.identityValue());
            BINDING_SWITCHER.switchBinding(beChange, targetFactory, pluginContext);
        }

        pluginContext.addActionMessage(context, "已经成功将" + this.getTargetIds().size() + "个实例切换到‘" + dbName + "’数据源");
        //BINDING_SWITCHER.switchBinding();
        //for (IdentityName  this.getTargetIds());

        // OntologyObjectType.loadDetail(pluginMeta, )
    }

    @TISExtension
    public static class DefaultDesc extends ListItemsManager.BasicDesc implements DescriptorUseableShortComment {
        public DefaultDesc() {
            super();
        }

        @Override
        public final ListItemsManagerTargetResType parseTargetResType() {
            return ListItemsManagerTargetResType.OntologyObjectType;
        }

        @Override
        protected boolean validateAll(IControlMsgHandler msgHandler, Context context, PostFormVals postFormVals) {

            ObjectTypeChangeBinding changeBinding = postFormVals.newInstance();
            DataSourceFactory targetFactory = changeBinding.loadDataSource();
            OntologyPluginMeta pluginMeta = OntologyPluginMeta.createPluginMeta();
            OntologyObjectType beChange = null;
            BindingSwitchReport validateResult = null;
            List<Pair<BindingSwitchReport, String>> invaildReports = Lists.newArrayList();

            for (IdentityName objTypeName : changeBinding.getTargetIds()) {

                beChange = OntologyObjectType.loadDetail(pluginMeta.getDomain(), objTypeName.identityValue());
                validateResult = BINDING_SWITCHER.validate((IPluginContext) msgHandler, beChange, targetFactory);
                if (!validateResult.ok()) {
                    invaildReports.add(Pair.of(validateResult, objTypeName.identityValue()));
                }
            }

            if (CollectionUtils.isNotEmpty(invaildReports)) {
                invaildReports.forEach(pair -> {
                    BindingSwitchReport r = pair.getKey();
                    logger.warn("ObjectType '{}' cannot switch datasource: error={}, missingColumns={}, "
                                    + "extraColumns={}, typeMismatches={}",
                            pair.getValue(), r.error(), r.missingColumns(), r.extraColumns(), r.typeMismatches());
                });
                msgHandler.addErrorMessage(context,
                        "实体对象：" + invaildReports.stream().map(Pair::getValue).collect(Collectors.joining(",")) +
                                "，与目标‘" + targetFactory.name + "’端中表不一致（名称或结构）");
                return false;
            }

            return true;
        }

        @Override
        public ListItemsManagerAction getAction() {
            return new ListItemsManagerAction(ACTION_CHANGE_BINDING);
        }

        @Override
        public String shortComment() {
            return "批量将对象类型切换绑定到指定数据源";
        }

    }
}
