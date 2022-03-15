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

package com.qlangtech.tis.extension.impl;

import com.google.common.collect.Lists;
import com.qlangtech.tis.datax.IDataxProcessor;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.IPropertyType;
import com.qlangtech.tis.extension.PluginFormProperties;
import com.qlangtech.tis.manage.common.TisUTF8;
import com.qlangtech.tis.offline.DataxUtils;
import com.qlangtech.tis.util.DescriptorsJSON;
import com.qlangtech.tis.util.UploadPluginMeta;
import junit.framework.TestCase;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2022-02-01 10:40
 **/
public class TestSuFormProperties extends TestCase {
    String dataXName = "testDataXName";

    File writerDescFile;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        this.writerDescFile = IDataxProcessor.getWriterDescFile(null, dataXName);
        FileUtils.write(writerDescFile, RewriteSuFormPropertiesPlugin.class.getName(), TisUTF8.get(), false);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        FileUtils.deleteQuietly(writerDescFile);
    }

    public void testVisitSubForm() {
        SubFieldContainPlugin plugin = new SubFieldContainPlugin();

        String pluginName = "test_plugin";
        // dataxName_" + dataXName
        UploadPluginMeta pluginMeta = UploadPluginMeta.parse(pluginName + ":require," + DataxUtils.DATAX_NAME + "_" + dataXName);

        IPropertyType.SubFormFilter subFormFilter
                = new IPropertyType.SubFormFilter(pluginMeta
                , SubFieldContainPlugin.PLUGIN_NAME, SubFieldContainPlugin.class.getName(), SubFieldContainPlugin.SUB_PROP_FIELD_NAME);

        Descriptor<SubFieldContainPlugin> descriptor = plugin.getDescriptor();
        assertNotNull("descriptor can not be null", descriptor);
        PluginFormProperties pluginFormPropertyTypes = descriptor.getPluginFormPropertyTypes(Optional.of(subFormFilter));
        assertNotNull("pluginFormPropertyTypes can not be null", pluginFormPropertyTypes);
        // AtomicBoolean hasExecVisitSubForm = new AtomicBoolean(false);
        boolean hasExecVisitSubForm
                = pluginFormPropertyTypes.accept(new DescriptorsJSON.SubFormFieldVisitor(Optional.of(subFormFilter)) {
            @Override
            public Boolean visit(//SuFormProperties.SuFormPropertiesBehaviorMeta behaviorMeta,
                                 SuFormProperties props) {
//                assertNotNull("behaviorMeta can not be null", behaviorMeta);
                assertNotNull("prop can not be null", props);

//                assertEquals("设置", behaviorMeta.getClickBtnLabel());
//                Map<String, SuFormProperties.SuFormPropertyGetterMeta>
//                        onClickFillData = behaviorMeta.getOnClickFillData();
//                assertEquals("onClickFillData.size() > 0", 2, onClickFillData.size());


//                SuFormProperties.SuFormPropertyGetterMeta getterMeta = onClickFillData.get("cols");
//                assertNotNull(getterMeta);
//                assertEquals("getTableMetadata", getterMeta.getMethod());
//                assertTrue("getParams equal"
//                        , CollectionUtils.isEqualCollection(Collections.singleton("id"), getterMeta.getParams()));
//
//
//                getterMeta = onClickFillData.get("recordField");
//                assertNotNull(getterMeta);
//                assertEquals("getPrimaryKeys", getterMeta.getMethod());
//                assertTrue("getParams equal"
//                        , CollectionUtils.isEqualCollection(Collections.singleton("id"), getterMeta.getParams()));

                //===============================================
                Set<Map.Entry<String, PropertyType>> kvTuples = props.getKVTuples();
                assertEquals(3, kvTuples.size());


                assertTrue(CollectionUtils.isEqualCollection(Lists.newArrayList("name", "subProp1", "subProp2")
                        , kvTuples.stream().map((kv) -> kv.getKey()).collect(Collectors.toList())));

                Object subField = props.newSubDetailed();

                assertTrue("subField must be type of " + SubFieldExtend.class.getSimpleName()
                        , subField instanceof SubFieldExtend);

                return true;
            }
        });

        assertTrue("hasExecVisitSubForm must has execute", hasExecVisitSubForm);
    }

//    public void testVisitSubForm() {
//
//    }
}
