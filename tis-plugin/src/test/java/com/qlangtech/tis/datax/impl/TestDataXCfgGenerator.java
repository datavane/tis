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

package com.qlangtech.tis.datax.impl;

import com.qlangtech.tis.common.utils.Assert;
import com.qlangtech.tis.datax.IDataXPluginMeta.DataXMeta;
import com.qlangtech.tis.datax.IDataxContext;
import com.qlangtech.tis.datax.IDataxGlobalCfg;
import com.qlangtech.tis.datax.IDataxProcessor;
import com.qlangtech.tis.datax.IDataxReader;
import com.qlangtech.tis.datax.IDataxReaderContext;
import com.qlangtech.tis.datax.IDataxWriter;
import com.qlangtech.tis.plugin.datax.transformer.RecordTransformer;
import com.qlangtech.tis.plugin.datax.transformer.RecordTransformerRules;
import com.qlangtech.tis.plugin.datax.transformer.impl.TestCopyValUDF;
import com.qlangtech.tis.plugin.datax.transformer.impl.VirtualTargetColumn;
import com.qlangtech.tis.plugin.datax.transformer.jdbcprop.TargetColType;
import com.qlangtech.tis.trigger.util.JsonUtil;
import com.qlangtech.tis.util.IPluginContext;
import junit.framework.TestCase;
import org.easymock.EasyMock;

import java.util.Optional;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-06-15 08:48
 **/
public class TestDataXCfgGenerator extends TestCase {

    /**
     * 测试Transformer的json生成
     *
     * @throws Exception
     */
    public void testGenerateDataxConfigForTransformer() throws Exception {
        String dataxName = "test";
        String transformerTable = "base";

        RecordTransformerRules.transformerRulesLoader4Test = (tab) -> {
            RecordTransformerRules tRules = new RecordTransformerRules();
            RecordTransformer transformer = new RecordTransformer();
            TestCopyValUDF cpUDF =  TestCopyValUDF.create();
//            cpUDF.from = "base_id";
//            TargetColType targetColType = new TargetColType();
//            VirtualTargetColumn targetColumn = new VirtualTargetColumn();
            final String baseId = "new_base_id";
//            targetColumn.name = baseId;
//            targetColType.setTarget(targetColumn);
//            cpUDF.to = targetColType;
            transformer.setUdf(cpUDF);
            tRules.rules.add(transformer);

            //
            Assert.assertEquals(baseId, String.join(",", tRules.relevantColKeys()));
            return tRules;
        };

        IPluginContext pluginCtx = IPluginContext.namedContext(dataxName);

        IDataxProcessor dataxProcessor = EasyMock.createMock("processor", IDataxProcessor.class);

        IDataxGlobalCfg globalCfg = EasyMock.createMock("globalCfg", IDataxGlobalCfg.class);

        EasyMock.expect(globalCfg.getTemplate())
                .andReturn("{\n" +
                        "        \"job\": {\n" +
                        "            \"content\": [\n" +
                        "                {\n" +
                        "                    \"reader\": <!--reader-->,\n" +
                        "                    \"writer\": <!--writer-->\n" +
                        "                }\n" +
                        "            ]\n" +
                        "        }\n" +
                        "}");

        EasyMock.expect(dataxProcessor.getDataXGlobalCfg()).andReturn(globalCfg);

        IDataxReaderContext readerContext = EasyMock.createMock("readerContext", IDataxReaderContext.class);
        EasyMock.expect(readerContext.getSourceEntityName()).andReturn(transformerTable).anyTimes();

        IDataxWriter writer = EasyMock.createMock("write", IDataxWriter.class);
        IDataxReader reader = EasyMock.createMock("reader", IDataxReader.class);
        EasyMock.expect(reader.getTemplate()).andReturn("{}").times(2);
        EasyMock.expect(writer.getTemplate()).andReturn("{}");
        EasyMock.expect(writer.getSubTask(Optional.empty(), Optional.empty())).andReturn(new IDataxContext() {
        });
        DataXMeta writeDataXMeta = new DataXMeta();
        EasyMock.expect(writer.getDataxMeta()).andReturn(writeDataXMeta);
        DataXMeta readerDataXMeta = new DataXMeta();
        EasyMock.expect(reader.getDataxMeta()).andReturn(readerDataXMeta);
        Optional<IDataxProcessor.TableMap> tableMap = Optional.empty();

        EasyMock.replay(writer, reader, readerContext, dataxProcessor, globalCfg);

        DataXCfgGenerator generator = new DataXCfgGenerator(pluginCtx, dataxName, dataxProcessor);
        String dataXCfg = generator.generateDataxConfig(readerContext, writer, reader, tableMap);

        String assertFileName = "generate-datax-config-for-transformer.json";

        JsonUtil.assertJSONEqual(TestDataXCfgGenerator.class, assertFileName, dataXCfg, (message, expected, actual) -> {
            Assert.assertEquals(message, expected, actual);
        });

        EasyMock.verify(writer, reader, readerContext, dataxProcessor, globalCfg);
    }

}
