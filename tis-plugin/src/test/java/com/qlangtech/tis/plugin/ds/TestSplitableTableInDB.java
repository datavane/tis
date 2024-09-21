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

package com.qlangtech.tis.plugin.ds;

import com.google.common.collect.Lists;
import com.qlangtech.tis.common.utils.Assert;
import junit.framework.TestCase;

import java.util.List;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-09-21 13:26
 **/
public class TestSplitableTableInDB extends TestCase {

    //@Test
    public void testRewritePhysicsTabs() {
        DBIdentity order2 = DBIdentity.parseId("order2");
        boolean prefixWildcardStyle = true;
        SplitableTableInDB splitableTableInDB = new SplitableTableInDB(order2, SplitTableStrategy.PATTERN_PHYSICS_TABLE, prefixWildcardStyle);
        String logicTab = "orderdetail";
        List<String> physicsTabs = Lists.newArrayList(logicTab + "_01", logicTab + "_02");
        List<String> flinkCDCMatchTabs
                = splitableTableInDB.rewritePhysicsTabs(logicTab, physicsTabs);
        Assert.assertNotNull(flinkCDCMatchTabs);
        Assert.assertEquals(logicTab + "_(\\d+)", String.join(",", flinkCDCMatchTabs));
    }
}