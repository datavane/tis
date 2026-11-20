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
import org.mockito.Mockito;

import java.util.Optional;

import org.junit.Test;
/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-06-15 08:48
 **/
public class TestDataXCfgGenerator {

    /**
     * 测试Transformer的json生成
     *
     * @throws Exception
     */
    @Test
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

        IDataxProcessor dataxProcessor = Mockito.mock(IDataxProcessor.class, Mockito.withSettings().name("processor"));

        IDataxGlobalCfg globalCfg = Mockito.mock(IDataxGlobalCfg.class, Mockito.withSettings().name("globalCfg"));

        Mockito.when(globalCfg.getTemplate())
                .thenReturn("{\n" +
                        "        \"job\": {\n" +
                        "            \"content\": [\n" +
                        "                {\n" +
                        "                    \"reader\": <!--reader-->,\n" +
                        "                    \"writer\": <!--writer-->\n" +
                        "                }\n" +
                        "            ]\n" +
                        "        }\n" +
                        "}");

        Mockito.when(dataxProcessor.getDataXGlobalCfg()).thenReturn(globalCfg);

        IDataxReaderContext readerContext = Mockito.mock(IDataxReaderContext.class, Mockito.withSettings().name("readerContext"));
        Mockito.when(readerContext.getSourceEntityName()).thenReturn(transformerTable);

        IDataxWriter writer = Mockito.mock(IDataxWriter.class, Mockito.withSettings().name("write"));
        IDataxReader reader = Mockito.mock(IDataxReader.class, Mockito.withSettings().name("reader"));
        Mockito.when(reader.getTemplate()).thenReturn("{}");
        Mockito.when(writer.getTemplate()).thenReturn("{}");
        Mockito.when(writer.getSubTask(Optional.empty(), Optional.empty())).thenReturn(new IDataxContext() {
        });
        DataXMeta writeDataXMeta = new DataXMeta();
        Mockito.when(writer.getDataxMeta()).thenReturn(writeDataXMeta);
        DataXMeta readerDataXMeta = new DataXMeta();
        Mockito.when(reader.getDataxMeta()).thenReturn(readerDataXMeta);
        Optional<IDataxProcessor.TableMap> tableMap = Optional.empty();

        DataXCfgGenerator generator = new DataXCfgGenerator(pluginCtx, dataxName, dataxProcessor);
        String dataXCfg = generator.generateDataxConfig(readerContext, writer, reader, tableMap);

        String assertFileName = "generate-datax-config-for-transformer.json";

        JsonUtil.assertJSONEqual(TestDataXCfgGenerator.class, assertFileName, dataXCfg, (message, expected, actual) -> {
            Assert.assertEquals(message, expected, actual);
        });

        Mockito.verify(globalCfg).getTemplate();
        Mockito.verify(dataxProcessor).getDataXGlobalCfg();
        Mockito.verify(writer).getTemplate();
        Mockito.verify(writer).getSubTask(Optional.empty(), Optional.empty());
        Mockito.verify(writer).getDataxMeta();
        Mockito.verify(reader, Mockito.times(2)).getTemplate();
        Mockito.verify(reader).getDataxMeta();
    }

}
