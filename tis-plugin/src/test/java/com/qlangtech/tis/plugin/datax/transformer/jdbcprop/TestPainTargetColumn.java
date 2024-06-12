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

package com.qlangtech.tis.plugin.datax.transformer.jdbcprop;

import com.qlangtech.tis.common.utils.Assert;
import com.qlangtech.tis.manage.common.Option;
import com.qlangtech.tis.plugin.datax.transformer.UDFDesc;
import com.qlangtech.tis.trigger.util.JsonUtil;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.List;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-06-12 17:01
 **/
public class TestPainTargetColumn extends TestCase {

    @Test
    public void testSerialize() {
        String colName = "user_name";
        PainTargetColumn targetColumn = new PainTargetColumn(colName);
        Assert.assertEquals("\"" + colName + "\"", JsonUtil.toString(targetColumn, true));

        List<UDFDesc> descs = targetColumn.getLiteria();
        Assert.assertEquals(1, descs.size());
        for (UDFDesc desc : descs) {
            Assert.assertEquals(1, desc.getPairs().size());
            for (Option opt : desc.getPairs()) {
                Assert.assertEquals("column", opt.getName());
                Assert.assertEquals(colName, opt.getValue());
            }
        }
    }
}
