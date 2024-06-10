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

package com.qlangtech.tis.plugin.datax.transformer.impl;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.fastjson.JSONArray;
import com.qlangtech.tis.common.utils.Assert;
import com.qlangtech.tis.extension.impl.IOUtils;
import com.qlangtech.tis.plugin.datax.transformer.RecordTransformer;
import com.qlangtech.tis.plugin.ds.CMeta.ParsePostMCols;
import com.qlangtech.tis.plugin.ds.DataType;
import com.qlangtech.tis.runtime.module.misc.IFieldErrorHandler;
import junit.framework.TestCase;
import org.easymock.EasyMock;

public class TestTransformerRuleElementCreatorFactory extends TestCase {

    public void testParsePostMCols() {
        TransformerRuleElementCreatorFactory creatorFactory = new TransformerRuleElementCreatorFactory();

        IFieldErrorHandler msgHandler = EasyMock.createMock("msgHandler", IFieldErrorHandler.class);
        Context context = EasyMock.createMock("context", Context.class);
        String keyColsMeta = "colsMeta";
        JSONArray jsonArray
                = JSONArray.parseArray(
                IOUtils.loadResourceFromClasspath(TransformerRuleElementCreatorFactory.class, "transformer-rule-sample.json"));
        EasyMock.replay(msgHandler, context);

        ParsePostMCols<RecordTransformer> postResult
                = creatorFactory.parsePostMCols(msgHandler, context, keyColsMeta, jsonArray);

        Assert.assertFalse(postResult.validateFaild);
        Assert.assertEquals(1, postResult.writerCols.size());
        DataType type = null;
        CopyValUDF udf = null;
        for (RecordTransformer transformer : postResult.writerCols) {
            Assert.assertEquals("sort_num", transformer.getTarget());
            type = transformer.getType();
            Assert.assertNotNull("type can not be null", type);
            Assert.assertEquals(-5, type.getType());

            Assert.assertTrue(transformer.getUdf() instanceof CopyValUDF);
            udf = (CopyValUDF) transformer.getUdf();
            Assert.assertNotNull(udf);

            Assert.assertEquals("emp_id", udf.from);

        }

        EasyMock.verify(msgHandler, context);
    }

}
