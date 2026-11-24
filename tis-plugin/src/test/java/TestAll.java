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

import com.qlangtech.tis.extension.TestMultiSelectableValRoundTrip;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * 该模块可离线运行的单元测试聚合套件(由测试验证脚本自动整理生成)。
 * 依赖数据库/本地文件/内网服务/Testcontainers 的用例见 TEST_SKIP_REPORT.md。
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        com.qlangtech.tis.datax.impl.TestGenerateCfgs.class,
        com.qlangtech.tis.datax.impl.TestTableAlias.class,
        com.qlangtech.tis.extension.TestValueChangePipe.class,
        com.qlangtech.tis.extension.impl.TestEnumFormFieldType.class,
        com.qlangtech.tis.extension.impl.TestPropertyType.class,
        com.qlangtech.tis.extension.util.AbstractPropAssistTest.class,
        com.qlangtech.tis.extension.util.TestPluginExtraProps.class,
        com.qlangtech.tis.manage.common.UserProfileTest.class,
        com.qlangtech.tis.offline.TestFileSystemFactory.class,
        com.qlangtech.tis.plugin.annotation.TestFormFieldTypeColor.class,
        com.qlangtech.tis.plugin.annotation.TestFormFieldTypeDate.class,
        com.qlangtech.tis.plugin.annotation.TestValidator.class,
        com.qlangtech.tis.plugin.datax.TestCreateDDL.class,
        com.qlangtech.tis.plugin.datax.doris.TestDorisSelectedTab.class,
        com.qlangtech.tis.plugin.datax.format.guesstype.TestTargetTabsEntities.class,
        com.qlangtech.tis.plugin.ds.TestDataSourceFactory.class,
        com.qlangtech.tis.plugin.k8s.TestK8sImage.class,
        com.qlangtech.tis.plugin.license.TestTISLicense.class,
        com.qlangtech.tis.plugin.llm.TestQWenLLMProvider.class,
        com.qlangtech.tis.plugin.ontology.OntologyObjectTypeTest.class,
        com.qlangtech.tis.plugin.ontology.impl.objtype.TestDataSourceQueryCriteria.class,
        com.qlangtech.tis.plugin.proxy.TestHttpRequestProxy.class,
        com.qlangtech.tis.plugin.table.join.TableJoinFilterConditionTest.class,
        com.qlangtech.tis.plugin.utils.HexColorValidatorTest.class,
        com.qlangtech.tis.util.TestAttrValMap.class,
        com.qlangtech.tis.util.TestDescribableJSONModelAssist.class,
        com.qlangtech.tis.util.TestDescribableJSONParentChain.class,
        com.qlangtech.tis.util.TestDescriptorsJSONSkipAttrs.class,
        com.qlangtech.tis.util.TestPluginMeta.class,
        com.qlangtech.tis.util.TestRelevantFieldsContextAttrValMap.class,
        com.qlangtech.tis.util.TestTransformerRuleKey.class,
        com.qlangtech.tis.util.TestUploadPluginMeta.class,
        TestMultiSelectableValRoundTrip.class,
})
public class TestAll {
}
